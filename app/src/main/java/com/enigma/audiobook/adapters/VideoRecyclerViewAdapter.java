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
import com.enigma.audiobook.models.VideoMediaModel;

import java.util.List;

public class VideoRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    RequestManager requestManager;
    List<VideoMediaModel> mediaObjects;


    public VideoRecyclerViewAdapter(List<VideoMediaModel> mediaObjects, RequestManager requestManager) {
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
        ((VideoPlayerViewHolder) viewHolder).onBind(mediaObjects.get(i), requestManager);
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
        //        FixedVideoView videoView;
        VideoView videoView;
        View parent;

        public VideoPlayerViewHolder(View itemView) {
            super(itemView);
            parent = itemView;
            this.media_container = itemView.findViewById(R.id.videoMediaContainer);
            this.videoView = itemView.findViewById(R.id.cardVideoView);
//            this.videoView.setVideoSize(0, 200);
            this.thumbnail = itemView.findViewById(R.id.cardVideoThumbnail);
            this.title = itemView.findViewById(R.id.cardVideoHeadingTitle);
//            title.setTextColor(Color.BLACK);
            this.description = itemView.findViewById(R.id.cardVideoDescription);
            this.progressBar = itemView.findViewById(R.id.cardVideoProgressBar);
//            this.volumeControl = itemView.findViewById(R.id.card_video_volume_control);
            this.headingImage = itemView.findViewById(R.id.cardVideoHeadingImage);
        }

        public void onBind(VideoMediaModel videoMediaModel, RequestManager requestManager) {
            parent.setTag(this);
            this.title.setText(videoMediaModel.getTitle());
            this.description.setText(videoMediaModel.getDescription());
            requestManager
                    .load(videoMediaModel.getThumbnail())
                    .into(thumbnail);
            requestManager
                    .load(videoMediaModel.getThumbnail())
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
