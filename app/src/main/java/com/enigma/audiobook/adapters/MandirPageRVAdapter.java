package com.enigma.audiobook.adapters;

import static com.enigma.audiobook.adapters.GodPageRVAdapter.GodPageViewTypes.POST_MESSAGE;
import static com.enigma.audiobook.adapters.MandirPageRVAdapter.MandirPageViewTypes.DETAILS;
import static com.enigma.audiobook.adapters.MandirPageRVAdapter.MandirPageViewTypes.FEED_ITEM;
import static com.enigma.audiobook.adapters.MandirPageRVAdapter.MandirPageViewTypes.FEED_ITEM_FOOTER;
import static com.enigma.audiobook.adapters.MandirPageRVAdapter.MandirPageViewTypes.HEADER;

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
import com.enigma.audiobook.models.MandirPageDetailsModel;
import com.enigma.audiobook.models.MandirPageHeaderModel;
import com.enigma.audiobook.models.ModelClassRetriever;
import com.enigma.audiobook.models.PostMessageModel;
import com.enigma.audiobook.proxies.FollowingsService;
import com.enigma.audiobook.proxies.ProxyUtils;
import com.enigma.audiobook.utils.FollowingUtils;
import com.enigma.audiobook.viewHolders.FeedItemFooterViewHolder;
import com.enigma.audiobook.viewHolders.FeedItemViewHolder;
import com.enigma.audiobook.viewHolders.PostMessageViewHolder;

import java.util.List;

public class MandirPageRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public enum MandirPageViewTypes implements ModelClassRetriever {
        HEADER(MandirPageHeaderModel.class),
        DETAILS(MandirPageDetailsModel.class),
        POST_MESSAGE(PostMessageModel.class),
        FEED_ITEM(FeedItemModel.class),
        FEED_ITEM_FOOTER(FeedItemFooterModel.class);

        Class<?> clazz;

        MandirPageViewTypes(Class<?> clazz) {
            this.clazz = clazz;
        }

        public Class<?> getModelClazz() {
            return clazz;
        }
    }

    RequestManager requestManager;
    List<GenericPageCardItemModel<MandirPageViewTypes>> cardItems;
    Context context;
    FollowingsService followingsService;
    String userId;
    String mandirId;

    public MandirPageRVAdapter(RequestManager requestManager,
                               List<GenericPageCardItemModel<MandirPageViewTypes>> cardItems,
                               Context context,
                               FollowingsService followingsService,
                               String userId,
                               String mandirId) {
        this.requestManager = requestManager;
        this.cardItems = cardItems;
        this.context = context;
        this.followingsService = followingsService;
        this.userId = userId;
        this.mandirId = mandirId;
    }

    public List<GenericPageCardItemModel<MandirPageViewTypes>> getCardItems() {
        return cardItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == HEADER.ordinal()) {
            return new MandirPageRVAdapter.MandirPageHeaderViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.card_mandir_page_header, parent, false));
        } else if (viewType == DETAILS.ordinal()) {
            return new MandirPageRVAdapter.MandirPageDetailsViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.card_mandir_page_details, parent, false));
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
        MandirPageRVAdapter.MandirPageViewTypes type = cardItems.get(position).getType();
        switch (type) {
            case HEADER:
                ((MandirPageRVAdapter.MandirPageHeaderViewHolder) holder)
                        .onBind((MandirPageHeaderModel) cardItems.get(position).getCardItem(),
                                requestManager,
                                followingsService,
                                userId,
                                mandirId);
                break;
            case DETAILS:
                ((MandirPageRVAdapter.MandirPageDetailsViewHolder) holder).onBind((MandirPageDetailsModel) cardItems.get(position).getCardItem(), requestManager);
                break;
            case POST_MESSAGE:
                ((PostMessageViewHolder) holder).onBind((PostMessageModel) cardItems.get(position).getCardItem(), requestManager, context, position);
                break;
            case FEED_ITEM:
                ((FeedItemViewHolder) holder)
                        .onBind((FeedItemModel) cardItems.get(position).getCardItem(),
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

    public static class MandirPageHeaderViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG = "MandirPageHeaderViewHolder";
        TextView title, followerCount;
        ImageView image;
        Button followBtn;
        LinearLayout followBtnLL;
        View parent;

        FollowingsService followingsService;
        String userId;
        String mandirId;
        boolean isFollowed = false;

        public MandirPageHeaderViewHolder(View itemView) {
            super(itemView);
            this.parent = itemView;
            this.title = itemView.findViewById(R.id.cardMandirPageHeaderTitleText);
            this.followerCount = itemView.findViewById(R.id.cardMandirPageHeaderFollowersCountText);
            this.image = itemView.findViewById(R.id.cardMandirPageHeaderImage);
            this.followBtn = itemView.findViewById(R.id.cardMandirPageHeaderFollowBtn);
            this.followBtnLL = itemView.findViewById(R.id.cardMandirPageHeaderFollowBtnLL);

        }

        public void onBind(MandirPageHeaderModel MandirPageHeaderModel, RequestManager requestManager,
                           FollowingsService followingsService, String userId,
                           String mandirId) {
            parent.setTag(this);
            this.followingsService = followingsService;
            this.userId = userId;
            this.mandirId = mandirId;
            this.title.setText(MandirPageHeaderModel.getTitle());
            this.followerCount.setText(MandirPageHeaderModel.getFollowerCountTxt());
            requestManager
                    .load(MandirPageHeaderModel.getImageUrl())
                    .into(image);

            isFollowed = MandirPageHeaderModel.isFollowed();
            if (MandirPageHeaderModel.isMyProfilePage()) {
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
                                    true, userId, mandirId, FollowingType.MANDIR);
                            setToFollowing();
                        } else {
                            ProxyUtils.updateFollowing(followingsService,
                                    false, userId, mandirId, FollowingType.MANDIR);
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

    public static class MandirPageDetailsViewHolder extends RecyclerView.ViewHolder {
        TextView details;

        public MandirPageDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            details = itemView.findViewById(R.id.cardMandirPageDetailsDescription);
        }

        public void onBind(MandirPageDetailsModel MandirPageDetailsModel, RequestManager requestManager) {
            this.details.setText(MandirPageDetailsModel.getHtmlDescription());
        }
    }
}
