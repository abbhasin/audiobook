package com.enigma.audiobook.adapters;

import static com.enigma.audiobook.adapters.GodPageRVAdapter.GodPageViewTypes.DETAILS;
import static com.enigma.audiobook.adapters.GodPageRVAdapter.GodPageViewTypes.FEED_ITEM;
import static com.enigma.audiobook.adapters.GodPageRVAdapter.GodPageViewTypes.FEED_ITEM_FOOTER;
import static com.enigma.audiobook.adapters.GodPageRVAdapter.GodPageViewTypes.HEADER;
import static com.enigma.audiobook.adapters.GodPageRVAdapter.GodPageViewTypes.POST_MESSAGE;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.enigma.audiobook.R;
import com.enigma.audiobook.backend.models.FollowingType;
import com.enigma.audiobook.models.FeedItemFooterModel;
import com.enigma.audiobook.models.FeedItemModel;
import com.enigma.audiobook.models.GenericPageCardItemModel;
import com.enigma.audiobook.models.GodPageDetailsModel;
import com.enigma.audiobook.models.GodPageHeaderModel;
import com.enigma.audiobook.models.ModelClassRetriever;
import com.enigma.audiobook.models.PostMessageModel;
import com.enigma.audiobook.proxies.FollowingsService;
import com.enigma.audiobook.proxies.ProxyUtils;
import com.enigma.audiobook.utils.FollowingUtils;
import com.enigma.audiobook.viewHolders.FeedItemFooterViewHolder;
import com.enigma.audiobook.viewHolders.FeedItemViewHolder;
import com.enigma.audiobook.viewHolders.PostMessageViewHolder;

import java.util.List;

public class GodPageRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public enum GodPageViewTypes implements ModelClassRetriever {
        HEADER(GodPageHeaderModel.class),
        DETAILS(GodPageDetailsModel.class),
        POST_MESSAGE(PostMessageModel.class),
        FEED_ITEM(FeedItemModel.class),
        FEED_ITEM_FOOTER(FeedItemFooterModel.class);

        Class<?> clazz;

        GodPageViewTypes(Class<?> clazz) {
            this.clazz = clazz;
        }

        public Class<?> getModelClazz() {
            return clazz;
        }
    }

    RequestManager requestManager;
    List<GenericPageCardItemModel<GodPageViewTypes>> cardItems;
    Context context;

    FollowingsService followingsService;
    String userId;
    String godId;

    public GodPageRVAdapter(RequestManager requestManager,
                            List<GenericPageCardItemModel<GodPageViewTypes>> cardItems,
                            Context context,
                            FollowingsService followingsService,
                            String userId,
                            String godId) {
        this.requestManager = requestManager;
        this.cardItems = cardItems;
        this.context = context;
        this.followingsService = followingsService;
        this.userId = userId;
        this.godId = godId;
    }

    public List<GenericPageCardItemModel<GodPageViewTypes>> getCardItems() {
        return cardItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == HEADER.ordinal()) {
            return new GodPageHeaderViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.card_god_page_header, parent, false));
        } else if (viewType == DETAILS.ordinal()) {
            return new GodPageDetailsViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.card_god_page_details, parent, false));
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
        GodPageViewTypes type = cardItems.get(position).getType();
        switch (type) {
            case HEADER:
                ((GodPageHeaderViewHolder) holder)
                        .onBind((GodPageHeaderModel) cardItems.get(position).getCardItem(),
                                requestManager,
                                followingsService,
                                userId,
                                godId);
                break;
            case DETAILS:
                ((GodPageDetailsViewHolder) holder).onBind((GodPageDetailsModel) cardItems.get(position).getCardItem(), requestManager);
                break;
            case POST_MESSAGE:
                ((PostMessageViewHolder) holder).onBind((PostMessageModel) cardItems.get(position).getCardItem(), requestManager, context, position);
                break;
            case FEED_ITEM:
                ((FeedItemViewHolder) holder)
                        .onBind(
                                (FeedItemModel) cardItems.get(position).getCardItem(),
                                requestManager,
                                context);
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

    public static class GodPageHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title, followerCount;
        ImageView image;
        Button followBtn;
        LinearLayout followBtnLL;
        View parent;

        FollowingsService followingsService;
        String userId;
        String godId;
        boolean isFollowed = false;

        public GodPageHeaderViewHolder(View itemView) {
            super(itemView);
            this.parent = itemView;
            this.title = itemView.findViewById(R.id.cardGodPageHeaderTitleText);
            this.followerCount = itemView.findViewById(R.id.cardGodPageHeaderFollowersCountText);
            this.image = itemView.findViewById(R.id.cardGodPageHeaderImage);
            this.followBtn = itemView.findViewById(R.id.cardGodPageHeaderFollowBtn);
            this.followBtnLL = itemView.findViewById(R.id.cardGodPageHeaderFollowBtnLL);
        }

        public void onBind(GodPageHeaderModel godPageHeaderModel, RequestManager requestManager,
                           FollowingsService followingsService, String userId,
                           String godId) {
            parent.setTag(this);
            this.followingsService = followingsService;
            this.godId = godId;
            this.userId = userId;
            this.title.setText(godPageHeaderModel.getTitle());
            this.followerCount.setText(godPageHeaderModel.getFollowerCountTxt());
            requestManager
                    .load(godPageHeaderModel.getImageUrl())
                    .into(image);
            isFollowed = godPageHeaderModel.isFollowed();
            if (godPageHeaderModel.isMyProfilePage()) {
                followBtnLL.setVisibility(View.GONE);
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
                                    true, userId, godId, FollowingType.GOD);
                            setToFollowing();
                        } else {
                            ProxyUtils.updateFollowing(followingsService,
                                    false, userId, godId, FollowingType.GOD);
                            setToNotFollowing();
                        }
                    }
                });
            }
        }

        private void setToFollowing() {
            isFollowed = FollowingUtils.setToFollowing(followBtn, followBtnLL);
        }

        private void setToNotFollowing() {
            isFollowed = FollowingUtils.setToNotFollowing(followBtn, followBtnLL);
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

    public static class GodPageDetailsViewHolder extends RecyclerView.ViewHolder {
        TextView details;

        public GodPageDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            details = itemView.findViewById(R.id.cardGodPageDetailsDescription);
        }

        public void onBind(GodPageDetailsModel godPageDetailsModel, RequestManager requestManager) {
            this.details.setText(godPageDetailsModel.getHtmlDescription());
        }
    }
}
