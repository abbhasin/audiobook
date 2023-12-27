package com.enigma.audiobook.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.enigma.audiobook.R;
import com.enigma.audiobook.models.SwipeVideoMediaModel;
import com.enigma.audiobook.views.FixedVideoView;

import java.util.List;

public class SwipeCardVideoViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    RequestManager requestManager;
    List<SwipeVideoMediaModel> mediaObjects;

    public SwipeCardVideoViewAdapter(RequestManager requestManager, List<SwipeVideoMediaModel> mediaObjects) {
        this.requestManager = requestManager;
        this.mediaObjects = mediaObjects;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SwipeCardVideoHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.card_swipe_video, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((SwipeCardVideoHolder) holder).onBind(mediaObjects.get(position), requestManager);
    }

    @Override
    public int getItemCount() {
        return mediaObjects.size();
    }

    public static class SwipeCardVideoHolder extends RecyclerView.ViewHolder {
        private ImageView thumbnail;
        private FixedVideoView videoView;
        private TextView heading, description;
        private ProgressBar progressBar;
        private View parent;

        private String videoUrl;

        public SwipeCardVideoHolder(View view) {
            super(view);
            parent = view;
            thumbnail = view.findViewById(R.id.cardSwipeVideoThumbnail);
            videoView = view.findViewById(R.id.cardSwipeVideoView);
            progressBar = view.findViewById(R.id.cardSwipeVideoProgressBar);
            heading = view.findViewById(R.id.cardSwipeVideoHeading);
            description = view.findViewById(R.id.cardSwipeVideoDescription);
        }

        public void onBind(SwipeVideoMediaModel videoMediaObject, RequestManager requestManager) {
            this.parent.setTag(this);
            this.heading.setText(videoMediaObject.getTitle());
            this.description.setText(videoMediaObject.getDescription());
            this.videoUrl = videoMediaObject.getVideoUrl();
            requestManager
                    .load(videoMediaObject.getThumbnail())
                    .into(thumbnail);
        }

        public ImageView getThumbnail() {
            return thumbnail;
        }

        public FixedVideoView getVideoView() {
            return videoView;
        }

        public TextView getHeading() {
            return heading;
        }

        public TextView getDescription() {
            return description;
        }

        public ProgressBar getProgressBar() {
            return progressBar;
        }

        public View getParent() {
            return parent;
        }

        public String getVideoUrl() {
            return videoUrl;
        }
    }
}
