package com.enigma.audiobook.viewHolders;

import android.content.Context;
import android.content.Intent;
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
import com.enigma.audiobook.utils.ALog;
import com.enigma.audiobook.utils.ActivityResultLauncherProvider;
import com.enigma.audiobook.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    LinearLayout musicLinearLayout;
    Button musicPlayPauseBtn;
    String musicUrl;
    SeekBar musicSeekBar;
    TextView musicLengthTotalTime, musicLengthProgressTime;

    View parent;
    Button addImages, addVideo, addAudio, submit, clearContent;
    EditText title, description;
    Spinner tagsSpinner;
    Map<String, PostMessageModel.SpinnerTag> tagTextToTag;

    RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
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
        setupDescriptionET(context);
        clearAllVisualAudioContent(cardItem);

        setupSpinner(cardItem, context);
        setupSubmit(cardItem, context);
        setupClearAll(cardItem);

        setupAddVideos(cardItem, context, requestManager);
        setupAddImages(cardItem, context, requestManager);
        setupAddAudio(cardItem, context, requestManager);
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
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ALog.i(TAG, String.format("title:%s, desc:%s tag:%s", title.getText().toString(), description.getText().toString(), tagsSpinner.getSelectedItem().toString()));
                String selectedItemTxt = tagsSpinner.getSelectedItem().toString();
                if(cardItem.getSelectedItemPosition() == 0) {
                    Toast.makeText(context, "please select God's Tag", Toast.LENGTH_SHORT).show();
                    return;
                }
                ;
                PostMessageModel.SpinnerTag tag = tagTextToTag.get(selectedItemTxt);

                clearTextContent();
                clearAllVisualAudioContent(cardItem);
                cardItem.clearVideoAudioContent();
                cardItem.clearTextContent();
                setupSpinner(cardItem, context);
            }
        });
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
                ALog.i(TAG, "on spinner item selected:"+position);
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
