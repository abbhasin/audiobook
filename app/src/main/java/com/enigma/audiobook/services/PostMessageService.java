package com.enigma.audiobook.services;

import static com.enigma.audiobook.utils.S3UploadUtils.uploadParts;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.enigma.audiobook.backend.aws.models.MPURequestStatus;
import com.enigma.audiobook.backend.aws.models.S3MPUCompletedPart;
import com.enigma.audiobook.backend.aws.models.S3MPUPreSignedUrlsResponse;
import com.enigma.audiobook.backend.models.Post;
import com.enigma.audiobook.backend.models.PostType;
import com.enigma.audiobook.backend.models.requests.PostContentUploadReq;
import com.enigma.audiobook.backend.models.requests.PostInitRequest;
import com.enigma.audiobook.backend.models.requests.UploadCompletionReq;
import com.enigma.audiobook.backend.models.requests.UploadFileCompletionReq;
import com.enigma.audiobook.backend.models.requests.UploadFileInitReq;
import com.enigma.audiobook.backend.models.requests.UploadInitReq;
import com.enigma.audiobook.backend.models.responses.PostCompletionResponse;
import com.enigma.audiobook.backend.models.responses.PostInitResponse;
import com.enigma.audiobook.backend.models.responses.UploadFileInitRes;
import com.enigma.audiobook.backend.models.responses.UploadInitRes;
import com.enigma.audiobook.models.PostMessageModel;
import com.enigma.audiobook.proxies.PostMsgProxyService;
import com.enigma.audiobook.proxies.RetrofitFactory;
import com.enigma.audiobook.utils.ALog;
import com.google.firebase.components.Preconditions;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Response;

public class PostMessageService extends Service {
    private static final String TAG = "PostMessageService";
    private final IBinder srvBinder = new PostMessageSrvBinder();
    private final ExecutorService executor = Executors.newFixedThreadPool(1);
    private PostMsgProxyService proxyService;
    private AtomicReference<PostMessageModel> currentPost = new AtomicReference<>();
    private AtomicReference<PostMsgProcessorResponse> currentPostMsgResponseRef =
            new AtomicReference<>();
    private AtomicReference<Status> currentStatusRef = new AtomicReference<>(Status.SUCCESS);
    private AtomicReference<Future<?>> currentPostHandlerFut = new AtomicReference<>();
    private AtomicReference<Progress> progressRef = new AtomicReference<>();

    public class PostMessageSrvBinder extends Binder {
        public PostMessageService getService() {
            return PostMessageService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return srvBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        proxyService = RetrofitFactory.getInstance().createService(PostMsgProxyService.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static class MakePostResponse {
        boolean initiatedPost;
        String notInitiationReason;

        public boolean isInitiatedPost() {
            return initiatedPost;
        }

        public void setInitiatedPost(boolean initiatedPost) {
            this.initiatedPost = initiatedPost;
        }

        public String getNotInitiationReason() {
            return notInitiationReason;
        }

        public void setNotInitiationReason(String notInitiationReason) {
            this.notInitiationReason = notInitiationReason;
        }
    }

    public MakePostResponse makePost(PostMessageModel postCard) {
        if (currentStatusRef.get().equals(Status.IN_PROGRESS)) {
            MakePostResponse response = new MakePostResponse();
            response.setInitiatedPost(false);
            response.setNotInitiationReason("Another message is already in progress, please wait for its completion or cancel before posting another message");
            return response;
        }
        currentPost.set(postCard);
        progressRef.set(new Progress(new AtomicLong(0),
                new AtomicLong(0)));
        currentStatusRef.set(Status.IN_PROGRESS);
        Future<?> postHandlerFut =
                executor.submit(new PostHandler(new PostMsgProcessorCallable(proxyService, postCard,
                        progressRef),
                        currentPostMsgResponseRef,
                        currentStatusRef,
                        progressRef));
        currentPostHandlerFut.set(postHandlerFut);

        MakePostResponse response = new MakePostResponse();
        response.setInitiatedPost(true);
        return response;
    }

    public void cancelRunningPost() {

    }

    public Optional<Progress> getProgress() {
        if (progressRef.get() != null) {
            return Optional.of(progressRef.get());
        }

        return Optional.empty();
    }

    public Status getStatus() {
        return currentStatusRef.get();
    }

    public Optional<PostMessageModel> getLatestPost() {
        return Optional.ofNullable(currentPost.get());
    }

    public static class Progress {
        private final AtomicLong totalParts;
        private final AtomicLong completedParts;

        public Progress(AtomicLong totalParts, AtomicLong completedParts) {
            this.totalParts = totalParts;
            this.completedParts = completedParts;
        }

        public AtomicLong getTotalParts() {
            return totalParts;
        }

        public AtomicLong getCompletedParts() {
            return completedParts;
        }

        @Override
        public String toString() {
            return "Progress{" +
                    "totalParts=" + totalParts.get() +
                    ", completedParts=" + completedParts.get() +
                    '}';
        }
    }

    public static class PostMsgProcessorResponse {
        private final Status status;
        private final String abortReason;

        public PostMsgProcessorResponse(Status status, String abortReason) {
            this.status = status;
            this.abortReason = abortReason;
        }

        public Status getStatus() {
            return status;
        }

        public String getAbortReason() {
            return abortReason;
        }
    }

    public enum Status {
        IN_PROGRESS(false),
        SUCCESS(true),
        FAILED(true);

        private final boolean terminal;

        Status(boolean terminal) {
            this.terminal = terminal;
        }

        public boolean isTerminal() {
            return terminal;
        }
    }

    public static class PostHandler implements Runnable {

        private final PostMsgProcessorCallable callable;
        private final AtomicReference<PostMsgProcessorResponse> processorResponseRef;
        private final AtomicReference<Status> currentStatus;
        private final AtomicReference<Progress> progressRef;

        public PostHandler(PostMsgProcessorCallable callable,
                           AtomicReference<PostMsgProcessorResponse> processorResponseRef,
                           AtomicReference<Status> currentStatus,
                           AtomicReference<Progress> progressRef) {
            this.callable = callable;
            this.processorResponseRef = processorResponseRef;
            this.currentStatus = currentStatus;
            this.progressRef = progressRef;
        }

        @Override
        public void run() {
            try {
                PostMsgProcessorResponse response = callable.call();
                processorResponseRef.set(response);
                currentStatus.set(response.status);
            } catch (Exception e) {
                ALog.e(TAG, "unable to make post message call", e);
                processorResponseRef.set(new PostMsgProcessorResponse(Status.FAILED,
                        "Unknown Reason"));
                currentStatus.set(Status.FAILED);
            }
        }
    }

    public static class PostMsgProcessorCallable implements Callable<PostMsgProcessorResponse> {
        private final PostMsgProxyService proxyService;
        private final PostMessageModel postCard;
        private final AtomicReference<Progress> progressRef;

        public PostMsgProcessorCallable(PostMsgProxyService proxyService, PostMessageModel postCard,
                                        AtomicReference<Progress> progressRef) {
            this.proxyService = proxyService;
            this.postCard = postCard;
            this.progressRef = progressRef;
        }

        @Override
        public PostMsgProcessorResponse call() throws Exception {
            try {
                return invoke();
            } catch (Exception e) {
                ALog.e(TAG, "unable to make post message", e);
                return new PostMsgProcessorResponse(Status.FAILED,
                        "Unknown Reason");
            }
        }

        private PostMsgProcessorResponse invoke() {
            // init
            PostInitResponse postInitResponse = initPost();

            UploadCompletionReq uploadCompletionReq = null;
            // upload parts
            switch (postCard.getType()) {
                case VIDEO:
                case AUDIO:
                case IMAGES:
                    UploadInitRes uploadInitRes = postInitResponse.getUploadInitRes();
                    if (!uploadInitRes.getRequestStatus().equals(MPURequestStatus.COMPLETED)) {
                        return new PostMsgProcessorResponse(Status.FAILED,
                                uploadInitRes.getAbortedReason().toString());
                    }

                    initProgress(postInitResponse);
                    // upload parts
                    List<UploadFileCompletionReq> uploadFileCompletionReqs =
                            uploadPostParts(postInitResponse);

                    uploadCompletionReq = new UploadCompletionReq();
                    uploadCompletionReq.setUploadFileCompletionReqs(uploadFileCompletionReqs);
                    break;
                case TEXT:
                    return new PostMsgProcessorResponse(Status.SUCCESS, "");
            }

            // complete post
            PostCompletionResponse completionResponse = completePost(postInitResponse.getPost(),
                    uploadCompletionReq);
            if (!completionResponse.getUploadCompletionRes().getRequestStatus().equals(MPURequestStatus.COMPLETED)) {
                return new PostMsgProcessorResponse(Status.FAILED,
                        completionResponse.getUploadCompletionRes().getAbortedReason().toString());
            }
            updateProgress();
            return new PostMsgProcessorResponse(Status.SUCCESS, "");
        }

        private void updateProgress() {
            progressRef.get().getCompletedParts().incrementAndGet();
        }

        private void initProgress(PostInitResponse postInitResponse) {
            Progress progress = new Progress(new AtomicLong(getTotalParts(postInitResponse) + 1),
                    new AtomicLong(0));
            progressRef.set(progress);
        }

        private long getTotalParts(PostInitResponse postInitResponse) {
            Optional<Long> totalParts =
                    postInitResponse.getUploadInitRes()
                            .getFileNameToUploadFileResponse()
                            .entrySet()
                            .stream()
                            .map(entry ->
                                    entry.getValue()
                                            .getS3MPUPreSignedUrlsResponse()
                                            .getTotalNumOfParts())
                            .reduce(Long::sum);
            Preconditions.checkArgument(totalParts.isPresent(), "total parts not present");
            return totalParts.get();
        }

        private List<UploadFileCompletionReq> uploadPostParts(PostInitResponse postInitResponse) {
            UploadInitRes uploadInitRes = postInitResponse.getUploadInitRes();
            Map<String, File> fileNameToFile = getNameToFile();
            List<UploadFileCompletionReq> uploadFileCompletionReqs =
                    uploadInitRes.getFileNameToUploadFileResponse()
                            .entrySet()
                            .stream()
                            .map(entry -> {
                                String fileName = entry.getKey();
                                UploadFileInitRes uploadFileInitRes = entry.getValue();
                                S3MPUPreSignedUrlsResponse s3MPUPreSignedUrlsResponse =
                                        uploadFileInitRes.getS3MPUPreSignedUrlsResponse();
                                List<S3MPUCompletedPart> completedParts =
                                        uploadParts(s3MPUPreSignedUrlsResponse,
                                                fileNameToFile.get(fileName),
                                                progressRef);

                                UploadFileCompletionReq fileCompletionReq = new UploadFileCompletionReq();

                                fileCompletionReq.setUploadId(uploadFileInitRes.getUploadId());
                                fileCompletionReq.setFileName(fileName);
                                fileCompletionReq.setObjectKey(uploadFileInitRes.getObjectKey());
                                fileCompletionReq.setS3MPUCompletedParts(completedParts);
                                return fileCompletionReq;
                            }).collect(Collectors.toList());
            return uploadFileCompletionReqs;
        }

        private Map<String, File> getNameToFile() {
            Map<String, File> fileNameToFile = new HashMap<>();
            switch (postCard.getType()) {
                case VIDEO:
                    File vfile = new File(postCard.getVideoUrl());
                    fileNameToFile.put(vfile.getName(), vfile);
                    break;
                case AUDIO:
                    File afile = new File(postCard.getMusicUrl());
                    fileNameToFile.put(afile.getName(), afile);
                    break;
                case IMAGES:
                    return postCard.getImagesUrl()
                            .stream()
                            .map(File::new)
                            .collect(Collectors.toMap(File::getName, f -> f));
                case TEXT:
                    throw new IllegalStateException("no file exists for Text type, we should not be called here");
            }
            return fileNameToFile;
        }

        private PostCompletionResponse completePost(Post post, UploadCompletionReq uploadCompletionReq) {
            PostContentUploadReq contentUploadReq = new PostContentUploadReq();
            contentUploadReq.setPost(post);
            contentUploadReq.setUploadCompletionReq(uploadCompletionReq);
            // TODO: add retries
            Call<PostCompletionResponse> completionResponseCall = proxyService.completePost(contentUploadReq);
            try {
                Response<PostCompletionResponse> completionResponse = completionResponseCall.execute();
                if (!completionResponse.isSuccessful()) {
                    throw new RuntimeException("unable to complete post:" + completionResponse.code());
                }
                return completionResponse.body();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private PostInitResponse initPost() {
            Call<PostInitResponse> initResponseCall = proxyService.initPost(convertToInitRequest(postCard));
            // TODO: add retries
            try {
                Response<PostInitResponse> response = initResponseCall.execute();
                if (!response.isSuccessful()) {
                    throw new RuntimeException("unable to init post:" + response.code());
                }
                return response.body();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private PostInitRequest convertToInitRequest(PostMessageModel postCard) {
            PostInitRequest postInitRequest = new PostInitRequest();
            postInitRequest.setPost(createPost(postCard));
            UploadInitReq uploadInitReq = null;
            switch (postCard.getType()) {
                case VIDEO:
                    File videoFile = new File(postCard.getVideoUrl());

                    uploadInitReq = create(Arrays.asList(videoFile));
                    break;
                case AUDIO:
                    File audioFile = new File(postCard.getMusicUrl());

                    uploadInitReq = create(Arrays.asList(audioFile));
                    break;
                case IMAGES:
                    List<File> imagesFiles = postCard.getImagesUrl()
                            .stream()
                            .map(File::new)
                            .collect(Collectors.toList());
                    uploadInitReq = create(imagesFiles);
                    break;
                case TEXT:
                    break;
            }
            postInitRequest.setUploadInitReq(uploadInitReq);
            return postInitRequest;
        }

        private Post createPost(PostMessageModel postCard) {
            Post post = new Post();
            // static items of a postCard
            post.setAssociationType(postCard.getAssociationType());
            switch (postCard.getAssociationType()) {
                case GOD:
                    post.setAssociatedGodId(postCard.getAssociatedGodId());
                    break;
                case MANDIR:
                    post.setAssociatedMandirId(postCard.getAssociatedMandirId());
                    break;
                case INFLUENCER:
                    post.setAssociatedInfluencerId(postCard.getAssociatedInfluencerId());
                    break;
            }
            post.setFromUserId(postCard.getFromUserId());

            // dynamic items
            post.setTitle(postCard.getTitle());
            post.setDescription(postCard.getDescription());
            post.setTag(postCard.getSelectedItem().getText());

            switch (postCard.getType()) {
                case VIDEO:
                    post.setType(PostType.VIDEO);
                    break;
                case AUDIO:
                    post.setType(PostType.AUDIO);
                    break;
                case IMAGES:
                    post.setType(PostType.IMAGES);
                    break;
                case TEXT:
                    post.setType(PostType.TEXT);
                    break;
            }

            return post;
        }

        private UploadInitReq create(List<File> files) {
            UploadInitReq uploadInitReq = new UploadInitReq();
            List<UploadFileInitReq> uploadFileInitReqs =
                    files.stream()
                            .map(file -> {
                                UploadFileInitReq uploadFileInitReq = new UploadFileInitReq();
                                uploadFileInitReq.setFileName(file.getName());
                                uploadFileInitReq.setTotalSize(file.length());
                                return uploadFileInitReq;
                            })
                            .collect(Collectors.toList());

            uploadInitReq.setUploadFileInitReqs(uploadFileInitReqs);
            return uploadInitReq;
        }
    }
}
