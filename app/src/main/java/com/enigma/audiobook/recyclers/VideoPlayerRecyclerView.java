package com.enigma.audiobook.recyclers;

import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.enigma.audiobook.adapters.VideoRecyclerViewAdapter;
import com.enigma.audiobook.models.VideoMediaObject;
import com.enigma.audiobook.utils.ALog;

import java.util.ArrayList;
import java.util.List;

public class VideoPlayerRecyclerView extends RecyclerView {

    private static final String TAG = "VideoPlayerRecyclerView";

    private enum VolumeState {ON, OFF}

    ;

    // ui
    private ImageView thumbnail, volumeControl;
    private ProgressBar progressBar;

    private VideoView videoView;
    private View viewHolderParent;
    // vars
    private List<VideoMediaObject> mediaObjects = new ArrayList<>();

    private MediaController mediaController;
    private Context context;
    private int playPosition = -1;
    private boolean isVideoViewAdded;

    // controlling playback state
    private VolumeState volumeState;

    public VideoPlayerRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public VideoPlayerRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context) {
        this.context = context.getApplicationContext();
        setVolumeControl(VolumeState.ON);
        addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                ALog.i(TAG, "scroll state changed");
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    ALog.i(TAG, "onScrollStateChanged: called.");
                    if (thumbnail != null) { // show the old thumbnail
                        thumbnail.setVisibility(VISIBLE);
                    }

                    // There's a special case when the end of the list has been reached.
                    // Need to handle that with this bit of logic
                    if (!recyclerView.canScrollVertically(1)) {
                        playVideo(true);
                    } else {
                        playVideo(false);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                ALog.i(TAG, "scrolling...");
                mediaController.hide();
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        addOnChildAttachStateChangeListener(new OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {

            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                resetVideoView();
            }
        });
    }

    public void playVideo(boolean isEndOfList) {

        int targetPosition;
        LinearLayoutManager layoutManager = ((LinearLayoutManager) getLayoutManager());
//        View focusedChild = layoutManager.getFocusedChild();

        if (!isEndOfList) {
            int startPosition = layoutManager.findFirstVisibleItemPosition();
            int endPosition = layoutManager.findLastVisibleItemPosition();

            // something is wrong. return.
            if (startPosition < 0 || endPosition < 0) {
                return;
            }

            // if there is more than 1 list-item on the screen
            if (startPosition != endPosition) {
                targetPosition = endPosition - startPosition > 1 ? startPosition + 1 : startPosition;
            } else {
                targetPosition = startPosition;
            }
        } else {
            targetPosition = mediaObjects.size() - 1;
        }

        ALog.i(TAG, "playVideo: target position: " + targetPosition);

//        int focusedChildPosition = layoutManager.getPosition(focusedChild);

//        ALog.i(TAG, "playVideo: focused child position: " + focusedChildPosition);
        // video is already playing so return
        if (targetPosition == playPosition) {
            mediaController.show(3000);
            return;
        }

        resetVideoView();

        // set the position of the list-item that is to be played
        playPosition = targetPosition;

        int currentViewGroupPosition = targetPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        ALog.i(TAG, "playVideo: current view group position: " + currentViewGroupPosition);
        View child = getChildAt(currentViewGroupPosition);
        if (child == null) {
            return;
        }

        VideoRecyclerViewAdapter.VideoPlayerViewHolder holder = (VideoRecyclerViewAdapter.VideoPlayerViewHolder) child.getTag();
        if (holder == null) {
            playPosition = -1;
            return;
        }
        thumbnail = holder.getThumbnail();
        progressBar = holder.getProgressBar();
        volumeControl = holder.getVolumeControl();
        viewHolderParent = holder.itemView;
        videoView = holder.getVideoView();

        AudioAttributes.Builder builder = new AudioAttributes.Builder();
        builder.setContentType(AudioAttributes.CONTENT_TYPE_MOVIE);
        builder.setUsage(AudioAttributes.USAGE_MEDIA);

        videoView.setAudioAttributes(builder.build());
        viewHolderParent.setOnClickListener(videoViewClickListener);

        String mediaUrl = mediaObjects.get(playPosition).getVideoUrl();
        ALog.i(TAG, "zzz media uri:" + mediaUrl);
        if (mediaUrl != null) {
            ALog.i(TAG, "media uri:" + mediaUrl);
            Uri trackUri = Uri.parse(mediaUrl);
            ALog.i(TAG, "track uri:" + trackUri);
            videoView.setVisibility(VISIBLE);
            thumbnail.setVisibility(GONE);
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    ALog.i(TAG, "video view started playing at position:" + playPosition);
                    if (progressBar != null) {
                        progressBar.setVisibility(GONE);
                    }
                    mp.setLooping(true);
                    videoView.start();
                    videoView.setMediaController(mediaController);
                }
            });

            videoView.setVideoURI(trackUri);
//            videoView.start();
            ALog.i(TAG, "video view audio session:" + videoView.getAudioSessionId());
            ALog.i(TAG, "video view isPlaying:" + videoView.isPlaying());


            videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    ALog.i(TAG, "video view errored at position:" + playPosition);
                    return false;
                }
            });
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    ALog.i(TAG, "video view completed, seeking to 0, at position:" + playPosition);
                    mp.seekTo(0);
                }
            });

            videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    if (what == MEDIA_INFO_BUFFERING_START && progressBar != null) {
                        progressBar.setVisibility(VISIBLE);
                    } else if (what == MEDIA_INFO_BUFFERING_END && progressBar != null) {
                        progressBar.setVisibility(GONE);
                    }
                    return false;
                }
            });
        }
    }

    private OnClickListener videoViewClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            toggleVolume();
        }
    };

    private void resetVideoView() {
        ALog.i(TAG, "resetting video view, vv is null:" + (videoView == null));
        if (videoView != null) {
            thumbnail.setVisibility(VISIBLE);
            videoView.setVisibility(GONE);
            videoView.stopPlayback();
            playPosition = -1;
            videoView = null;
        }
    }


    private void toggleVolume() {
//        if (videoPlayer != null) {
//            if (volumeState == VolumeState.OFF) {
//                Log.d(TAG, "togglePlaybackState: enabling volume.");
//                setVolumeControl(VolumeState.ON);
//
//            } else if (volumeState == VolumeState.ON) {
//                Log.d(TAG, "togglePlaybackState: disabling volume.");
//                setVolumeControl(VolumeState.OFF);
//
//            }
//        }
    }

    private void setVolumeControl(VolumeState state) {
        volumeState = state;
//        if (state == VolumeState.OFF) {
//            videoPlayer.setVolume(0f);
//
//            animateVolumeControl();
//        } else if (state == VolumeState.ON) {
//            videoPlayer.setVolume(1f);
//            animateVolumeControl();
//        }
    }


    public void setMediaObjects(List<VideoMediaObject> mediaObjects) {
        this.mediaObjects = mediaObjects;
    }

    public void setMediaController(MediaController mediaController){
        this.mediaController = mediaController;
    }
}