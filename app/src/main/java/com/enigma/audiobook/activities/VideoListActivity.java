package com.enigma.audiobook.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.MediaController;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.enigma.audiobook.R;
import com.enigma.audiobook.adapters.VideoRecyclerViewAdapter;
import com.enigma.audiobook.models.VideoMediaModel;
import com.enigma.audiobook.recyclers.VideoPlayerRecyclerView;
import com.enigma.audiobook.utils.Utils;

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

        List<VideoMediaModel> mediaObjects = getVideos();
        mRecyclerView.setMediaObjects(mediaObjects);
        mRecyclerView.setMediaController(mediaController);
        VideoRecyclerViewAdapter adapter = new VideoRecyclerViewAdapter(mediaObjects, initGlide());
        mRecyclerView.setAdapter(adapter);
    }

    private static List<VideoMediaModel> getVideos() {
        return new ArrayList<>();
    }

    private RequestManager initGlide() {
        return Utils.initGlide(this);
    }
}