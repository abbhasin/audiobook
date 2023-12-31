package com.enigma.audiobook.activities;

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
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FollowGodMandirDevoteePageAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_mandir_and_devotees);

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
                new FollowGodMandirDevoteeFragmentsModel(FollowGodMandirDevoteeFragmentsModel.FragmentType.GOD
                ),
                new FollowGodMandirDevoteeFragmentsModel(FollowGodMandirDevoteeFragmentsModel.FragmentType.MANDIR
                ),
                new FollowGodMandirDevoteeFragmentsModel(FollowGodMandirDevoteeFragmentsModel.FragmentType.DEVOTEE
                )
        };
        return new ArrayList<>(Arrays.asList(MEDIA_OBJECTS));
    }
}