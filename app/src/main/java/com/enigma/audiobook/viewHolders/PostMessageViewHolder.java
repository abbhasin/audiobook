package com.enigma.audiobook.viewHolders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.enigma.audiobook.R;
import com.enigma.audiobook.adapters.FeedImagesChildRVAdapter;
import com.enigma.audiobook.models.PostMessageModel;
import com.enigma.audiobook.pageTransformers.ScrollingPagerIndicator;
import com.enigma.audiobook.recyclers.controllers.PlayableMusicViewController;
import com.enigma.audiobook.recyclers.controllers.PlayableVideoViewController;
import com.enigma.audiobook.services.PostMessageService;
import com.enigma.audiobook.utils.ALog;
import com.enigma.audiobook.utils.ActivityResultLauncherProvider;
import com.enigma.audiobook.utils.ContentUtils;
import com.enigma.audiobook.utils.PostMessageServiceProvider;
import com.enigma.audiobook.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class PostMessageViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "PostMessageViewHolder";
    LinearLayout mediaContentLL;
    ImageView videoThumbnail;
    VideoView videoView;
    ProgressBar progressBar;
    String videoUrl;
    ImageView videoPlayPause;

    List<String> imagesUrl;
    RecyclerView imagesChildRV;
    ScrollingPagerIndicator indicator;
    FeedImagesChildRVAdapter childItemAdapter;

    LinearLayout musicLinearLayout, lastPostLL;
    Button musicPlayPauseBtn;
    String musicUrl;
    SeekBar musicSeekBar;
    TextView musicLengthTotalTime, musicLengthProgressTime, lastPostTitle, lastPostFiles,
            lastPostStatus, lastPostReason, lastPostProgressPercent;
    ProgressBar lastPostProgressBar;

    View parent;
    Button addImages, addVideo, addAudio, submit, clearContent;
    EditText title, description;
    Spinner tagsSpinner;
    Map<String, PostMessageModel.SpinnerTag> tagTextToTag;

    PostMessageService postMessageService;
    RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
    Handler handlerLastPostProgressBar;
    Runnable runnableLastPostProgressBar;
    private static PlayableMusicViewController musicViewController;
    private static PlayableVideoViewController videoViewController;

    public static void setPlayableMusicViewController(PlayableMusicViewController musicViewController_) {
        musicViewController = musicViewController_;
    }

    public static void setPlayableVideoViewController(PlayableVideoViewController videoViewController_) {
        videoViewController = videoViewController_;
    }

    public PostMessageViewHolder(@NonNull View itemView) {
        super(itemView);
        parent = itemView;
        title = itemView.findViewById(R.id.cardPostMessageAddTitleET);
        description = itemView.findViewById(R.id.cardPostMessageAddDescriptionET);
        tagsSpinner = itemView.findViewById(R.id.cardPostMessageSelectTagSpinner);

        addImages = itemView.findViewById(R.id.cardPostMessageAddImagesBtn);
        addVideo = itemView.findViewById(R.id.cardPostMessageAddVideoBtn);
        addAudio = itemView.findViewById(R.id.cardPostMessageAddAudioBtn);

        mediaContentLL = itemView.findViewById(R.id.cardPostMessageMediaContentLL);

        musicLinearLayout = itemView.findViewById(R.id.cardPostMessageMusicLL);
        musicPlayPauseBtn = itemView.findViewById(R.id.cardPostMessageMusicPlayPauseBtn);
        musicSeekBar = itemView.findViewById(R.id.cardPostMessageMusicSeekBar);
        musicLengthTotalTime = itemView.findViewById(R.id.cardPostMessageMusicLengthTotalTime);
        musicLengthProgressTime = itemView.findViewById(R.id.cardPostMessageMusicLengthProgress);

        imagesChildRV = itemView.findViewById(R.id.cardPostMessageImagesChildRecyclerView);
        indicator = itemView.findViewById(R.id.cardPostMessageImagesChildRecyclerViewScrollPageIndicator);

        videoView = itemView.findViewById(R.id.cardPostMessageContentVideo);
        progressBar = itemView.findViewById(R.id.cardPostMessageContentVideoProgressBar);
        videoThumbnail = itemView.findViewById(R.id.cardPostMessageContentThumbnail);
        videoPlayPause = itemView.findViewById(R.id.cardPostMessageContentPlayPauseBtn);

        submit = itemView.findViewById(R.id.cardPostMessageSubmit);
        clearContent = itemView.findViewById(R.id.cardPostMessageClearAssets);

        // last post items
        lastPostLL = itemView.findViewById(R.id.cardPostMessageLastPostLL);
        lastPostTitle = itemView.findViewById(R.id.cardPostMessageLastPostTitleTxt);
        lastPostFiles = itemView.findViewById(R.id.cardPostMessageLastPostFilesTxt);
        lastPostStatus = itemView.findViewById(R.id.cardPostMessageLastPostStatusTxt);
        lastPostReason = itemView.findViewById(R.id.cardPostMessageLastPostReasonTxt);
        lastPostProgressPercent = itemView.findViewById(R.id.cardPostMessageLastPostProgressPercentTxt);

        lastPostProgressBar = itemView.findViewById(R.id.cardPostMessageLastPostProgressBar);
    }

    private void setupDescriptionET(Context context) {
        description.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.cardPostMessageAddDescriptionET) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });
    }

    public void onBind(PostMessageModel cardItem, RequestManager requestManager, Context context, int position) {
        parent.setTag(this);
        initPostMsgService(context);

        handlerLastPostProgressBar = new Handler();
        updateLastPostDetails();
        setupDescriptionET(context);
        clearAllVisualAudioContent(cardItem);

        setupSpinner(cardItem, context);
        setupSubmit(cardItem, context);
        setupClearAll(cardItem);

        setupAddVideos(cardItem, context, requestManager);
        setupAddImages(cardItem, context, requestManager);
        setupAddAudio(cardItem, context, requestManager);
    }

    private void initPostMsgService(Context context) {
        if (postMessageService == null) {
            if (((PostMessageServiceProvider) context).isServiceBound()) {
                postMessageService = ((PostMessageServiceProvider) context).getPostMessageService();
            }
        }
    }

    private void setupClearAll(PostMessageModel cardItem) {
        clearContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllVisualAudioContent(cardItem);
                cardItem.clearVideoAudioContent();
            }
        });
    }

    private void clearTextContent() {
        description.getText().clear();
        title.getText().clear();
    }

    private void clearAllVisualAudioContent(PostMessageModel cardItem) {
        videoUrl = null;
        if (!Utils.isEmpty(imagesUrl)) {
            imagesUrl.clear();
        }
        if (childItemAdapter != null) {
            childItemAdapter.notifyDataSetChanged();
        }
        musicUrl = null;

        videoView.stopPlayback();
        videoViewController.resetVideoFeed();
        musicViewController.resetMusicFeed();

        setVideoVisibility(View.GONE);
        setMusicVisibility(View.GONE);
        setImagesVisibility(View.GONE);
        mediaContentLL.setVisibility(View.GONE);
    }

    private void setupSubmit(PostMessageModel cardItem, Context context) {
        try {
            validate(context, cardItem);
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    initPostMsgService(context);
                    ALog.i(TAG, String.format("title:%s, desc:%s tag:%s", title.getText().toString(), description.getText().toString(), tagsSpinner.getSelectedItem().toString()));
                    cardItem.setTitle(title.getText().toString());
                    cardItem.setDescription(description.getText().toString());
                    if (!isValidStateForSubmission(context, cardItem)) {
                        return;
                    }

                    if (!postMessageService.getStatus().isTerminal()) {
                        Toast.makeText(context,
                                "a post message is already in progress. Please wait for it to complete or cancel it before posting next message",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    PostMessageModel clonedModel = new PostMessageModel(cardItem);
                    ALog.i(TAG, "cloned post message model:" + clonedModel);
                    if (clonedModel.getType().equals(PostMessageModel.PostMessageType.VIDEO)) {
                        Uri videoFile = Uri.parse(clonedModel.getVideoUrl());
                        long size = ContentUtils.getFileSize(context, videoFile);
                        ALog.i(TAG, "testing video length:" + size);
                        if (size <= 0) {
                            throw new IllegalStateException("video length is wrong");
                        }
                    }

                    PostMessageService.MakePostResponse response =
                            postMessageService.makePost(clonedModel);
                    if (!response.isInitiatedPost()) {
                        Toast.makeText(context, response.getNotInitiationReason(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updateLastPostDetails();

                    clearTextContent();
                    clearAllVisualAudioContent(cardItem);
                    cardItem.clearVideoAudioContent();
                    cardItem.clearTextContent();
                    setupSpinner(cardItem, context);
                }
            });
        } catch (Exception e) {
            ALog.e(TAG, "unable to validate post message", e);
            submit.setClickable(false);
        }
    }

    @SuppressLint("DefaultLocale")
    private void updateLastPostDetails() {
        Optional<PostMessageModel> lastPostCard = postMessageService.getLatestPost();
        if (!lastPostCard.isPresent()) {
            lastPostLL.setVisibility(View.GONE);
        } else {
            lastPostLL.setVisibility(View.VISIBLE);
            lastPostTitle.setText(lastPostCard.get().getTitle());
            switch (lastPostCard.get().getType()) {
                case VIDEO:
                    lastPostFiles.setText(new File(lastPostCard.get().getVideoUrl()).getName());
                    break;
                case AUDIO:
                    lastPostFiles.setText(new File(lastPostCard.get().getMusicUrl()).getName());
                    break;
                case IMAGES:
                    String concatenatedFileNames = getConcatenatedFileNamesForImages(lastPostCard.get());
                    lastPostFiles.setText(concatenatedFileNames);
                    break;
                case TEXT:
                    lastPostFiles.setText("");
                    break;
            }
            PostMessageService.Status status = postMessageService.getStatus();
            lastPostStatus.setText(status.toString());
            runnableLastPostProgressBar = new Runnable() {

                @Override
                public void run() {
                    ALog.i(TAG, "last post progress runnable executing");
                    PostMessageService.Status status = postMessageService.getStatus();
                    lastPostStatus.setText(status.toString());
                    Optional<PostMessageService.Progress> progress = postMessageService.getProgress();
                    ALog.i(TAG, String.format("last post progress runnable, progress present:%s, status:%s", progress.isPresent(), status));
                    if (progress.isPresent()) {
                        ALog.i(TAG, String.format("last post progress runnable, progress:%s, status:%s", progress.get(), status));
                        int totalParts = (int) progress.get().getTotalParts().get();
                        if (totalParts > 0) {
                            lastPostProgressBar.setMax(totalParts);
                            int completedParts = (int) progress.get().getCompletedParts().get();
                            lastPostProgressBar.setProgress(completedParts);

                            float progressPerc = getPercent(progress.get().getTotalParts().get(), progress.get().getCompletedParts().get());
                            ALog.i(TAG, "progress per:" + progressPerc);
                            lastPostProgressPercent.setText(String.format("%d%%", (int) progressPerc));
                        } else {
                            lastPostProgressBar.setMax(0);
                            lastPostProgressBar.setProgress(0);
                            lastPostProgressPercent.setText(String.format("%d%%", 0));
                        }
                    }
                    if (!status.isTerminal()) {
                        handlerLastPostProgressBar.postDelayed(runnableLastPostProgressBar, 1000);
                    }
                }
            };
            handlerLastPostProgressBar.post(runnableLastPostProgressBar);
        }
    }

    private float getPercent(long total, long done) {
        return ((float) done / total) * 100;
    }

    private String getConcatenatedFileNamesForImages(PostMessageModel postMessageModel) {
        StringJoiner joiner = new StringJoiner(", ");
        postMessageModel.getImagesUrl()
                .forEach(img -> joiner.add(new File(img).getName()));
        return joiner.toString();
    }

    private boolean isValidStateForSubmission(Context context, PostMessageModel cardItem) {
        validate(context, cardItem);

        if (cardItem.getSelectedItemPosition() == 0) {
            Toast.makeText(context, "please select God's Tag", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (Utils.isEmpty(cardItem.getTitle())) {
            Toast.makeText(context, "please add a title", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (Utils.isEmpty(cardItem.getDescription())) {
            Toast.makeText(context, "please add a description", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void validate(Context context, PostMessageModel cardItem) {
//        Preconditions.checkState(!Utils.isEmpty(cardItem.getFromUserId()), "from user id is empty");
//        Preconditions.checkState(cardItem.getAssociationType() != null, "association type is empty");
//        switch (cardItem.getAssociationType()) {
//            case GOD:
//                Preconditions.checkState(!Utils.isEmpty(cardItem.getAssociatedGodId()), "god id is empty");
//                break;
//            case MANDIR:
//                Preconditions.checkState(!Utils.isEmpty(cardItem.getAssociatedMandirId()), "mandir id is empty");
//                break;
//            case INFLUENCER:
//                Preconditions.checkState(!Utils.isEmpty(cardItem.getAssociatedInfluencerId()), "influencer id is empty");
//                break;
//        }
    }

    private void setupAddVideos(PostMessageModel cardItem, Context context, RequestManager requestManager) {
        ALog.i(TAG, "cardItem type:" + cardItem.getType());
        ActivityResultLauncher<PickVisualMediaRequest> pickVideo = ((ActivityResultLauncherProvider) context).getPickVideoLauncher();
        if (cardItem.getType() == PostMessageModel.PostMessageType.VIDEO) {
            ALog.i(TAG, "initially cardItem videoUrl(should be empty):" + videoUrl);
            mediaContentLL.setVisibility(View.VISIBLE);
            videoThumbnail.setVisibility(View.VISIBLE);
            videoUrl = cardItem.getVideoUrl();
            ALog.i(TAG, "cardItem videoUrl:" + videoUrl);

            requestManager.load(cardItem.getVideoUrl()).into(videoThumbnail);
            videoPlayPause.setVisibility(View.VISIBLE);
            videoPlayPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    videoPlayPause.setVisibility(View.GONE);
                    videoViewController.resetVideoFeed();
                    musicViewController.resetMusicFeed();
                    videoViewController.playVideo(videoThumbnail, progressBar, videoView,
                            cardItem.getVideoUrl(), new VideoOnClickListener(), videoPlayPause);
                }
            });
        }

        addVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityResultContracts.PickVisualMedia.VisualMediaType mediaType = ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE;

                pickVideo.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(mediaType)
                        .build());
            }
        });
    }

    private void setupAddImages(PostMessageModel cardItem, Context context, RequestManager requestManager) {
        ActivityResultLauncher<PickVisualMediaRequest> pickMultipleImages = ((ActivityResultLauncherProvider) context).getPickImagesLauncher();

        if (cardItem.getType() == PostMessageModel.PostMessageType.IMAGES) {
            mediaContentLL.setVisibility(View.VISIBLE);
            setImagesVisibility(View.VISIBLE);
            imagesUrl = new ArrayList<>(cardItem.getImagesUrl());
            setupImagesChildRV(requestManager);
        }
        addImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityResultContracts.PickVisualMedia.VisualMediaType mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE;

                pickMultipleImages.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(mediaType)
                        .build());
            }
        });
    }

    private void setupAddAudio(PostMessageModel cardItem, Context context, RequestManager requestManager) {
        ActivityResultLauncher<Intent> pickAudio = ((ActivityResultLauncherProvider) context).getPickAudioLauncher();

        if (cardItem.getType() == PostMessageModel.PostMessageType.AUDIO) {
            mediaContentLL.setVisibility(View.VISIBLE);
            setMusicVisibility(View.VISIBLE);

            musicPlayPauseBtn.setClickable(false);
            musicSeekBar.setClickable(false);

            musicUrl = cardItem.getMusicUrl();
            videoViewController.resetVideoFeed();
            musicViewController.resetMusicFeed();
            musicViewController.playMusic(musicPlayPauseBtn, musicSeekBar,
                    musicLengthTotalTime, musicLengthProgressTime, musicUrl,
                    new MusicOnClickListener());

        }
        addAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_upload = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                pickAudio.launch(intent_upload);
            }
        });
    }

    public class VideoOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            videoViewController.resetVideoFeed();
            musicViewController.resetMusicFeed();
            videoPlayPause.setVisibility(View.GONE);
            videoViewController.playVideo(videoThumbnail, progressBar, videoView,
                    videoUrl, this, videoPlayPause);
        }
    }

    public class MusicOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            videoViewController.resetVideoFeed();
            musicViewController.resetMusicFeed();
            musicViewController.playMusic(musicPlayPauseBtn, musicSeekBar,
                    musicLengthTotalTime, musicLengthProgressTime, musicUrl,
                    this);
        }
    }

    private void setupImagesChildRV(RequestManager requestManager) {
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(
                imagesChildRV.getContext(),
                LinearLayoutManager.HORIZONTAL,
                false);

        layoutManager.setInitialPrefetchItemCount(imagesUrl.size());

        childItemAdapter
                = new FeedImagesChildRVAdapter(
                requestManager, imagesUrl);
        imagesChildRV.setLayoutManager(layoutManager);
        imagesChildRV.setAdapter(childItemAdapter);
        imagesChildRV.setRecycledViewPool(viewPool);
        indicator.attachToRecyclerView(imagesChildRV);
    }

    private void setImagesVisibility(int vis) {
        imagesChildRV.setVisibility(vis);
        indicator.setVisibility(vis);
    }

    private void setVideoVisibility(int vis) {
        videoView.setVisibility(vis);
        videoThumbnail.setVisibility(vis);
        progressBar.setVisibility(vis);
        videoPlayPause.setVisibility(vis);

    }

    private void setMusicVisibility(int vis) {
        musicLinearLayout.setVisibility(vis);
    }

    private void setupSpinner(PostMessageModel cardItem, Context context) {
        List<PostMessageModel.SpinnerTag> tags = cardItem.getSpinnerList();
        List<String> spinnerItems = tags.stream().map(tag -> tag.getText()).collect(Collectors.toList());
        tagTextToTag = tags.stream().collect(Collectors.toMap(tag -> tag.getText(), tag -> tag));
        ALog.i(TAG, "Zzzz cardItem:" + cardItem);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                R.layout.spinner_list_item_custom, spinnerItems);
        adapter.setDropDownViewResource(R.layout.spinner_list_item_custom);
        tagsSpinner.setAdapter(adapter);
        tagsSpinner.setSelection(cardItem.getSelectedItemPosition());
        tagsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ALog.i(TAG, "on spinner item selected:" + position);
                cardItem.setSelectedItemPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ALog.i(TAG, "on spinner item deselected");
                cardItem.setSelectedItemPosition(0);
            }
        });
    }
}
