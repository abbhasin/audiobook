package com.enigma.audiobook.viewHolders;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.enigma.audiobook.models.FeedItemFooterModel;

public class FeedItemFooterViewHolder extends RecyclerView.ViewHolder {
    View parent;
    public FeedItemFooterViewHolder(@NonNull View itemView) {
        super(itemView);
        this.parent = itemView;

    }

    public void onBind(FeedItemFooterModel feedItemFooterModel, RequestManager requestManager) {
        parent.setTag(this);
    }
}
