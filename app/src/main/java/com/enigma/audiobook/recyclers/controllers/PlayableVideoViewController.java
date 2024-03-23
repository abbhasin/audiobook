package com.enigma.audiobook.recyclers.controllers;

import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.enigma.audiobook.utils.Utils.addTryCatch;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.enigma.audiobook.backend.models.ContentUploadStatus;
import com.enigma.audiobook.proxies.ViewsService;
import com.enigma.audiobook.utils.ALog;
import com.enigma.audiobook.utils.RetryHelper;
import com.enigma.audiobook.viewHolders.FeedItemViewHolder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlayableVideoViewController {
    private static final String TAG = "PlayableVideoViewController";
    private static final int BASE_VIEW_HEARTBEAT_TIME_SEC = 5;
    private ImageView thumbnail;
    private ProgressBar progressBar;
    private VideoView videoView;
    private MediaController mediaController;
    private View.OnClickListener onClickListenerOnReset;
    private ImageView videoPlayPause;

    Handler handlerViewDuration;
    Runnable runnableViewDuration;
    int viewDurationCount = 0;

    String id;
    String fromUserId;
    ContentUploadStatus contentUploadStatus;
    ViewsService viewsService;

    public void init(MediaController mediaController,
                     String fromUserId,
                     ViewsService viewsService) {
        this.mediaController = mediaController;
        this.fromUserId = fromUserId;
        this.viewsService = viewsService;
        this.handlerViewDuration = new Handler();
    }

    public void playVideo(ImageView thumbnail_, ProgressBar progressBar_, VideoView videoView_,
                          String videoUrl_,
                          String id,
                          ContentUploadStatus contentUploadStatus
    ) {
        this.id = id;
        this.contentUploadStatus = contentUploadStatus;
        playVideoInternal(thumbnail_, progressBar_, videoView_, videoUrl_, null, null);
    }

    public void playVideo(ImageView thumbnail_, ProgressBar progressBar_, VideoView videoView_, String videoUrl_,
                          View.OnClickListener onClickListenerOnReset_, ImageView videoPlayPause_) {
        this.id = null;
        this.contentUploadStatus = null;
        playVideoInternal(thumbnail_, progressBar_, videoView_,
                videoUrl_, onClickListenerOnReset_, videoPlayPause_);
    }

    public void playVideoInternal(
            ImageView thumbnail_, ProgressBar progressBar_, VideoView videoView_, String videoUrl_,
            View.OnClickListener onClickListenerOnReset_, ImageView videoPlayPause_) {
        resetVideoFeedInternal(false);
        onClickListenerOnReset = onClickListenerOnReset_;
        videoPlayPause = videoPlayPause_;
        ALog.i(TAG, "play video called");
        thumbnail = thumbnail_;
        videoView = videoView_;
        progressBar = progressBar_;

        AudioAttributes.Builder builder = new AudioAttributes.Builder();
        builder.setContentType(AudioAttributes.CONTENT_TYPE_MOVIE);
        builder.setUsage(AudioAttributes.USAGE_MEDIA);

        videoView.setAudioAttributes(builder.build());

        String mediaUrl = videoUrl_;
        ALog.i(TAG, "media uri:" + mediaUrl);
        if (mediaUrl != null) {
            Uri trackUri = Uri.parse(mediaUrl);
            thumbnail.setVisibility(GONE);
            videoView.setVisibility(VISIBLE);
            progressBar.setVisibility(VISIBLE);

            runnableViewDuration = new Runnable() {
                @Override
                public void run() {
                    if (videoView.isPlaying()) {
                        int maxDuration = videoView.getDuration();
                        if (maxDuration > 0) {
                            addViewing(maxDuration,
                                    viewDurationCount * BASE_VIEW_HEARTBEAT_TIME_SEC * 1000);
                            viewDurationCount++;
                        }
                    }
                    handlerViewDuration.postDelayed(runnableViewDuration,
                            BASE_VIEW_HEARTBEAT_TIME_SEC * 1000L);
                }
            };

            final VideoView currentVV = videoView;
            final ProgressBar currentProgressBar = progressBar;
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    ALog.i(TAG, "video view started playing at position");
                    addTryCatch(() -> {
                        if (currentProgressBar != null) {
                            currentProgressBar.setVisibility(GONE);
                        }
                        mp.setLooping(true);
                        currentVV.start();
                        currentVV.setMediaController(mediaController);
                        handlerViewDuration.post(runnableViewDuration);
                    }, TAG);
                }
            });

            videoView.setVideoURI(trackUri);

            videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    ALog.i(TAG, "video view errored at position");
                    resetVideoFeed();
                    return false;
                }
            });
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    ALog.i(TAG, "video view completed, seeking to 0, at position");
                    mp.seekTo(0);
                }
            });

            videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    if (what == MEDIA_INFO_BUFFERING_START && progressBar != null) {
                        currentProgressBar.setVisibility(VISIBLE);
                    } else if (what == MEDIA_INFO_BUFFERING_END && progressBar != null) {
                        currentProgressBar.setVisibility(GONE);
                    }
                    return false;
                }
            });
        }
    }

    private void addViewing(int maxDurationMS, int viewDurationMS) {
        addTryCatch(() -> {
            if (id != null && maxDurationMS > 0) {
                com.enigma.audiobook.backend.models.View view =
                        new com.enigma.audiobook.backend.models.View();
                view.setPostId(this.id);
                view.setUserId(this.fromUserId);
                view.setViewDurationSec(viewDurationMS / 1000);
                view.setTotalLengthSec(maxDurationMS / 1000);
                Call<Void> call = viewsService.addViewing(view);
                RetryHelper.enqueueWithRetry(call, 1, new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!response.isSuccessful()) {
                            ALog.i(TAG, "ERROR: unable to post viewing:" + response);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        ALog.e(TAG, "unable to post viewing:", t);
                    }
                });
            }
        }, TAG);
    }

    public void resetVideoFeed() {
        resetVideoFeedInternal(true);
    }

    public void resetVideoFeedInternal(boolean resetPostBasedVars) {
        if (videoView != null) {
            addTryCatch(() -> {
                addTryCatch(() -> {
                    videoView.stopPlayback();
                }, TAG);
                mediaController.setEnabled(false);
                videoView.setVisibility(GONE);
                progressBar.setVisibility(GONE);
                thumbnail.setVisibility(VISIBLE);
                if (onClickListenerOnReset != null && videoPlayPause != null) {
                    videoPlayPause.setVisibility(VISIBLE);
                    videoPlayPause.setOnClickListener(onClickListenerOnReset);
                }

                videoView = null;
                progressBar = null;
                thumbnail = null;
                onClickListenerOnReset = null;
                videoPlayPause = null;
                handlerViewDuration.removeCallbacks(runnableViewDuration);
                viewDurationCount = 0;
                runnableViewDuration = null;
                if (resetPostBasedVars) {
                    id = null;
                    contentUploadStatus = null;
                }
            }, TAG);
        }
    }
}
