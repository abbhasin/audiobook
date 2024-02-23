package com.enigma.audiobook.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.enigma.audiobook.utils.ContentUtils;
import com.enigma.audiobook.utils.OneGodContentUploadUtils;
import com.enigma.audiobook.utils.RetryHelper;
import com.google.firebase.components.Preconditions;
//import com.enigma.audiobook.utils.ALog;

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

        currentPostMsgResponseRef.set(null);
        currentPost.set(postCard);
        progressRef.set(new Progress(new AtomicLong(0),
                new AtomicLong(0)));

        currentStatusRef.set(Status.IN_PROGRESS);

        ALog.i(TAG, "creating new post:" + postCard);
        Future<?> postHandlerFut =
                executor.submit(new PostHandler(new PostMsgProcessorCallable(getApplicationContext(),
                        proxyService,
                        postCard,
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

    public Optional<PostMsgProcessorResponse> getResponse() {
        return Optional.ofNullable(currentPostMsgResponseRef.get());
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
                ALog.i(TAG, "post handler initiated");
                PostMsgProcessorResponse response = callable.call();
                processorResponseRef.set(response);
                currentStatus.set(response.status);
            } catch (Exception e) {
                ALog.e(TAG, "unable to make post message call", e);
                processorResponseRef.set(new PostMsgProcessorResponse(Status.FAILED,
                        "Please check internet connection"));
                currentStatus.set(Status.FAILED);
            }
        }
    }

    public static class PostMsgProcessorCallable implements Callable<PostMsgProcessorResponse> {
        private final PostMsgProxyService proxyService;
        private final PostMessageModel postCard;
        private final AtomicReference<Progress> progressRef;
        private final Context context;

        public PostMsgProcessorCallable(Context applicationContext, PostMsgProxyService proxyService, PostMessageModel postCard,
                                        AtomicReference<Progress> progressRef) {
            this.proxyService = proxyService;
            this.postCard = postCard;
            this.progressRef = progressRef;
            this.context = applicationContext;
        }

        @Override
        public PostMsgProcessorResponse call() throws Exception {
            try {
                ALog.i(TAG, "post msg processor initiated");
                return invoke();
            } catch (Exception e) {
                ALog.e(TAG, "unable to make post message", e);
                return new PostMsgProcessorResponse(Status.FAILED,
                        "Please check internet connection");
            }
        }

        private PostMsgProcessorResponse invoke() {
            // init
            ALog.i(TAG, "initiating init request");
            PostInitResponse postInitResponse = initPost();
//            ALog.i(TAG, "init response:" + postInitResponse);

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
                    updateProgressToSuccess();
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

        private void updateProgressToSuccess() {
            Progress progress = new Progress(new AtomicLong(1),
                    new AtomicLong(1));
            ALog.i(TAG, "progress success:" + progress);
            progressRef.set(progress);
        }

        private void updateProgress() {
            progressRef.get().getCompletedParts().incrementAndGet();
        }

        private void initProgress(PostInitResponse postInitResponse) {
            Progress progress = new Progress(new AtomicLong(getTotalParts(postInitResponse) + 1),
                    new AtomicLong(0));
            ALog.i(TAG, "init progress:" + progress);
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
//            ALog.i(TAG, "initiating upload post parts:" + postInitResponse.getUploadInitRes()
//                    .getFileNameToUploadFileResponse());
            UploadInitRes uploadInitRes = postInitResponse.getUploadInitRes();
            Map<String, Uri> fileNameToUri = getNameToUri();
//            ALog.i(TAG, "initiating upload post parts fileNameToUri:" + fileNameToUri);
            List<UploadFileCompletionReq> uploadFileCompletionReqs =
                    uploadInitRes.getFileNameToUploadFileResponse()
                            .entrySet()
                            .stream()
                            .map(entry -> {
                                String fileName = entry.getKey();
                                UploadFileInitRes uploadFileInitRes = entry.getValue();
                                S3MPUPreSignedUrlsResponse s3MPUPreSignedUrlsResponse =
                                        uploadFileInitRes.getS3MPUPreSignedUrlsResponse();
//                                ALog.i(TAG, String.format("initiating upload post parts for file:%s, mpuURls:%s",
//                                        fileName, s3MPUPreSignedUrlsResponse));
                                try {
                                    List<S3MPUCompletedPart> completedParts =
                                            OneGodContentUploadUtils.uploadParts(
                                                    context,
                                                    s3MPUPreSignedUrlsResponse,
                                                    fileNameToUri.get(fileName),
                                                    progressRef);
                                    ALog.i(TAG, String.format("initiating upload post parts completed:%s",
                                            completedParts));

                                    UploadFileCompletionReq fileCompletionReq = new UploadFileCompletionReq();

                                    fileCompletionReq.setUploadId(uploadFileInitRes.getUploadId());
                                    fileCompletionReq.setFileName(fileName);
                                    fileCompletionReq.setObjectKey(uploadFileInitRes.getObjectKey());
                                    fileCompletionReq.setS3MPUCompletedParts(completedParts);
                                    return fileCompletionReq;
                                } catch (Throwable e) {
                                    ALog.e(TAG, "error while uploading parts", e);
                                    throw  new RuntimeException(e);
                                }

                            }).collect(Collectors.toList());
            return uploadFileCompletionReqs;
        }

        private Map<String, Uri> getNameToUri() {
            Map<String, Uri> fileNameToFile = new HashMap<>();
            switch (postCard.getType()) {
                case VIDEO:
                    Uri vfile = Uri.parse(postCard.getVideoUrl());
                    fileNameToFile.put(ContentUtils.getFileName(context, vfile), vfile);
                    break;
                case AUDIO:
                    Uri afile = Uri.parse(postCard.getMusicUrl());
                    fileNameToFile.put(ContentUtils.getFileName(context, afile), afile);
                    break;
                case IMAGES:
                    return postCard.getImagesUrl()
                            .stream()
                            .map(Uri::parse)
                            .collect(Collectors.toMap(uri -> ContentUtils.getFileName(context, uri),
                                    uri -> uri));
                case TEXT:
                    throw new IllegalStateException("no file exists for Text type, we should not be called here");
            }
            return fileNameToFile;
        }

        private PostCompletionResponse completePost(Post post, UploadCompletionReq uploadCompletionReq) {
            PostContentUploadReq contentUploadReq = new PostContentUploadReq();
            contentUploadReq.setPost(post);
            contentUploadReq.setUploadCompletionReq(uploadCompletionReq);

            Call<PostCompletionResponse> completionResponseCall = proxyService.completePost(contentUploadReq);
            try {
                Response<PostCompletionResponse> completionResponse =
                        RetryHelper.executeWithRetry(completionResponseCall);
                if (!completionResponse.isSuccessful()) {
                    throw new RuntimeException("unable to complete post:" + completionResponse.code());
                }
                return completionResponse.body();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private PostInitResponse initPost() {
            Call<PostInitResponse> initResponseCall = proxyService.initPost(convertToInitRequest(postCard));
            try {
                Response<PostInitResponse> response = RetryHelper.executeWithRetry(initResponseCall);
                if (!response.isSuccessful()) {
                    throw new RuntimeException("unable to init post:" + response.code());
                }
                return response.body();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private PostInitRequest convertToInitRequest(PostMessageModel postCard) {
            PostInitRequest postInitRequest = new PostInitRequest();
            postInitRequest.setPost(createPost(postCard));
            UploadInitReq uploadInitReq = null;
            switch (postCard.getType()) {
                case VIDEO:
                    Uri videoFile = Uri.parse(postCard.getVideoUrl());

                    uploadInitReq = create(Arrays.asList(videoFile));
                    break;
                case AUDIO:
                    Uri audioFile = Uri.parse(postCard.getMusicUrl());

                    uploadInitReq = create(Arrays.asList(audioFile));
                    break;
                case IMAGES:
                    List<Uri> imagesFiles = postCard.getImagesUrl()
                            .stream()
                            .map(img -> Uri.parse(img))
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

        private UploadInitReq create(List<Uri> files) {
            UploadInitReq uploadInitReq = new UploadInitReq();
            List<UploadFileInitReq> uploadFileInitReqs =
                    files.stream()
                            .map(file -> {
                                UploadFileInitReq uploadFileInitReq = new UploadFileInitReq();
                                uploadFileInitReq.setFileName(ContentUtils.getFileName(context, file));
                                uploadFileInitReq.setTotalSize(ContentUtils.getFileSize(context, file));
                                return uploadFileInitReq;
                            })
                            .collect(Collectors.toList());

            uploadInitReq.setUploadFileInitReqs(uploadFileInitReqs);
            return uploadInitReq;
        }
    }
}
