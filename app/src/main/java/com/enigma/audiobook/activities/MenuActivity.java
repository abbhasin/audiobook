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
import com.enigma.audiobook.models.MenuItemModel;

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

        List<MenuItemModel> menuItems = getMenuItems();
        MenuItemCardViewAdapter adapter = new MenuItemCardViewAdapter(initGlide(), menuItems, this);
        menuRecyclerView.setAdapter(adapter);
    }

    private List<MenuItemModel> getMenuItems() {
        MenuItemModel[] MEDIA_OBJECTS = {
                new MenuItemModel("Darshan",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        MenuItemModel.ActivityType.DARSHAN
                ),
                new MenuItemModel("Video Library",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/REST+API%2C+Retrofit2%2C+MVVM+Course+SUMMARY.png",
                        MenuItemModel.ActivityType.VIDEO_LIST
                ),
                new MenuItemModel("Music Library",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/mvvm+and+livedata.png",
                        MenuItemModel.ActivityType.MUSIC_LIST
                ),
                new MenuItemModel("God Page",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/mvvm+and+livedata.png",
                        MenuItemModel.ActivityType.GOD_PAGE
                ),
                new MenuItemModel("Mandir Page",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/mvvm+and+livedata.png",
                        MenuItemModel.ActivityType.MANDIR_PAGE
                ),
                new MenuItemModel("Pujari Page",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/mvvm+and+livedata.png",
                        MenuItemModel.ActivityType.PUJARI_PAGE
                ),
                new MenuItemModel("My Feed",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/REST+API%2C+Retrofit2%2C+MVVM+Course+SUMMARY.png",
                        MenuItemModel.ActivityType.MY_FEED
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