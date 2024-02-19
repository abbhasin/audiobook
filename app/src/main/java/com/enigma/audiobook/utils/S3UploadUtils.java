package com.enigma.audiobook.utils;

import android.util.Pair;

import com.enigma.audiobook.backend.aws.models.S3MPUCompletedPart;
import com.enigma.audiobook.backend.aws.models.S3MPUPreSignedUrlsResponse;
import com.enigma.audiobook.proxies.RestClient;
import com.enigma.audiobook.services.PostMessageService;

import org.apache.http.NameValuePair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

public class S3UploadUtils {

    private static int concurrentUploads = 5;
    private static ExecutorService executorService = Executors.newFixedThreadPool(concurrentUploads);
    private static RestClient restClient = new RestClient();
    private static final Semaphore completedPartsSemaphore = new Semaphore(concurrentUploads);

    /**
     * execute on a thread other than the main thread
     */
    public static List<S3MPUCompletedPart> uploadParts(S3MPUPreSignedUrlsResponse s3MPUPreSignedUrlsResponse,
                                                       File file,
                                                       AtomicReference<PostMessageService.Progress> progressRef) {
        long chunkSize = s3MPUPreSignedUrlsResponse.getChunkSize();
        long totalNumOfParts = s3MPUPreSignedUrlsResponse.getTotalNumOfParts();
        Map<Integer, String> partNumToUrl = s3MPUPreSignedUrlsResponse.getPartNumToUrl();


        int concurrentPacketUploads = concurrentUploads;
        List<Future<S3MPUCompletedPart>> packets = new ArrayList<>();
        List<S3MPUCompletedPart> completedParts = new ArrayList<>();
        int partNum = 1;

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[(int) chunkSize];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                Pair<Integer, byte[]> pair = new Pair<>(partNum++, Arrays.copyOf(buffer, len));

                // add to futures of uploads
                // upload parts waits for at least one permit to become available before
                // proceeding further, thus total data in memory will be
                // (concurrentUploads + 1) * chunkSize
                Future<S3MPUCompletedPart> s3MPUCompletedPartFuture =
                        uploadPart(pair, partNumToUrl, progressRef);
                packets.add(s3MPUCompletedPartFuture);

//                if (packets.size() == concurrentPacketUploads) {
//                    // wait for completion
//                    completedParts.addAll(waitForCompletion(packets));
//                    packets.clear();
//                }
            }
            if (!packets.isEmpty()) {
                // wait for completion
                completedParts.addAll(waitForCompletion(packets));
                packets.clear();
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (totalNumOfParts != partNum - 1 || completedParts.size() != totalNumOfParts) {
            throw new IllegalStateException(String.format("parts count is different, partNum:%s, totalParts:%s, completeParts:%s",
                    partNum - 1, totalNumOfParts, completedParts));
        }
        return completedParts;
    }

    private static List<S3MPUCompletedPart> waitForCompletion(List<Future<S3MPUCompletedPart>> packets) {
        List<S3MPUCompletedPart> completedParts = new ArrayList<>();
        for (Future<S3MPUCompletedPart> completedPartFuture : packets) {
            try {
                S3MPUCompletedPart completedPart = completedPartFuture.get();
                completedParts.add(completedPart);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        return completedParts;
    }

    private static Future<S3MPUCompletedPart> uploadPart(Pair<Integer, byte[]> partNumAndData,
                                                         Map<Integer, String> partNumToUrl,
                                                         AtomicReference<PostMessageService.Progress> progressRef) throws InterruptedException {
        completedPartsSemaphore.acquire();
        return executorService.submit(() -> {
            try {
                return new UploadPartCallable(
                        partNumAndData.first,
                        partNumAndData.second,
                        partNumToUrl.get(partNumAndData.first),
                        progressRef).call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                completedPartsSemaphore.release();
            }
        });
    }

    public static class UploadPartCallable implements Callable<S3MPUCompletedPart> {
        final int partNum;
        final byte[] data;
        final String url;
        final AtomicReference<PostMessageService.Progress> progressRef;

        public UploadPartCallable(int partNum, byte[] data, String url,
                                  AtomicReference<PostMessageService.Progress> progressRef) {
            this.partNum = partNum;
            this.data = data;
            this.url = url;
            this.progressRef = progressRef;
        }

        @Override
        public S3MPUCompletedPart call() throws Exception {
            // TODO: add retries
            RestClient.HeaderAndEntity response = restClient.doPut(url, data);
            Optional<String> ETag = response.getHeaders().stream().filter(header -> header.getName().equals("ETag"))
                    .map(NameValuePair::getValue)
                    .findFirst();

            S3MPUCompletedPart completedPart = new S3MPUCompletedPart();
            completedPart.setPartNum(partNum);
            completedPart.setSize(data.length);
            completedPart.setETag(ETag.orElseThrow(() -> new IllegalStateException("no etag found from upload")));

            updateProgress();
            return completedPart;
        }

        private void updateProgress() {
            progressRef.get().getCompletedParts().incrementAndGet();
        }
    }
}
