package com.enigma.audiobook.activities;


import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.enigma.audiobook.R;
import com.enigma.audiobook.adapters.SwipeVideoCardAdapter;
import com.enigma.audiobook.models.SwipeVideoMediaObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DarshanActivity extends FragmentActivity {

    private ViewPager2 viewPager;
    private SwipeVideoCardAdapter pagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe_video_card);

        viewPager = findViewById(R.id.swipeVideoCardViewPager);
        pagerAdapter = new SwipeVideoCardAdapter(this);
        pagerAdapter.setOrPaginate(getVideos());
        viewPager.setAdapter(pagerAdapter);
    }

    private List<SwipeVideoMediaObject> getVideos() {
        SwipeVideoMediaObject[] MEDIA_OBJECTS = {
                new SwipeVideoMediaObject("Sending Data to a New Activity with Intent Extras",
                        "Description for media object #1",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png"
                ),
                new SwipeVideoMediaObject("REST API, Retrofit2, MVVM Course SUMMARY",
                        "Description for media object #2",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/REST+API+Retrofit+MVVM+Course+Summary.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/REST+API%2C+Retrofit2%2C+MVVM+Course+SUMMARY.png"),
                new SwipeVideoMediaObject("MVVM and LiveData",
                        "Description for media object #3",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/MVVM+and+LiveData+for+youtube.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/mvvm+and+livedata.png"
                ),
                new SwipeVideoMediaObject("Swiping Views with a ViewPager",
                        "Description for media object #4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/SwipingViewPager+Tutorial.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Swiping+Views+with+a+ViewPager.png"
                ),
                new SwipeVideoMediaObject("Dancing Woman Video",
                        "Test for Potrait Mode Videos",
                        "https://assets.mixkit.co/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-large.mp4",
                        "https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg"
                ),
                new SwipeVideoMediaObject("Going Bowling",
                        "Bowling video Potrait Mode",
                        "https://assets.mixkit.co/videos/preview/mixkit-young-man-at-the-bowling-center-makes-a-shot-49114-large.mp4",
                        "https://mixkit.imgix.net/videos/preview/mixkit-young-man-at-the-bowling-center-makes-a-shot-49114-0.jpg"
                ),
                new SwipeVideoMediaObject("Database Cache, MVVM, Retrofit, REST API demo for upcoming course",
                        "Description for media object #5",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Rest+api+teaser+video.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Rest+API+Integration+with+MVVM.png"
                )
        };
        return new ArrayList<>(Arrays.asList(MEDIA_OBJECTS));
    }
}