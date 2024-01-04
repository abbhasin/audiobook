package com.enigma.audiobook.activities;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.enigma.audiobook.R;
import com.enigma.audiobook.adapters.SwipeVideoCardAdapter;
import com.enigma.audiobook.models.SwipeVideoMediaModel;
import com.enigma.audiobook.pageTransformers.ZoomOutPageTransformer;
import com.enigma.audiobook.utils.ALog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DarshanActivity extends FragmentActivity {

    private static String TAG = "DarshanActivity";
    private ViewPager2 viewPager;
    private LinearLayout animateSwipeRightLL;
    private SwipeVideoCardAdapter pagerAdapter;

    private boolean hasSwipedRightAtLeastOnce = false;
    private boolean animationShown = false;
    Runnable timerOnCurrentPage;
    Handler animationHandler;
    int timeSecOnCurrentPage = 0;

    private static int ctr = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe_video_card);
        animateSwipeRightLL = findViewById(R.id.swipeVideoCardAnimateSwipeRightLL);
        ctr = 0;

        viewPager = findViewById(R.id.swipeVideoCardViewPager);
        pagerAdapter = new SwipeVideoCardAdapter(this);
        List<SwipeVideoMediaModel> videos = getVideos();
        pagerAdapter.setOrPaginate(videos);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());
        viewPager.setAdapter(pagerAdapter);

        animationHandler = new Handler();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if(position > 0 && !hasSwipedRightAtLeastOnce) {
                    hasSwipedRightAtLeastOnce = true;
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                ALog.i(TAG, "zzz current postion of page selected:" + position);
                if (pagerAdapter.getVideosSize() > 0 && position == pagerAdapter.getVideosSize() - 1) {
                    if (ctr == 0) {
                        List<SwipeVideoMediaModel> videos = getMoreVideos();
                        pagerAdapter.setOrPaginate(videos);
                        Toast.makeText(DarshanActivity.this,
                                "More Darshan Videos added. Please swipe right to see more.", Toast.LENGTH_SHORT).show();
                        ctr++;
                    } else {
                        Toast.makeText(DarshanActivity.this,
                                "You have visited all Darshan videos for today! Thank You! :)", Toast.LENGTH_SHORT).show();
                    }
                }
                if(position == 0) {
                    checkForAnimation();
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
    }

    private void checkForAnimation() {
        if (!hasSwipedRightAtLeastOnce) {
            timerOnCurrentPage = new Runnable() {
                @Override
                public void run() {
                    timeSecOnCurrentPage++;
                    if (timeSecOnCurrentPage >= 15) {
                        setupAnimation();
                        animationShown = true;
                        return;
                    }
                    animationHandler.postDelayed(timerOnCurrentPage, 1000);
                }
            };
            animationHandler.post(timerOnCurrentPage);
        }
    }

    private void setupAnimation() {
        if (!hasSwipedRightAtLeastOnce && !animationShown) {
            animateSwipeRightLL.setVisibility(View.VISIBLE);
            viewPager.setAlpha(0.3f);
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(animateSwipeRightLL, "translationX", 0, 150f, 0f);
            objectAnimator.setInterpolator(new AccelerateInterpolator());
            objectAnimator.setDuration(800);
            objectAnimator.setRepeatMode(ObjectAnimator.RESTART);
            objectAnimator.setRepeatCount(8);
            objectAnimator.start();
            objectAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(@NonNull Animator animation) {

                }

                @Override
                public void onAnimationEnd(@NonNull Animator animation) {
                    animateSwipeRightLL.setVisibility(View.GONE);
                    viewPager.setAlpha(1f);
                    animationShown = true;
                }

                @Override
                public void onAnimationCancel(@NonNull Animator animation) {

                }

                @Override
                public void onAnimationRepeat(@NonNull Animator animation) {

                }
            });
        }
    }

    private List<SwipeVideoMediaModel> getVideos() {
        SwipeVideoMediaModel[] MEDIA_OBJECTS = {
                new SwipeVideoMediaModel("Sending Data to a New Activity with Intent Extras",
                        "Description for media object #1",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png"
                ),
                new SwipeVideoMediaModel("REST API, Retrofit2, MVVM Course SUMMARY",
                        "Description for media object #2",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/REST+API+Retrofit+MVVM+Course+Summary.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/REST+API%2C+Retrofit2%2C+MVVM+Course+SUMMARY.png"),

                new SwipeVideoMediaModel("Dancing Woman Video",
                        "Test for Potrait Mode Videos",
                        "https://assets.mixkit.co/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-large.mp4",
                        "https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg"
                ),
                new SwipeVideoMediaModel("Going Bowling",
                        "Bowling video Potrait Mode",
                        "https://assets.mixkit.co/videos/preview/mixkit-young-man-at-the-bowling-center-makes-a-shot-49114-large.mp4",
                        "https://mixkit.imgix.net/videos/preview/mixkit-young-man-at-the-bowling-center-makes-a-shot-49114-0.jpg"
                )
        };
        return new ArrayList<>(Arrays.asList(MEDIA_OBJECTS));
    }

    private List<SwipeVideoMediaModel> getMoreVideos() {
        SwipeVideoMediaModel[] MEDIA_OBJECTS = {
                new SwipeVideoMediaModel("MVVM and LiveData",
                        "Description for media object #3",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/MVVM+and+LiveData+for+youtube.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/mvvm+and+livedata.png"
                ),
                new SwipeVideoMediaModel("Swiping Views with a ViewPager",
                        "Description for media object #4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/SwipingViewPager+Tutorial.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Swiping+Views+with+a+ViewPager.png"
                ),
                new SwipeVideoMediaModel("Database Cache, MVVM, Retrofit, REST API demo for upcoming course",
                        "Description for media object #5",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Rest+api+teaser+video.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Rest+API+Integration+with+MVVM.png"
                )
        };
        return new ArrayList<>(Arrays.asList(MEDIA_OBJECTS));
    }
}