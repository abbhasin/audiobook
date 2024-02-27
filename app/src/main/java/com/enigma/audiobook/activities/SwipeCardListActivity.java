package com.enigma.audiobook.activities;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.enigma.audiobook.utils.Utils.addTryCatch;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.RequestManager;
import com.enigma.audiobook.R;
import com.enigma.audiobook.adapters.SwipeCardVideoViewAdapter;
import com.enigma.audiobook.models.SwipeVideoMediaModel;
import com.enigma.audiobook.utils.ALog;
import com.enigma.audiobook.utils.Utils;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeableMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SwipeCardListActivity extends AppCompatActivity implements CardStackListener {
    private static final String TAG = "SwipeCardListActivity";

    CardStackLayoutManager manager;
    CardStackView cardStackView;
    SwipeCardVideoViewAdapter adapter;
    volatile boolean isRewind = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe_card_list);
        cardStackView = findViewById(R.id.cardStackView);
        manager = new CardStackLayoutManager(this, this);

        List<SwipeVideoMediaModel> mediaObjects = getVideos();
        adapter = new SwipeCardVideoViewAdapter(initGlide(), mediaObjects);
        manager.setStackFrom(StackFrom.None);
        manager.setVisibleCount(3);
        manager.setTranslationInterval(8.0f);
        manager.setScaleInterval(0.95f);
        manager.setSwipeThreshold(0.3f);
        manager.setMaxDegree(20.0f);
        manager.setDirections(Direction.VERTICAL);
        manager.setCanScrollHorizontal(false);
        manager.setCanScrollVertical(true);
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual);
        manager.setOverlayInterpolator(new LinearInterpolator());
        cardStackView.setLayoutManager(manager);

        cardStackView.setAdapter(adapter);

//        initialize(manager, cardStackView, true);
    }

    private List<SwipeVideoMediaModel> getVideos() {
        SwipeVideoMediaModel[] MEDIA_OBJECTS = {
                new SwipeVideoMediaModel("Sending Data to a New Activity with Intent Extras",
                        "Description for media object #1",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "", "", "darshanId"),
                new SwipeVideoMediaModel("REST API, Retrofit2, MVVM Course SUMMARY",
                        "Description for media object #2",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/REST+API+Retrofit+MVVM+Course+Summary.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/REST+API%2C+Retrofit2%2C+MVVM+Course+SUMMARY.png", "", "", "darshanId"),
                new SwipeVideoMediaModel("MVVM and LiveData",
                        "Description for media object #3",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/MVVM+and+LiveData+for+youtube.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/mvvm+and+livedata.png",
                        "", "", "darshanId"),
                new SwipeVideoMediaModel("Swiping Views with a ViewPager",
                        "Description for media object #4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/SwipingViewPager+Tutorial.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Swiping+Views+with+a+ViewPager.png",
                        "", "", "darshanId"),
                new SwipeVideoMediaModel("Dancing Woman Video",
                        "Test for Potrait Mode Videos",
                        "https://assets.mixkit.co/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-large.mp4",
                        "https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg",
                        "", "", "darshanId"),
                new SwipeVideoMediaModel("Going Bowling",
                        "Bowling video Potrait Mode",
                        "https://assets.mixkit.co/videos/preview/mixkit-young-man-at-the-bowling-center-makes-a-shot-49114-large.mp4",
                        "https://mixkit.imgix.net/videos/preview/mixkit-young-man-at-the-bowling-center-makes-a-shot-49114-0.jpg",
                        "", "", "darshanId"),
                new SwipeVideoMediaModel("Database Cache, MVVM, Retrofit, REST API demo for upcoming course",
                        "Description for media object #5",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Rest+api+teaser+video.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Rest+API+Integration+with+MVVM.png",
                        "", "", "darshanId")
        };
        return new ArrayList<>(Arrays.asList(MEDIA_OBJECTS));
    }

    private RequestManager initGlide() {
        return Utils.initGlide(this);
    }

    private void initialize(CardStackLayoutManager manager, CardStackView cardStackView, boolean resetAdapter) {
        manager.setStackFrom(StackFrom.None);
        manager.setVisibleCount(3);
        manager.setTranslationInterval(8.0f);
        manager.setScaleInterval(0.95f);
        manager.setSwipeThreshold(0.3f);
        manager.setMaxDegree(20.0f);
        manager.setDirections(Direction.VERTICAL);
        manager.setCanScrollHorizontal(false);
        manager.setCanScrollVertical(true);
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual);
        manager.setOverlayInterpolator(new LinearInterpolator());
        cardStackView.setLayoutManager(manager);
        if (resetAdapter) {
            cardStackView.setAdapter(adapter);
        }

    }

    @Override
    public void onCardDragging(Direction direction, float ratio) {

    }

    @Override
    public void onCardSwiped(Direction direction) {
        Toast.makeText(this, "card swiped in direction:" + direction, Toast.LENGTH_SHORT).show();
        if (direction == Direction.Bottom) {
            ALog.i(TAG, "rewinding on card swiped to bottom");
            // manager = new CardStackLayoutManager(this, this);
            // initialize(manager, cardStackView, false);
            cardStackView.rewind();
        }

    }

    @Override
    public void onCardRewound() {

    }

    @Override
    public void onCardCanceled() {

    }

    @Override
    public void onCardAppeared(View view, int position) {
        SwipeCardVideoViewAdapter.SwipeCardVideoHolder holder = (SwipeCardVideoViewAdapter.SwipeCardVideoHolder) view.getTag();
        ALog.i(TAG, "card appeared at position:" + position);
//        if(isRewind) {
//            isRewind = false;
//            cardStackView.rewind();
//            return;
//        }
        holder.getVideoView().setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                ALog.i(TAG, "swipe video view started playing at position:" + position);
                addTryCatch(() -> {
                    holder.getThumbnail().setVisibility(GONE);
                    if (holder.getProgressBar() != null) {
                        holder.getProgressBar().setVisibility(GONE);
                    }
                    mp.setLooping(true);
                    holder.getVideoView().start();
                }, TAG);
            }
        });
        holder.getProgressBar().setVisibility(VISIBLE);

        holder.getVideoView().setVisibility(VISIBLE);
        holder.getVideoView().setVideoURI(Uri.parse(holder.getVideoUrl()));
    }

    @Override
    public void onCardDisappeared(View view, int position) {
        resetVideoView(view, position);
    }

    private void resetVideoView(View view, int position) {
        SwipeCardVideoViewAdapter.SwipeCardVideoHolder holder = (SwipeCardVideoViewAdapter.SwipeCardVideoHolder) view.getTag();
        ALog.i(TAG, "resetting video view at position:" + position);
        if (holder.getVideoView() != null) {
            addTryCatch(() -> {
                holder.getVideoView().stopPlayback();
                holder.getVideoView().setVisibility(GONE);
                holder.getProgressBar().setVisibility(GONE);
                holder.getThumbnail().setVisibility(VISIBLE);
            }, TAG);
        }
    }

}