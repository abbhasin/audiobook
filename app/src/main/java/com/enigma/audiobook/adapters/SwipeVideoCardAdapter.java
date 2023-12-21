package com.enigma.audiobook.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.enigma.audiobook.fragments.SwipeVideoCardFragment;
import com.enigma.audiobook.models.SwipeVideoMediaObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SwipeVideoCardAdapter extends FragmentStateAdapter {
    private List<SwipeVideoMediaObject> swipeVideoMediaObjects;

    public SwipeVideoCardAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        SwipeVideoMediaObject mediaObj = this.swipeVideoMediaObjects.get(position);
        return SwipeVideoCardFragment.newInstance(mediaObj.getTitle(), mediaObj.getDescription(),
                mediaObj.getThumbnail(), mediaObj.getVideoUrl());
    }

    @Override
    public int getItemCount() {
        return swipeVideoMediaObjects.size();
    }

    public void setOrPaginate(List<SwipeVideoMediaObject> swipeVideoMediaObjects) {
        if(this.swipeVideoMediaObjects == null || swipeVideoMediaObjects.isEmpty()) {
            this.swipeVideoMediaObjects = swipeVideoMediaObjects;
        } else {
            List<SwipeVideoMediaObject> newList = new ArrayList<>();
            newList.addAll(this.swipeVideoMediaObjects);
            newList.addAll(swipeVideoMediaObjects);
            DiffUtil.Callback callback = new SwipeVideoCardDiffCallback(this.swipeVideoMediaObjects, newList);
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
            this.swipeVideoMediaObjects = newList;
            result.dispatchUpdatesTo(this);
        }
    }

    public int getVideosSize() {
        return this.swipeVideoMediaObjects.size();
    }

    public static class SwipeVideoCardDiffCallback extends DiffUtil.Callback {
        private List<SwipeVideoMediaObject> oldList;
        private List<SwipeVideoMediaObject> newList;

        public SwipeVideoCardDiffCallback(List<SwipeVideoMediaObject> oldList, List<SwipeVideoMediaObject> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getVideoUrl().equals(newList.get(newItemPosition).getVideoUrl());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }
    }
}
