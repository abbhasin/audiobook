package com.enigma.audiobook.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.enigma.audiobook.R;

import java.util.List;

public class ImagesChildRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    RequestManager requestManager;
    List<String> imagesUrls;

    public ImagesChildRVAdapter(RequestManager requestManager, List<String> imagesUrls) {
        this.requestManager = requestManager;
        this.imagesUrls = imagesUrls;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImagesChildViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.card_feed_item_child_card_image, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ImagesChildViewHolder) holder).onBind(imagesUrls.get(position), requestManager);
    }

    @Override
    public int getItemCount() {
        return imagesUrls.size();
    }

    public static class ImagesChildViewHolder extends RecyclerView.ViewHolder {
        ImageView image;

        public ImagesChildViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.cardFeedItemChildCardImagesImage);
        }

        public void onBind(String imageUrl, RequestManager requestManager) {
            requestManager
                    .load(imageUrl)
                    .into(image);
        }
    }
}
