package com.enigma.audiobook.activities;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.enigma.audiobook.R;
import com.enigma.audiobook.adapters.SwipeVideoCardAdapter;
import com.enigma.audiobook.backend.models.Darshan;
import com.enigma.audiobook.backend.models.User;
import com.enigma.audiobook.models.SwipeVideoMediaModel;
import com.enigma.audiobook.pageTransformers.ZoomOutPageTransformer;
import com.enigma.audiobook.proxies.DarshanService;
import com.enigma.audiobook.proxies.RetrofitFactory;
import com.enigma.audiobook.proxies.UserFeaturesService;
import com.enigma.audiobook.proxies.adapters.ModelAdapters;
import com.enigma.audiobook.utils.ALog;
import com.enigma.audiobook.utils.NavigationUtils;
import com.enigma.audiobook.utils.RetryHelper;
import com.enigma.audiobook.utils.SharedPreferencesHandler;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DarshanActivity extends FragmentActivity {

    private static String TAG = "DarshanActivity";
    private String userId;
    private ViewPager2 viewPager;
    private LinearLayout animateSwipeRightLL;
    private SwipeVideoCardAdapter pagerAdapter;
    private BottomNavigationView bottomNavigationView;
    private ProgressBar progressBar;
    private DarshanService darshanService;
    private UserFeaturesService userFeaturesService;

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
        setupNavigation();
        userId = SharedPreferencesHandler.getUserId(this).get();

        animateSwipeRightLL = findViewById(R.id.swipeVideoCardAnimateSwipeRightLL);
        animationHandler = new Handler();
        ctr = 0;

        progressBar = findViewById(R.id.swipeVideoCardProgressBar);
        viewPager = findViewById(R.id.swipeVideoCardViewPager);
        pagerAdapter = new SwipeVideoCardAdapter(this);

        User user = new User();
        user.setUserId(userId);

        darshanService = RetrofitFactory.getInstance().createService(DarshanService.class);
        userFeaturesService = RetrofitFactory.getInstance().createService(UserFeaturesService.class);

        Call<Boolean> swipeEnabledCall = userFeaturesService.isSwipeDarshanPugAnimationEnabled(user.getUserId());
        RetryHelper.enqueueWithRetry(swipeEnabledCall, 2, new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                boolean isEnabled = response.body();
                hasSwipedRightAtLeastOnce = !isEnabled;
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {

            }
        });

        Call<List<Darshan>> darshansCallable = darshanService.getDarshans();
        RetryHelper.enqueueWithRetry(darshansCallable,
                new Callback<List<Darshan>>() {
            @Override
            public void onResponse(Call<List<Darshan>> call, Response<List<Darshan>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(DarshanActivity.this,
                            "Unable to fetch details. Please check internet connection & try again later!",
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                List<Darshan> darshans = response.body();
                progressBar.setVisibility(View.GONE);
                pagerAdapter.setOrPaginate(ModelAdapters.convert(darshans));
                viewPager.setPageTransformer(new ZoomOutPageTransformer());
                viewPager.setAdapter(pagerAdapter);
            }

            @Override
            public void onFailure(Call<List<Darshan>> call, Throwable t) {
                ALog.e(TAG, "unable to load darshans", t);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DarshanActivity.this, "failed to load darshans",
                        Toast.LENGTH_SHORT).show();
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if (position > 0 && !hasSwipedRightAtLeastOnce) {
                    hasSwipedRightAtLeastOnce = true;
                    Call<Void> swipedDarshanCall = userFeaturesService.swipedDarshan(user);
                    RetryHelper.enqueueWithRetry(swipedDarshanCall, new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {

                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                ALog.i(TAG, "current postion of page selected:" + position);
                if (pagerAdapter.getVideosSize() > 0 && position == pagerAdapter.getVideosSize() - 1) {
                    // no pagination required for darshans as of now
//                    if (ctr == 0) {
//                        List<SwipeVideoMediaModel> videos = getMoreVideos();
//                        pagerAdapter.setOrPaginate(videos);
//                        Toast.makeText(DarshanActivity.this,
//                                "More Darshan Videos added. Please swipe right to see more.", Toast.LENGTH_SHORT).show();
//                        ctr++;
//                    } else {
//                        Toast.makeText(DarshanActivity.this,
//                                "You have visited all Darshan videos for today! Thank You! :)", Toast.LENGTH_SHORT).show();
//                    }
                }
                if (position == 0) {
                    checkForAnimation();
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        NavigationUtils.setMenuItemChecked(bottomNavigationView, R.id.menuItemDarshans);
    }

    private void setupNavigation() {
        bottomNavigationView = NavigationUtils.setupNavigationDrawer(
                this,
                R.id.darshansBottomNavigation,
                R.id.menuItemDarshans);
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
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(animateSwipeRightLL, "translationY", 0, 200f, 0f);
            objectAnimator.setInterpolator(new AccelerateInterpolator());
            objectAnimator.setDuration(600);
            objectAnimator.setRepeatMode(ObjectAnimator.RESTART);
            objectAnimator.setRepeatCount(4);
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
}