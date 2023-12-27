package com.enigma.audiobook.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.enigma.audiobook.fragments.SwipeVideoCardFragment;
import com.enigma.audiobook.models.SwipeVideoMediaModel;

import java.util.ArrayList;
import java.util.List;

public class SwipeVideoCardAdapter extends FragmentStateAdapter {
    private List<SwipeVideoMediaModel> swipeVideoMediaModels;

    public SwipeVideoCardAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        SwipeVideoMediaModel mediaObj = this.swipeVideoMediaModels.get(position);
        return SwipeVideoCardFragment.newInstance(mediaObj.getTitle(), mediaObj.getDescription(),
                mediaObj.getThumbnail(), mediaObj.getVideoUrl());
    }

    @Override
    public int getItemCount() {
        return swipeVideoMediaModels.size();
    }

    public void setOrPaginate(List<SwipeVideoMediaModel> swipeVideoMediaModels) {
        if(this.swipeVideoMediaModels == null || swipeVideoMediaModels.isEmpty()) {
            this.swipeVideoMediaModels = swipeVideoMediaModels;
        } else {
            List<SwipeVideoMediaModel> newList = new ArrayList<>();
            newList.addAll(this.swipeVideoMediaModels);
            newList.addAll(swipeVideoMediaModels);
            DiffUtil.Callback callback = new SwipeVideoCardDiffCallback(this.swipeVideoMediaModels, newList);
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
            this.swipeVideoMediaModels = newList;
            result.dispatchUpdatesTo(this);
        }
    }

    public int getVideosSize() {
        return this.swipeVideoMediaModels.size();
    }

    public static class SwipeVideoCardDiffCallback extends DiffUtil.Callback {
        private List<SwipeVideoMediaModel> oldList;
        private List<SwipeVideoMediaModel> newList;

        public SwipeVideoCardDiffCallback(List<SwipeVideoMediaModel> oldList, List<SwipeVideoMediaModel> newList) {
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
