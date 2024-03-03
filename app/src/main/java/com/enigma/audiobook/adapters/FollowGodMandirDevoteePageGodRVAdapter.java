package com.enigma.audiobook.adapters;

import static com.enigma.audiobook.activities.GodPageActivity.GOD_ID_KEY;

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
import com.enigma.audiobook.activities.GodPageActivity;
import com.enigma.audiobook.backend.models.FollowingType;
import com.enigma.audiobook.models.FollowGodMandirDevoteePageGodItemModel;
import com.enigma.audiobook.proxies.FollowingsService;
import com.enigma.audiobook.proxies.ProxyUtils;
import com.enigma.audiobook.utils.FollowingUtils;

import java.util.List;

public class FollowGodMandirDevoteePageGodRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    RequestManager requestManager;
    List<FollowGodMandirDevoteePageGodItemModel> cardItems;
    FollowingsService followingsService;
    String userId;
    Context context;

    public FollowGodMandirDevoteePageGodRVAdapter(RequestManager requestManager,
                                                  List<FollowGodMandirDevoteePageGodItemModel> cardItems,
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
        return new FollowGodMandirDevoteePageGodItemViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.card_follow_god_mandir_devotee_fragment_god, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((FollowGodMandirDevoteePageGodItemViewHolder) holder)
                .onBind(cardItems.get(position), requestManager,
                        followingsService, userId,
                        context
                );
    }

    @Override
    public int getItemCount() {
        return cardItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public static class FollowGodMandirDevoteePageGodItemViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image;
        Button followBtn;
        View parent;

        FollowingsService followingsService;
        String userId;
        String godId;
        boolean isFollowed;

        public FollowGodMandirDevoteePageGodItemViewHolder(View itemView) {
            super(itemView);
            this.parent = itemView;
            this.title = itemView.findViewById(R.id.cardFragmentFollowGodMandirDevotee_GodCardTitle);
            this.image = itemView.findViewById(R.id.cardFragmentFollowGodMandirDevotee_GodCardGodImage);
            this.followBtn = itemView.findViewById(R.id.cardFragmentFollowGodMandirDevotee_GodCardFollowBtn);
        }

        public void onBind(FollowGodMandirDevoteePageGodItemModel model, RequestManager requestManager,
                           FollowingsService followingsService, String userId,
                           Context context) {
            parent.setTag(this);
            this.followingsService = followingsService;
            this.userId = userId;
            this.godId = model.getGodId();

            this.title.setText(model.getGodName());
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
                                true, userId, godId, FollowingType.GOD);
                        setToFollowing();
                    } else {
                        ProxyUtils.updateFollowing(followingsService,
                                false, userId, godId, FollowingType.GOD);
                        setToNotFollowing();
                    }
                }
            });

            parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, GodPageActivity.class);
                    i.putExtra(GOD_ID_KEY, godId);
                    context.startActivity(i);
                }
            });
        }

        private void setToFollowing() {
            isFollowed = FollowingUtils.setToFollowing(followBtn, null);
        }

        private void setToNotFollowing() {
            isFollowed = FollowingUtils.setToNotFollowing(followBtn, null);
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
