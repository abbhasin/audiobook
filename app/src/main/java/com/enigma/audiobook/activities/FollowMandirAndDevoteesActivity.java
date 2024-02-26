package com.enigma.audiobook.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.enigma.audiobook.R;
import com.enigma.audiobook.adapters.FollowGodMandirDevoteePageAdapter;
import com.enigma.audiobook.models.FollowGodMandirDevoteeFragmentsModel;
import com.enigma.audiobook.pageTransformers.ZoomOutPageTransformer;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FollowMandirAndDevoteesActivity extends FragmentActivity {
    public static final String ONLY_FOLLOWED_KEY = "onlyFollowed";
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FollowGodMandirDevoteePageAdapter pagerAdapter;
    private boolean onlyFollowed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_mandir_and_devotees);

        Intent i = getIntent();
        onlyFollowed = i.getBooleanExtra(ONLY_FOLLOWED_KEY, false);

        tabLayout = findViewById(R.id.followGodMandirAndDevoteesVPTabLayout);
        viewPager = findViewById(R.id.followGodMandirAndDevoteesVP);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());
        pagerAdapter = new FollowGodMandirDevoteePageAdapter(this);
        List<FollowGodMandirDevoteeFragmentsModel> videos = getVPItems();
        pagerAdapter.setOrPaginate(videos);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(videos.get(position).getType().name())
        ).attach();
    }

    private List<FollowGodMandirDevoteeFragmentsModel> getVPItems() {
        FollowGodMandirDevoteeFragmentsModel[] MEDIA_OBJECTS = {
                new FollowGodMandirDevoteeFragmentsModel(FollowGodMandirDevoteeFragmentsModel.FragmentType.GOD,
                        onlyFollowed),
                new FollowGodMandirDevoteeFragmentsModel(FollowGodMandirDevoteeFragmentsModel.FragmentType.MANDIR,
                        onlyFollowed),
                new FollowGodMandirDevoteeFragmentsModel(FollowGodMandirDevoteeFragmentsModel.FragmentType.DEVOTEE,
                        onlyFollowed)
        };
        return new ArrayList<>(Arrays.asList(MEDIA_OBJECTS));
    }
}