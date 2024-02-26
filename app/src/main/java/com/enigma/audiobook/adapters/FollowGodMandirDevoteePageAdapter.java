package com.enigma.audiobook.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.enigma.audiobook.fragments.FollowGodMandirDevoteePageDevoteeFragment;
import com.enigma.audiobook.fragments.FollowGodMandirDevoteePageGodFragment;
import com.enigma.audiobook.fragments.FollowGodMandirDevoteePageMandirFragment;
import com.enigma.audiobook.models.FollowGodMandirDevoteeFragmentsModel;
import com.enigma.audiobook.utils.ALog;

import java.util.List;

public class FollowGodMandirDevoteePageAdapter extends FragmentStateAdapter {
    private String TAG = "FollowGodMandirDevoteePageAdapter";
    private List<FollowGodMandirDevoteeFragmentsModel> followGodMandirDevoteeFragmentsModels;

    public FollowGodMandirDevoteePageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        FollowGodMandirDevoteeFragmentsModel mediaObj = this.followGodMandirDevoteeFragmentsModels.get(position);
        ALog.i(TAG, String.format("creating fragment at position:%d of type:%s", position, mediaObj.getType()));
        switch (mediaObj.getType()) {
            case GOD:
                return FollowGodMandirDevoteePageGodFragment.newInstance(mediaObj.isOnlyFollowed());
            case MANDIR:
                return FollowGodMandirDevoteePageMandirFragment.newInstance(mediaObj.isOnlyFollowed());
            case DEVOTEE:
                return FollowGodMandirDevoteePageDevoteeFragment.newInstance(mediaObj.isOnlyFollowed());
            default:
                throw new IllegalStateException("unhandled fragment type:" + mediaObj.getType());
        }
    }
    @Override
    public int getItemCount() {
        return followGodMandirDevoteeFragmentsModels.size();
    }

    @Override
    public int getItemViewType(int position) {
        FollowGodMandirDevoteeFragmentsModel mediaObj = this.followGodMandirDevoteeFragmentsModels.get(position);
        return mediaObj.getType().ordinal();
    }

    public void setOrPaginate(List<FollowGodMandirDevoteeFragmentsModel> followGodMandirDevoteeFragmentsModels) {
        if (this.followGodMandirDevoteeFragmentsModels == null || followGodMandirDevoteeFragmentsModels.isEmpty()) {
            this.followGodMandirDevoteeFragmentsModels = followGodMandirDevoteeFragmentsModels;
        } else {
            this.followGodMandirDevoteeFragmentsModels = followGodMandirDevoteeFragmentsModels;
            this.notifyDataSetChanged();
        }
    }
}
