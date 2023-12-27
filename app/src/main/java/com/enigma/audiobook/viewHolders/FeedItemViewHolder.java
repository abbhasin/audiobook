package com.enigma.audiobook.viewHolders;

import static com.enigma.audiobook.models.FeedItemModel.FeedItemType.MUSIC;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.enigma.audiobook.R;
import com.enigma.audiobook.models.FeedItemModel;

public class FeedItemViewHolder extends RecyclerView.ViewHolder {

    TextView fromText, description;
    ImageView thumbnail, fromImage;
    VideoView videoView;
    ProgressBar progressBar;
    String videoUrl;
    RecyclerView imagesRV;

    LinearLayout musicLinearLayout;
    Button musicPlayPauseBtn;
    String musicUrl;
    SeekBar musicSeekBar;
    TextView musicLengthTotalTime, musicLengthProgressTime;

    View parent;
    FeedItemModel.FeedItemType feedItemType;


    public FeedItemViewHolder(@NonNull View itemView) {
        super(itemView);
        this.parent = itemView;

        this.fromText = itemView.findViewById(R.id.cardFeedItemFromText);
        this.description = itemView.findViewById(R.id.cardFeedItemDescriptionTxt);
        this.fromImage = itemView.findViewById(R.id.cardFeedItemFromImage);

        this.thumbnail = itemView.findViewById(R.id.cardFeedItemContentThumbnail);
        this.videoView = itemView.findViewById(R.id.cardFeedItemContentVideo);
        this.progressBar = itemView.findViewById(R.id.cardFeedItemContentVideoProgressBar);

        this.imagesRV = itemView.findViewById(R.id.cardFeedItemImagesChildRecyclerView);

        this.musicLinearLayout = itemView.findViewById(R.id.cardFeedItemMusicLL);
        this.musicPlayPauseBtn = itemView.findViewById(R.id.cardFeedItemMusicPlayPauseBtn);
        this.musicSeekBar = itemView.findViewById(R.id.cardFeedItemMusicSeekBar);
        this.musicLengthProgressTime = itemView.findViewById(R.id.cardFeedItemMusicLengthProgress);
        this.musicLengthTotalTime = itemView.findViewById(R.id.cardFeedItemMusicLengthTotalTime);

    }

    public void onBind(FeedItemModel feedItemModel, RequestManager requestManager) {
        parent.setTag(this);
        fromText.setText(feedItemModel.getFrom());
        requestManager
                .load(feedItemModel.getFromImgUrl())
                .into(fromImage);
        if (feedItemModel.getDescription() != null) {
            description.setText(feedItemModel.getDescription());
        } else {
            description.setText("");
        }

        feedItemType = feedItemModel.getType();

        switch (feedItemType) {
            case VIDEO:
                thumbnail.setVisibility(View.VISIBLE);
                videoUrl = feedItemModel.getVideoUrl();
                break;
            case MUSIC:
                musicUrl = feedItemModel.getMusicUrl();
                musicLinearLayout.setVisibility(View.VISIBLE);
                break;
            case IMAGES:
                imagesRV = itemView.findViewById(R.id.cardFeedItemImagesChildRecyclerView);
                imagesRV.setVisibility(View.VISIBLE);
                break;
            case TEXT_ONLY:
            default:
                throw new IllegalStateException("feed item type not found:" + feedItemModel.getType());
        }
    }

    public boolean isPlayable() {
        return feedItemType == FeedItemModel.FeedItemType.VIDEO || feedItemType == MUSIC;
    }

    public FeedItemModel.FeedItemType getType() {
        return feedItemType;
    }

    public TextView getFromText() {
        return fromText;
    }

    public TextView getDescription() {
        return description;
    }

    public ImageView getThumbnail() {
        return thumbnail;
    }

    public ImageView getFromImage() {
        return fromImage;
    }

    public VideoView getVideoView() {
        return videoView;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public RecyclerView getImagesRV() {
        return imagesRV;
    }

    public LinearLayout getMusicLinearLayout() {
        return musicLinearLayout;
    }

    public Button getMusicPlayPauseBtn() {
        return musicPlayPauseBtn;
    }

    public String getMusicUrl() {
        return musicUrl;
    }

    public SeekBar getMusicSeekBar() {
        return musicSeekBar;
    }

    public TextView getMusicLengthTotalTime() {
        return musicLengthTotalTime;
    }

    public TextView getMusicLengthProgressTime() {
        return musicLengthProgressTime;
    }

    public View getParent() {
        return parent;
    }
}
