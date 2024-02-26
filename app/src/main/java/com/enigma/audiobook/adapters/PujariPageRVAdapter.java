package com.enigma.audiobook.adapters;

import static com.enigma.audiobook.adapters.GodPageRVAdapter.GodPageViewTypes.POST_MESSAGE;
import static com.enigma.audiobook.adapters.PujariPageRVAdapter.PujariPageViewTypes.DETAILS;
import static com.enigma.audiobook.adapters.PujariPageRVAdapter.PujariPageViewTypes.FEED_ITEM;
import static com.enigma.audiobook.adapters.PujariPageRVAdapter.PujariPageViewTypes.FEED_ITEM_FOOTER;
import static com.enigma.audiobook.adapters.PujariPageRVAdapter.PujariPageViewTypes.HEADER;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.enigma.audiobook.R;
import com.enigma.audiobook.backend.models.FollowingType;
import com.enigma.audiobook.models.FeedItemFooterModel;
import com.enigma.audiobook.models.FeedItemModel;
import com.enigma.audiobook.models.GenericPageCardItemModel;
import com.enigma.audiobook.models.PostMessageModel;
import com.enigma.audiobook.models.PujariPageDetailsModel;
import com.enigma.audiobook.models.PujariPageHeaderModel;
import com.enigma.audiobook.models.ModelClassRetriever;
import com.enigma.audiobook.proxies.FollowingsService;
import com.enigma.audiobook.proxies.ProxyUtils;
import com.enigma.audiobook.viewHolders.FeedItemFooterViewHolder;
import com.enigma.audiobook.viewHolders.FeedItemViewHolder;
import com.enigma.audiobook.viewHolders.PostMessageViewHolder;

import java.util.List;

public class PujariPageRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public enum PujariPageViewTypes implements ModelClassRetriever {
        HEADER(PujariPageHeaderModel.class),
        DETAILS(PujariPageDetailsModel.class),
        POST_MESSAGE(PostMessageModel.class),
        FEED_ITEM(FeedItemModel.class),
        FEED_ITEM_FOOTER(FeedItemFooterModel.class);

        Class<?> clazz;

        PujariPageViewTypes(Class<?> clazz) {
            this.clazz = clazz;
        }

        public Class<?> getModelClazz() {
            return clazz;
        }
    }

    RequestManager requestManager;
    List<GenericPageCardItemModel<PujariPageRVAdapter.PujariPageViewTypes>> cardItems;
    Context context;

    FollowingsService followingsService;
    String userId;
    String influencerId;

    public PujariPageRVAdapter(RequestManager requestManager,
                               List<GenericPageCardItemModel<PujariPageViewTypes>> cardItems,
                               Context context,
                               FollowingsService followingsService,
                               String userId,
                               String influencerId) {
        this.requestManager = requestManager;
        this.cardItems = cardItems;
        this.context = context;
        this.followingsService = followingsService;
        this.userId = userId;
        this.influencerId = influencerId;
    }

    public List<GenericPageCardItemModel<PujariPageViewTypes>> getCardItems() {
        return cardItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == HEADER.ordinal()) {
            return new PujariPageRVAdapter.PujariPageHeaderViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.card_pujari_page_header, parent, false));
        } else if (viewType == DETAILS.ordinal()) {
            return new PujariPageRVAdapter.PujariPageDetailsViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.card_pujari_page_details, parent, false));
        } else if (viewType == POST_MESSAGE.ordinal()) {
            return new PostMessageViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.card_post_message, parent, false));
        } else if (viewType == FEED_ITEM.ordinal()) {
            return new FeedItemViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.card_feed_item, parent, false));
        } else if (viewType == FEED_ITEM_FOOTER.ordinal()) {
            return new FeedItemFooterViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.card_feed_item_footer, parent, false));
        } else {
            throw new IllegalStateException("unhandled view type:" + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PujariPageRVAdapter.PujariPageViewTypes type = cardItems.get(position).getType();
        switch (type) {
            case HEADER:
                ((PujariPageRVAdapter.PujariPageHeaderViewHolder) holder)
                        .onBind((PujariPageHeaderModel) cardItems.get(position).getCardItem(),
                                requestManager,
                                followingsService,
                                userId,
                                influencerId);
                break;
            case DETAILS:
                ((PujariPageRVAdapter.PujariPageDetailsViewHolder) holder).onBind((PujariPageDetailsModel) cardItems.get(position).getCardItem(), requestManager);
                break;
            case POST_MESSAGE:
                ((PostMessageViewHolder) holder).onBind((PostMessageModel) cardItems.get(position).getCardItem(), requestManager, context, position);
                break;
            case FEED_ITEM:
                ((FeedItemViewHolder) holder).onBind((FeedItemModel) cardItems.get(position).getCardItem(), requestManager);
                break;
            case FEED_ITEM_FOOTER:
                ((FeedItemFooterViewHolder) holder).onBind((FeedItemFooterModel) cardItems.get(position).getCardItem(), requestManager);
                break;
            default:
                throw new IllegalStateException("unhandled view type:" + type);
        }
    }

    @Override
    public int getItemCount() {
        return cardItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return cardItems.get(position).getType().ordinal();
    }

    public static class PujariPageHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title, followerCount;
        ImageView image;
        Button followBtn;
        View parent;

        FollowingsService followingsService;
        String userId;
        String influencerId;
        boolean isFollowed = false;

        public PujariPageHeaderViewHolder(View itemView) {
            super(itemView);
            this.parent = itemView;
            this.title = itemView.findViewById(R.id.cardPujariPageHeaderTitleText);
            this.followerCount = itemView.findViewById(R.id.cardPujariPageHeaderFollowersCountText);
            this.image = itemView.findViewById(R.id.cardPujariPageHeaderImage);
            this.followBtn = itemView.findViewById(R.id.cardPujariPageHeaderFollowBtn);
        }

        public void onBind(PujariPageHeaderModel PujariPageHeaderModel, RequestManager requestManager,
                           FollowingsService followingsService, String userId,
                           String influencerId) {
            parent.setTag(this);
            this.followingsService = followingsService;
            this.userId = userId;
            this.influencerId = influencerId;
            this.title.setText(PujariPageHeaderModel.getTitle());
            this.followerCount.setText(PujariPageHeaderModel.getFollowerCountTxt());
            requestManager
                    .load(PujariPageHeaderModel.getImageUrl())
                    .into(image);
            isFollowed = PujariPageHeaderModel.isFollowed();
            if (PujariPageHeaderModel.isMyProfilePage()) {
                followBtn.setVisibility(View.GONE);
            } else {
                if (!isFollowed) {
                    setToNotFollowing();
                } else {
                    setToFollowing();
                }
                followBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isFollowed) {
                            ProxyUtils.updateFollowing(followingsService,
                                    true, userId, influencerId, FollowingType.INFLUENCER);
                            setToFollowing();
                        } else {
                            ProxyUtils.updateFollowing(followingsService,
                                    false, userId, influencerId, FollowingType.INFLUENCER);
                            setToNotFollowing();
                        }
                    }
                });
            }
        }

        private void setToFollowing() {
            followBtn.setBackgroundColor(0xFFDFD1FA);
            followBtn.setText("Following");
            isFollowed = true;
        }

        private void setToNotFollowing() {
            followBtn.setBackgroundColor(0x29B6F6);
            followBtn.setText("Follow");
            isFollowed = false;
        }

        public TextView getTitle() {
            return title;
        }

        public TextView getFollowerCount() {
            return followerCount;
        }

        public ImageView getImage() {
            return image;
        }

        public Button getFollowBtn() {
            return followBtn;
        }

        public View getParent() {
            return parent;
        }
    }

    public static class PujariPageDetailsViewHolder extends RecyclerView.ViewHolder {
        TextView details;

        public PujariPageDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            details = itemView.findViewById(R.id.cardPujariPageDetailsDescription);
        }

        public void onBind(PujariPageDetailsModel PujariPageDetailsModel, RequestManager requestManager) {
            this.details.setText(PujariPageDetailsModel.getHtmlDescription());
        }
    }
}
