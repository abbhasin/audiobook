package com.enigma.audiobook.adapters;

import static com.enigma.audiobook.adapters.MyFeedRVAdapter.MyFeedViewTypes.FEED_ITEM;
import static com.enigma.audiobook.adapters.MyFeedRVAdapter.MyFeedViewTypes.FEED_ITEM_FOOTER;
import static com.enigma.audiobook.adapters.MyFeedRVAdapter.MyFeedViewTypes.HEADER;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.enigma.audiobook.R;
import com.enigma.audiobook.models.FeedItemFooterModel;
import com.enigma.audiobook.models.FeedItemModel;
import com.enigma.audiobook.models.GenericPageCardItemModel;
import com.enigma.audiobook.models.ModelClassRetriever;
import com.enigma.audiobook.models.MyFeedHeaderModel;
import com.enigma.audiobook.viewHolders.FeedItemFooterViewHolder;
import com.enigma.audiobook.viewHolders.FeedItemViewHolder;

import java.util.List;

public class MyFeedRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public enum MyFeedViewTypes implements ModelClassRetriever {
        HEADER(MyFeedHeaderModel.class),
        FEED_ITEM(FeedItemModel.class),
        FEED_ITEM_FOOTER(FeedItemFooterModel.class);

        Class<?> clazz;

        MyFeedViewTypes(Class<?> clazz) {
            this.clazz = clazz;
        }

        public Class<?> getModelClazz() {
            return clazz;
        }
    }

    RequestManager requestManager;
    List<GenericPageCardItemModel<MyFeedViewTypes>> cardItems;

    public MyFeedRVAdapter(RequestManager requestManager, List<GenericPageCardItemModel<MyFeedViewTypes>> cardItems) {
        this.requestManager = requestManager;
        this.cardItems = cardItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == HEADER.ordinal()) {
            return new MyFeedRVAdapter.MyFeedHeaderViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.card_my_feed_header, parent, false));
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
        MyFeedRVAdapter.MyFeedViewTypes type = cardItems.get(position).getType();
        switch (type) {
            case HEADER:
                ((MyFeedRVAdapter.MyFeedHeaderViewHolder) holder).onBind((MyFeedHeaderModel) cardItems.get(position).getCardItem(), requestManager);
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
        MyFeedRVAdapter.MyFeedViewTypes type = cardItems.get(position).getType();
        return type.ordinal();
    }

    public static class MyFeedHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView followingCount, followMore;

        public MyFeedHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            followingCount = itemView.findViewById(R.id.cardMyFeedHeaderFollowingCountText);
            followMore = itemView.findViewById(R.id.cardMyFeedHeaderFollowMore);
        }

        public void onBind(MyFeedHeaderModel myFeedHeaderModel, RequestManager requestManager) {
            followingCount.setText(String.valueOf(myFeedHeaderModel.getFollowingCount()));
            followMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }
}
