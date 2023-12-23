package com.enigma.audiobook.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.enigma.audiobook.R;
import com.enigma.audiobook.adapters.MenuItemCardViewAdapter;
import com.enigma.audiobook.models.MenuItemObject;
import com.enigma.audiobook.models.VideoMediaObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenuActivity extends AppCompatActivity {
    private RecyclerView menuRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        menuRecyclerView = findViewById(R.id.menuRecyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        menuRecyclerView.setLayoutManager(layoutManager);

        List<MenuItemObject> menuItems = getMenuItems();
        MenuItemCardViewAdapter adapter = new MenuItemCardViewAdapter(initGlide(), menuItems, this);
        menuRecyclerView.setAdapter(adapter);
    }

    private List<MenuItemObject> getMenuItems() {
        MenuItemObject[] MEDIA_OBJECTS = {
                new MenuItemObject("Darshan",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        MenuItemObject.ActivityType.DARSHAN
                ),
                new MenuItemObject("Video Library",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/REST+API%2C+Retrofit2%2C+MVVM+Course+SUMMARY.png",
                        MenuItemObject.ActivityType.VIDEO_LIST
                ),
                new MenuItemObject("Music Library",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/mvvm+and+livedata.png",
                        MenuItemObject.ActivityType.MUSIC_LIST
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