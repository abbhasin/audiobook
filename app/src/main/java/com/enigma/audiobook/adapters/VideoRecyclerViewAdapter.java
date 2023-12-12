package com.enigma.audiobook.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.enigma.audiobook.R;
import com.enigma.audiobook.models.VideoMediaObject;

import java.util.ArrayList;
import java.util.List;

public class VideoRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    RequestManager requestManager;
    List<VideoMediaObject> mediaObjects;


    public VideoRecyclerViewAdapter(List<VideoMediaObject> mediaObjects, RequestManager requestManager) {
        this.mediaObjects = mediaObjects;
        this.requestManager = requestManager;
    }
    
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new VideoPlayerViewHolder(
                LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_video, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        ((VideoPlayerViewHolder)viewHolder).onBind(mediaObjects.get(i), requestManager);
    }

    @Override
    public int getItemCount() {
        return mediaObjects.size();
    }


    public static class VideoPlayerViewHolder extends RecyclerView.ViewHolder {
        FrameLayout media_container;
        TextView title, description;
        ImageView thumbnail, volumeControl, headingImage;
        ProgressBar progressBar;
        VideoView videoView;
        View parent;

        public VideoPlayerViewHolder(View itemView) {
            super(itemView);
            parent = itemView;
            this.media_container = itemView.findViewById(R.id.media_container);
            this.videoView = itemView.findViewById(R.id.card_video_view);
            this.thumbnail = itemView.findViewById(R.id.card_video_thumbnail);
            this.title = itemView.findViewById(R.id.card_video_heading_title);
            this.description = itemView.findViewById(R.id.card_video_description);
            this.progressBar = itemView.findViewById(R.id.card_video_progress_bar);
            this.volumeControl = itemView.findViewById(R.id.card_video_volume_control);
            this.headingImage = itemView.findViewById(R.id.card_video_heading_image);
        }

        public void onBind(VideoMediaObject videoMediaObject, RequestManager requestManager) {
            parent.setTag(this);
            this.title.setText(videoMediaObject.getTitle());
            this.description.setText(videoMediaObject.getDescription());
            requestManager
                    .load(videoMediaObject.getThumbnail())
                    .into(thumbnail);
            requestManager
                    .load(videoMediaObject.getThumbnail())
                    .into(headingImage);
        }

        public FrameLayout getMedia_container() {
            return media_container;
        }

        public TextView getTitle() {
            return title;
        }

        public TextView getDescription() {
            return description;
        }

        public ImageView getThumbnail() {
            return thumbnail;
        }

        public ImageView getVolumeControl() {
            return volumeControl;
        }

        public ProgressBar getProgressBar() {
            return progressBar;
        }

        public VideoView getVideoView() {
            return videoView;
        }

        public View getParent() {
            return parent;
        }


    }
}
