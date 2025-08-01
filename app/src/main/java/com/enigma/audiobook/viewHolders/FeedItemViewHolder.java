package com.enigma.audiobook.viewHolders;

import static com.enigma.audiobook.activities.GodPageActivity.GOD_ID_KEY;
import static com.enigma.audiobook.activities.MandirPageActivity.MANDIR_ID_KEY;
import static com.enigma.audiobook.activities.PujariPageActivity.INFLUENCER_ID_KEY;
import static com.enigma.audiobook.models.FeedItemModel.FeedItemType.MUSIC;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.enigma.audiobook.R;
import com.enigma.audiobook.activities.GodPageActivity;
import com.enigma.audiobook.activities.MandirPageActivity;
import com.enigma.audiobook.activities.PujariPageActivity;
import com.enigma.audiobook.adapters.FeedImagesChildRVAdapter;
import com.enigma.audiobook.backend.models.ContentUploadStatus;
import com.enigma.audiobook.models.FeedItemModel;
import com.enigma.audiobook.pageTransformers.ScrollingPagerIndicator;
import com.enigma.audiobook.utils.Utils;

import java.util.List;

public class FeedItemViewHolder extends RecyclerView.ViewHolder {

    LinearLayout fromLL, descriptionLL;
    TextView fromText, description, title;
    ImageView thumbnail, fromImage;
    VideoView videoView;
    ProgressBar progressBar;
    String videoUrl;

    List<String> imagesUrl;
    RecyclerView imagesChildRV;
    ScrollingPagerIndicator indicator;

    LinearLayout musicLinearLayout;
    Button musicPlayPauseBtn;
    String musicUrl;
    SeekBar musicSeekBar;
    TextView musicLengthTotalTime, musicLengthProgressTime;

    View parent;
    FeedItemModel.FeedItemType feedItemType;

    RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();

    String id;
    ContentUploadStatus contentUploadStatus;


    public FeedItemViewHolder(@NonNull View itemView) {
        super(itemView);
        this.parent = itemView;

        this.fromLL = itemView.findViewById(R.id.cardFeedItemFromLL);
        this.fromText = itemView.findViewById(R.id.cardFeedItemFromText);
        this.fromImage = itemView.findViewById(R.id.cardFeedItemFromImage);

        this.title = itemView.findViewById(R.id.cardFeedItemTitleTxt);
        this.description = itemView.findViewById(R.id.cardFeedItemDescriptionTxt);
        this.descriptionLL = itemView.findViewById(R.id.cardFeedItemDescriptionLL);

        this.thumbnail = itemView.findViewById(R.id.cardFeedItemContentThumbnail);
        this.videoView = itemView.findViewById(R.id.cardFeedItemContentVideo);
        this.progressBar = itemView.findViewById(R.id.cardFeedItemContentVideoProgressBar);

        this.imagesChildRV = itemView.findViewById(R.id.cardFeedItemImagesChildRecyclerView);
        this.indicator = itemView.findViewById(R.id.cardFeedItemImagesChildRecyclerViewScrollPageIndicator);

        this.musicLinearLayout = itemView.findViewById(R.id.cardFeedItemMusicLL);
        this.musicPlayPauseBtn = itemView.findViewById(R.id.cardFeedItemMusicPlayPauseBtn);
        this.musicSeekBar = itemView.findViewById(R.id.cardFeedItemMusicSeekBar);
        this.musicLengthProgressTime = itemView.findViewById(R.id.cardFeedItemMusicLengthProgress);
        this.musicLengthTotalTime = itemView.findViewById(R.id.cardFeedItemMusicLengthTotalTime);

    }

    public void onBind(FeedItemModel feedItemModel, RequestManager requestManager,
                       Context context) {
        parent.setTag(this);
        id = feedItemModel.getId();
        contentUploadStatus = feedItemModel.getContentUploadStatus();
        fromText.setText(feedItemModel.getFrom());
        requestManager
                .load(feedItemModel.getFromImgUrl())
                .into(fromImage);
        fromLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = null;
                switch (feedItemModel.getPostAssociationType()) {
                    case GOD:
                        i = new Intent(context, GodPageActivity.class);
                        i.putExtra(GOD_ID_KEY, feedItemModel.getGodId());
                        context.startActivity(i);
                        return;
                    case MANDIR:
                        i = new Intent(context, MandirPageActivity.class);
                        i.putExtra(MANDIR_ID_KEY, feedItemModel.getMandirId());
                        context.startActivity(i);
                        return;
                    case INFLUENCER:
                        i = new Intent(context, PujariPageActivity.class);
                        i.putExtra(INFLUENCER_ID_KEY, feedItemModel.getInfluencerID());
                        context.startActivity(i);
                        return;
                }

            }
        });


        title.setText(feedItemModel.getTitle());
        if (!Utils.isEmpty(feedItemModel.getDescription())) {
            descriptionLL.setVisibility(View.VISIBLE);
            description.setText(feedItemModel.getDescription());
        } else {
            descriptionLL.setVisibility(View.GONE);
            description.setText("");
        }

        feedItemType = feedItemModel.getType();

        switch (feedItemType) {
            case VIDEO:
                thumbnail.setVisibility(View.VISIBLE);
                videoUrl = feedItemModel.getVideoUrl();
                if (Utils.isEmpty(feedItemModel.getVideoThumbnailUrl())) {
                    requestManager
                            .asBitmap()
                            .timeout(10000)
                            .load(feedItemModel.getVideoUrl())
                            .error(feedItemModel.getFromImgUrl())
                            .into(thumbnail);
                } else {
                    requestManager
                            .load(feedItemModel.getVideoThumbnailUrl())
                            .into(thumbnail);
                }

                setImagesVisibility(View.GONE);
                setMusicVisibility(View.GONE);

                break;
            case MUSIC:
                musicUrl = feedItemModel.getMusicUrl();
                musicPlayPauseBtn.setClickable(false);
                musicSeekBar.setClickable(false);
                setMusicVisibility(View.VISIBLE);

                setImagesVisibility(View.GONE);
                setVideoVisibility(View.GONE);
                break;
            case IMAGES:
                imagesChildRV = itemView.findViewById(R.id.cardFeedItemImagesChildRecyclerView);
                imagesUrl = feedItemModel.getImagesUrls();
                setImagesVisibility(View.VISIBLE);
                setupImagesChildRV(requestManager);

                setVideoVisibility(View.GONE);
                setMusicVisibility(View.GONE);
                break;
            case TEXT_ONLY:
                setImagesVisibility(View.GONE);
                setVideoVisibility(View.GONE);
                setMusicVisibility(View.GONE);
                break;
            default:
                throw new IllegalStateException("feed item type not found:" + feedItemModel.getType());
        }
    }

    private void setMusicVisibility(int vis) {
        musicLinearLayout.setVisibility(vis);
    }

    private void setImagesVisibility(int vis) {
        imagesChildRV.setVisibility(vis);
        indicator.setVisibility(vis);
    }

    private void setVideoVisibility(int vis) {
        videoView.setVisibility(vis);
        thumbnail.setVisibility(vis);
    }

    private void setupImagesChildRV(RequestManager requestManager) {
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(
                imagesChildRV.getContext(),
                LinearLayoutManager.HORIZONTAL,
                false);

        layoutManager.setInitialPrefetchItemCount(imagesUrl.size());

        FeedImagesChildRVAdapter childItemAdapter
                = new FeedImagesChildRVAdapter(
                requestManager, imagesUrl);
        imagesChildRV.setLayoutManager(layoutManager);
        imagesChildRV.setAdapter(childItemAdapter);
        imagesChildRV.setRecycledViewPool(viewPool);
        indicator.attachToRecyclerView(imagesChildRV);
//        imagesChildRV.addItemDecoration(new CirclePagerIndicatorDecoration());
//        addTryCatch(() -> {
//            new PagerSnapHelper().attachToRecyclerView(imagesChildRV);
//        }, "FeedItemViewHolder");
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

    public RecyclerView getImagesChildRV() {
        return imagesChildRV;
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

    public String getId() {
        return id;
    }

    public ContentUploadStatus getContentUploadStatus() {
        return contentUploadStatus;
    }
}
