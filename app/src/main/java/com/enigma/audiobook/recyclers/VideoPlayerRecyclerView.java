package com.enigma.audiobook.recyclers;

import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START;

import static com.enigma.audiobook.utils.Utils.addTryCatch;

import android.content.Context;
import android.graphics.Rect;
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
        addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                ALog.i(TAG, "scroll state changed");
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    ALog.i(TAG, "onScrollStateChanged: called.");

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
//                ALog.i(TAG, "scrolling...");
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

            if(endPosition - startPosition > 1) {
                endPosition = startPosition + 1;
            }

            // if there is more than 1 list-item on the screen
            if (startPosition != endPosition) {
                int startPositionVisibility = getVisibilityPercents(startPosition);
                int endPositionVisibility = getVisibilityPercents(endPosition);
                ALog.i(TAG, "visibility percents start:" + startPositionVisibility + " end:" + endPositionVisibility);
                targetPosition = endPositionVisibility > startPositionVisibility ? endPosition : startPosition;
            } else {
                targetPosition = startPosition;
            }
        } else {
            targetPosition = mediaObjects.size() - 1;
        }

        ALog.i(TAG, "playVideo: target position: " + targetPosition);

        // video is already playing so return
        if (targetPosition == playPosition) {
//            mediaController.show(3000);
            return;
        }

        resetVideoView();

        // set the position of the list-item that is to be played
        playPosition = targetPosition;

        int currentViewGroupPosition = targetPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        ALog.i(TAG, "playVideo: current view group position: " + currentViewGroupPosition);
        View child = getChildAt(currentViewGroupPosition);
        if (child == null) {
            ALog.i(TAG, "playVideo: no child at current view group position: " + currentViewGroupPosition);
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

        String mediaUrl = mediaObjects.get(playPosition).getVideoUrl();
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
                    ALog.i(TAG, "video view started playing at position:" + playPosition);
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
                    ALog.i(TAG, "video view errored at position:" + playPosition);
                    resetVideoView();
                    return false;
                }
            });
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    ALog.i(TAG, "video view completed, seeking to 0, at position:" + playPosition);
                    mp.seekTo(0);
//                    scrollToNextPosition();
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

    private void scrollToNextPosition() {
        int nextPosition = (playPosition == mediaObjects.size() - 1) ? playPosition :
                playPosition + 1;
        getLayoutManager().scrollToPosition(nextPosition);
    }

    private void resetVideoView() {
        ALog.i(TAG, "resetting video view, vv is null:" + (videoView == null));
        if (videoView != null) {
            addTryCatch(() -> {
                videoView.stopPlayback();
                mediaController.setEnabled(false);
                videoView.setVisibility(GONE);
                progressBar.setVisibility(GONE);
                thumbnail.setVisibility(VISIBLE);

                playPosition = -1;
                videoView = null;
            }, TAG);
        }
    }

    public int getVisibilityPercents(int position) {
        int at = position - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        ALog.i(TAG, "getVisibleVideoSurfaceHeight: at: " + at);

        View child = getChildAt(at);
        if (child == null) {
            return 0;
        }
        return getVisibilityPercents(child);
    }

    public int getVisibilityPercents(View currentView) {

        int percents = 100;
        Rect rect = new Rect();
        currentView.getLocalVisibleRect(rect);
        ALog.i(TAG, "getVisibilityPercents rect top " + rect.top + ", left " + rect.left +
                ", bottom " + rect.bottom + ", right " + rect.right);

        int height = currentView.getHeight();
        ALog.i(TAG, "current view height:" + height);

        if(viewIsPartiallyHiddenTop(rect)){
            // view is partially hidden behind the top edge
            percents = (height - rect.top) * 100 / height;
        } else if(viewIsPartiallyHiddenBottom(height, rect)){
            percents = rect.bottom * 100 / height;
        }

        return percents;
    }

    private boolean viewIsPartiallyHiddenBottom(int height, Rect rect) {
        return rect.bottom > 0 && rect.bottom < height;
    }

    private boolean viewIsPartiallyHiddenTop(Rect rect) {
        return rect.top > 0;
    }

    public void setMediaObjects(List<VideoMediaObject> mediaObjects) {
        this.mediaObjects = mediaObjects;
    }

    public void setMediaController(MediaController mediaController) {
        this.mediaController = mediaController;
    }
}