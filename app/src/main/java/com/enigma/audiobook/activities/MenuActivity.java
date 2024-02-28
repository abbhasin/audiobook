package com.enigma.audiobook.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.enigma.audiobook.R;
import com.enigma.audiobook.adapters.MenuItemCardViewAdapter;
import com.enigma.audiobook.models.MenuItemModel;
import com.enigma.audiobook.utils.NavigationUtils;
import com.enigma.audiobook.utils.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenuActivity extends AppCompatActivity {
    private RecyclerView menuRecyclerView;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        setupNavigation();

        menuRecyclerView = findViewById(R.id.menuRecyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        menuRecyclerView.setLayoutManager(layoutManager);

        List<MenuItemModel> menuItems = getMenuItems();
        MenuItemCardViewAdapter adapter = new MenuItemCardViewAdapter(initGlide(), menuItems, this);
        menuRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        NavigationUtils.setMenuItemChecked(bottomNavigationView, R.id.menuItemMenu);
    }

    private void setupNavigation() {
        bottomNavigationView = NavigationUtils.setupNavigationDrawer(
                this,
                R.id.menuBottomNavigation,
                R.id.menuItemMenu);
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
                ),
                new MenuItemModel("Follow God etc Page",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/REST+API%2C+Retrofit2%2C+MVVM+Course+SUMMARY.png",
                        MenuItemModel.ActivityType.FOLLOW_GOD_MANDIR_DEVOTEE
                ),
                new MenuItemModel("Library Page",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/mvvm+and+livedata.png",
                        MenuItemModel.ActivityType.LIBRARY
                ),
                new MenuItemModel("Test Crash",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/mvvm+and+livedata.png",
                        MenuItemModel.ActivityType.TEST_CRASH
                ),
                new MenuItemModel("Sign In",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/mvvm+and+livedata.png",
                        MenuItemModel.ActivityType.SIGN_IN
                )
        };
        return new ArrayList<>(Arrays.asList(MEDIA_OBJECTS));
    }

    private RequestManager initGlide() {
        return Utils.initGlide(this);
    }
}