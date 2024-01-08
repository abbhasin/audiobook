package com.enigma.audiobook.recyclers.controllers;

import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.enigma.audiobook.utils.Utils.addTryCatch;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.enigma.audiobook.utils.ALog;
import com.enigma.audiobook.viewHolders.FeedItemViewHolder;

public class PlayableVideoViewController {
    private static final String TAG = "PlayableVideoViewController";
    private ImageView thumbnail;
    private ProgressBar progressBar;
    private VideoView videoView;
    private MediaController mediaController;
    private View.OnClickListener onClickListenerOnReset;
    private ImageView videoPlayPause;

    public void init(MediaController mediaController) {
        this.mediaController = mediaController;
    }

    public void playVideo(ImageView thumbnail_, ProgressBar progressBar_, VideoView videoView_, String videoUrl_) {
        playVideo(thumbnail_, progressBar_, videoView_, videoUrl_, null, null);
    }

    public void playVideo(ImageView thumbnail_, ProgressBar progressBar_, VideoView videoView_, String videoUrl_,
                          View.OnClickListener onClickListenerOnReset_, ImageView videoPlayPause_) {
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

    public void resetVideoFeed() {
        if (videoView != null) {
            addTryCatch(() -> {
                videoView.stopPlayback();
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
            }, TAG);
        }
    }
}
