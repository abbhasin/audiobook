package com.enigma.audiobook.adapters;

import static com.enigma.audiobook.activities.PujariPageActivity.INFLUENCER_ID_KEY;

import android.content.Context;
import android.content.Intent;
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
import com.enigma.audiobook.activities.PujariPageActivity;
import com.enigma.audiobook.backend.models.FollowingType;
import com.enigma.audiobook.models.FollowGodMandirDevoteePageDevoteeItemModel;
import com.enigma.audiobook.proxies.FollowingsService;
import com.enigma.audiobook.proxies.ProxyUtils;

import java.util.List;

public class FollowGodMandirDevoteePageDevoteeRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    RequestManager requestManager;
    List<FollowGodMandirDevoteePageDevoteeItemModel> cardItems;
    FollowingsService followingsService;
    String userId;
    Context context;

    public FollowGodMandirDevoteePageDevoteeRVAdapter(RequestManager requestManager, List<FollowGodMandirDevoteePageDevoteeItemModel> cardItems,
                                                      FollowingsService followingsService, String userId,
                                                      Context context) {
        this.requestManager = requestManager;
        this.cardItems = cardItems;
        this.followingsService = followingsService;
        this.userId = userId;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FollowGodMandirDevoteePageDevoteeItemViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.card_follow_god_mandir_devotee_fragment_devotee, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((FollowGodMandirDevoteePageDevoteeItemViewHolder) holder)
                .onBind(cardItems.get(position),
                        requestManager,
                        followingsService,
                        userId,
                        context);
    }

    @Override
    public int getItemCount() {
        return cardItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public static class FollowGodMandirDevoteePageDevoteeItemViewHolder extends RecyclerView.ViewHolder {
        TextView title, postsCount;
        ImageView image;
        Button followBtn;
        View parent;

        FollowingsService followingsService;
        String userId;
        String influencerId;
        boolean isFollowed;

        public FollowGodMandirDevoteePageDevoteeItemViewHolder(View itemView) {
            super(itemView);
            this.parent = itemView;
            this.title = itemView.findViewById(R.id.cardFragmentFollowGodMandirDevotee_DevoteeCardDevoteeName);
            this.image = itemView.findViewById(R.id.cardFragmentFollowGodMandirDevotee_DevoteeCardDevoteeImage);
            this.followBtn = itemView.findViewById(R.id.cardFragmentFollowGodMandirDevotee_DevoteeCardFollowBtn);
            this.postsCount = itemView.findViewById(R.id.cardFragmentFollowGodMandirDevotee_DevoteeCardDevoteePostsCount);
        }

        public void onBind(FollowGodMandirDevoteePageDevoteeItemModel model, RequestManager requestManager,
                           FollowingsService followingsService, String userId,
                           Context context) {
            parent.setTag(this);
            this.followingsService = followingsService;
            this.userId = userId;
            this.influencerId = model.getInfluencerId();

            this.title.setText(model.getDevoteeName());
            this.postsCount.setText(String.valueOf(model.getNumPosts()));
            requestManager
                    .load(model.getImageUrl())
                    .into(image);
            isFollowed = model.isFollowed();
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
            parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, PujariPageActivity.class);
                    i.putExtra(INFLUENCER_ID_KEY, influencerId);
                    context.startActivity(i);
                }
            });
        }

        private void setToFollowing() {
            followBtn.setBackgroundColor(0xFFDFD1FA);
            followBtn.setText("Following");
            isFollowed = true;
        }

        private void setToNotFollowing() {
            followBtn.setBackgroundColor(0xFFB0ECE6);
            followBtn.setText("Follow");
            isFollowed = false;
        }

        public TextView getTitle() {
            return title;
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
}
