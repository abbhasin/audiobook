package com.enigma.audiobook.recyclers;

import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START;
import static com.enigma.audiobook.adapters.GodPageRVAdapter.GodPageViewTypes.FEED_ITEM;
import static com.enigma.audiobook.utils.Utils.addTryCatch;
import static com.enigma.audiobook.utils.Utils.convertMSToTime;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.enigma.audiobook.R;
import com.enigma.audiobook.adapters.GodPageRVAdapter;
import com.enigma.audiobook.adapters.MandirPageRVAdapter;
import com.enigma.audiobook.adapters.MyFeedRVAdapter;
import com.enigma.audiobook.adapters.PujariPageRVAdapter;
import com.enigma.audiobook.models.FeedItemModel;
import com.enigma.audiobook.models.GenericPageCardItemModel;
import com.enigma.audiobook.recyclers.controllers.PlayableMusicViewController;
import com.enigma.audiobook.recyclers.controllers.PlayableVideoViewController;
import com.enigma.audiobook.services.MediaPlayerService;
import com.enigma.audiobook.utils.ALog;
import com.enigma.audiobook.viewHolders.FeedItemViewHolder;
import com.enigma.audiobook.viewHolders.PostMessageViewHolder;

import java.util.ArrayList;
import java.util.List;

public class PlayableFeedBasedRecyclerView extends RecyclerView {

    private static final String TAG = "PlayableFeedBasedRecyclerView";
    // video feed items:
    private ImageView thumbnail;
    private ProgressBar progressBar;
    private VideoView videoView;
    private MediaController mediaController;

    // music feed items:
    private MediaPlayerService musicSrv;
    private Intent musicPlayIntent = null;
    private boolean musicBound = false;

    // others
    private View viewHolderParent;
    // vars
    private List<? extends GenericPageCardItemModel<?>> mediaObjects = new ArrayList<>();
    private Context context;
    private int playPosition = -1;
    private PlayableMusicViewController musicViewController;
    private PlayableVideoViewController videoViewController;

    public PlayableFeedBasedRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public PlayableFeedBasedRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context) {
        this.context = context.getApplicationContext();
        musicViewController = new PlayableMusicViewController();
        videoViewController = new PlayableVideoViewController();
        PostMessageViewHolder.setPlayableMusicViewController(musicViewController);
        PostMessageViewHolder.setPlayableVideoViewController(videoViewController);
        addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                ALog.i(TAG, "scroll state changed");
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    ALog.i(TAG, "onScrollStateChanged to idle.");

                    // There's a special case when the end of the list has been reached.
                    // Need to handle that with this bit of logic
                    if (!recyclerView.canScrollVertically(1)) {
                        playFeedItem(true);
                    } else {
                        playFeedItem(false);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                ALog.i(TAG, "scrolling...");
                mediaController.hide();
//                mediaController.setEnabled(false);
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        addOnChildAttachStateChangeListener(new OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {

            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                resetPlayingFeedItem();
            }
        });
    }

    public void playFeedItem(boolean isEndOfList) {

        int targetPosition;
        LinearLayoutManager layoutManager = ((LinearLayoutManager) getLayoutManager());

        if (!isEndOfList) {
            int startPosition = layoutManager.findFirstVisibleItemPosition();
            int endPosition = layoutManager.findLastVisibleItemPosition();

            // something is wrong. return.
            if (startPosition < 0 || endPosition < 0) {
                return;
            }

            if (endPosition - startPosition > 1) {
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
            ALog.i(TAG, "target position same as play position, returning");
//            mediaController.show(3000);
//            mediaController.setEnabled(true);
            return;
        }

        resetPlayingFeedItem();

        // set the position of the list-item that is to be played
        playPosition = targetPosition;

        int currentViewGroupPosition = targetPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        ALog.i(TAG, "playFeedItem: current view group position: " + currentViewGroupPosition);
        View child = getChildAt(currentViewGroupPosition);
        if (child == null) {
            ALog.i(TAG, "playFeedItem: no child at current view group position: " + currentViewGroupPosition);
            return;
        }

        boolean isFeedItemType = isFeedItemType(targetPosition);
        if (isFeedItemType) {
            FeedItemViewHolder holder = (FeedItemViewHolder) child.getTag();
            if (holder == null) {
                playPosition = -1;
                return;
            }

            FeedItemModel.FeedItemType feedItemType = holder.getType();
            switch (feedItemType) {
                case VIDEO:
                    ALog.i(TAG, "trying to play video at position:" + playPosition);
                    playVideo(holder);
                    break;
                case MUSIC:
                    ALog.i(TAG, "trying to play music at position:" + playPosition);
                    playMusic(holder);
                    break;
                default:
                    return;
            }
        }
    }

    private void playMusic(FeedItemViewHolder holder) {
        musicViewController.playMusic(holder.getMusicPlayPauseBtn(), holder.getMusicSeekBar(),
                holder.getMusicLengthTotalTime(), holder.getMusicLengthProgressTime(),
                holder.getMusicUrl());
    }

    private void playVideo(FeedItemViewHolder holder) {
        ALog.i(TAG, "play video called:" + holder.getVideoUrl());
        viewHolderParent = holder.itemView;
        videoViewController.playVideo(holder.getThumbnail(), holder.getProgressBar(),
                holder.getVideoView(), holder.getVideoUrl());
    }

    public void onStart() {
        if (musicPlayIntent == null) {
            musicPlayIntent = new Intent(context, MediaPlayerService.class);
            context.bindService(musicPlayIntent, musicConnection, Context.BIND_AUTO_CREATE);
            context.startService(musicPlayIntent);
            ALog.i(TAG, "music service initialized");
        }
    }

    public void onPause() {
        resetPlayingFeedItem();
    }

    public void onDestroy() {
        musicSrv.unregisterCallback(mediaCallback);
        context.unbindService(musicConnection);
        musicPlayIntent = null;
    }

    private MediaPlayerService.MediaCallbackListener mediaCallback = new MediaPlayerService.MediaCallbackListener() {
        @Override
        public void onMediaPlayStart(MediaPlayer mp) {
            mp.setLooping(true);
        }

        @Override
        public void onTrackCompletion(MediaPlayer mp) {

        }

        @Override
        public void onError() {
            musicViewController.resetMusicFeed();
        }
    };

    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.MusicBinder binder = (MediaPlayerService.MusicBinder) service;
            musicSrv = binder.getService();
            musicBound = true;
            musicSrv.registerCallback(mediaCallback);

            musicViewController.init(context, musicBound, musicSrv);

            ALog.i(TAG, "Service connection established");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };


    private boolean isFeedItemType(int targetPosition) {
        if (mediaObjects.get(targetPosition).getType() instanceof GodPageRVAdapter.GodPageViewTypes) {
            GenericPageCardItemModel<GodPageRVAdapter.GodPageViewTypes> cardItem = (GenericPageCardItemModel<GodPageRVAdapter.GodPageViewTypes>) mediaObjects.get(targetPosition);
            return cardItem.getType() == FEED_ITEM;
        }

        if (mediaObjects.get(targetPosition).getType() instanceof MandirPageRVAdapter.MandirPageViewTypes) {
            GenericPageCardItemModel<MandirPageRVAdapter.MandirPageViewTypes> cardItem = (GenericPageCardItemModel<MandirPageRVAdapter.MandirPageViewTypes>) mediaObjects.get(targetPosition);
            return cardItem.getType() == MandirPageRVAdapter.MandirPageViewTypes.FEED_ITEM;
        }

        if (mediaObjects.get(targetPosition).getType() instanceof PujariPageRVAdapter.PujariPageViewTypes) {
            GenericPageCardItemModel<PujariPageRVAdapter.PujariPageViewTypes> cardItem = (GenericPageCardItemModel<PujariPageRVAdapter.PujariPageViewTypes>) mediaObjects.get(targetPosition);
            return cardItem.getType() == PujariPageRVAdapter.PujariPageViewTypes.FEED_ITEM;
        }

        if (mediaObjects.get(targetPosition).getType() instanceof MyFeedRVAdapter.MyFeedViewTypes) {
            GenericPageCardItemModel<MyFeedRVAdapter.MyFeedViewTypes> cardItem = (GenericPageCardItemModel<MyFeedRVAdapter.MyFeedViewTypes>) mediaObjects.get(targetPosition);
            return cardItem.getType() == MyFeedRVAdapter.MyFeedViewTypes.FEED_ITEM;
        }
        return false;
    }

    private void scrollToNextPosition() {
        int nextPosition = (playPosition == mediaObjects.size() - 1) ? playPosition :
                playPosition + 1;
        getLayoutManager().scrollToPosition(nextPosition);
    }

    private void resetPlayingFeedItem() {
        ALog.i(TAG, "resetting video view, vv is null:" + (videoView == null));
        resetVideoFeed();
        resetMusicFeed();
        playPosition = -1;
    }

    private void resetMusicFeed() {
        musicViewController.resetMusicFeed();
    }

    private void resetVideoFeed() {
        videoViewController.resetVideoFeed();
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

        if (viewIsPartiallyHiddenTop(rect)) {
            // view is partially hidden behind the top edge
            percents = (height - rect.top) * 100 / height;
        } else if (viewIsPartiallyHiddenBottom(height, rect)) {
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

    public void setMediaObjects(List<? extends GenericPageCardItemModel<?>> mediaObjects) {
        this.mediaObjects = mediaObjects;
    }

    public void setMediaController(MediaController mediaController) {
        this.mediaController = mediaController;
        videoViewController.init(this.mediaController);
    }
}
