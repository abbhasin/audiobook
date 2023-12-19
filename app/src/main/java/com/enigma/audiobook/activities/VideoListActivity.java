package com.enigma.audiobook.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.MediaController;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.enigma.audiobook.R;
import com.enigma.audiobook.adapters.VideoRecyclerViewAdapter;
import com.enigma.audiobook.models.VideoMediaObject;
import com.enigma.audiobook.recyclers.VideoPlayerRecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VideoListActivity extends AppCompatActivity {
    VideoPlayerRecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        mRecyclerView = findViewById(R.id.video_player_recycler_view);

        initRecyclerView();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void initRecyclerView() {
        MediaController mediaController = new MediaController(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        List<VideoMediaObject> mediaObjects = getVideos();
        mRecyclerView.setMediaObjects(mediaObjects);
        mRecyclerView.setMediaController(mediaController);
        VideoRecyclerViewAdapter adapter = new VideoRecyclerViewAdapter(mediaObjects, initGlide());
        mRecyclerView.setAdapter(adapter);
    }

    private static List<VideoMediaObject> getVideos() {
        VideoMediaObject[] MEDIA_OBJECTS = {
                new VideoMediaObject("Sending Data to a New Activity with Intent Extras",
                        "Description for media object #1",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png"
                ),
                new VideoMediaObject("REST API, Retrofit2, MVVM Course SUMMARY",
                        "Description for media object #2",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/REST+API+Retrofit+MVVM+Course+Summary.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/REST+API%2C+Retrofit2%2C+MVVM+Course+SUMMARY.png"),
                new VideoMediaObject("MVVM and LiveData",
                        "Description for media object #3",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/MVVM+and+LiveData+for+youtube.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/mvvm+and+livedata.png"
                ),
                new VideoMediaObject("Swiping Views with a ViewPager",
                        "Description for media object #4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/SwipingViewPager+Tutorial.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Swiping+Views+with+a+ViewPager.png"
                ),
                new VideoMediaObject("Dancing Woman Video",
                        "Test for Potrait Mode Videos",
                        "https://assets.mixkit.co/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-large.mp4",
                        "https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg"
                ),
                new VideoMediaObject("Going Bowling",
                        "Bowling video Potrait Mode",
                        "https://assets.mixkit.co/videos/preview/mixkit-young-man-at-the-bowling-center-makes-a-shot-49114-large.mp4",
                        "https://mixkit.imgix.net/videos/preview/mixkit-young-man-at-the-bowling-center-makes-a-shot-49114-0.jpg"
                ),
                new VideoMediaObject("Database Cache, MVVM, Retrofit, REST API demo for upcoming course",
                        "Description for media object #5",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Rest+api+teaser+video.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Rest+API+Integration+with+MVVM.png"
                )





        };
        return new ArrayList<>(Arrays.asList(MEDIA_OBJECTS));
    }

    private RequestManager initGlide() {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.background_card_view)
                .error(R.drawable.background_card_view);

        return Glide.with(this)
                .setDefaultRequestOptions(options);
    }
}