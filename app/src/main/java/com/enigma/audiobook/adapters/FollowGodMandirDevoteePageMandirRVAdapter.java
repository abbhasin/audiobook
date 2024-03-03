package com.enigma.audiobook.adapters;

import static com.enigma.audiobook.activities.MandirPageActivity.MANDIR_ID_KEY;

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
import com.enigma.audiobook.activities.MandirPageActivity;
import com.enigma.audiobook.backend.models.FollowingType;
import com.enigma.audiobook.models.FollowGodMandirDevoteePageMandirItemModel;
import com.enigma.audiobook.proxies.FollowingsService;
import com.enigma.audiobook.proxies.ProxyUtils;
import com.enigma.audiobook.utils.FollowingUtils;

import java.util.List;

public class FollowGodMandirDevoteePageMandirRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    RequestManager requestManager;
    List<FollowGodMandirDevoteePageMandirItemModel> cardItems;
    FollowingsService followingsService;
    String userId;
    Context context;

    public FollowGodMandirDevoteePageMandirRVAdapter(RequestManager requestManager,
                                                     List<FollowGodMandirDevoteePageMandirItemModel> cardItems,
                                                     FollowingsService followingsService, String userId, Context context) {
        this.requestManager = requestManager;
        this.cardItems = cardItems;
        this.followingsService = followingsService;
        this.userId = userId;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FollowGodMandirDevoteePageMandirItemViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.card_follow_god_mandir_devotee_fragment_mandir, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((FollowGodMandirDevoteePageMandirItemViewHolder) holder)
                .onBind(cardItems.get(position), requestManager,
                        followingsService, userId,
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

    public static class FollowGodMandirDevoteePageMandirItemViewHolder extends RecyclerView.ViewHolder {
        TextView title, location;
        ImageView image;
        Button followBtn;
        View parent;

        FollowingsService followingsService;
        String userId;
        String mandirId;
        boolean isFollowed;

        public FollowGodMandirDevoteePageMandirItemViewHolder(View itemView) {
            super(itemView);
            this.parent = itemView;
            this.title = itemView.findViewById(R.id.cardFragmentFollowGodMandirDevotee_MandirCardMandirTitle);
            this.image = itemView.findViewById(R.id.cardFragmentFollowGodMandirDevotee_MandirCardMandirImage);
            this.followBtn = itemView.findViewById(R.id.cardFragmentFollowGodMandirDevotee_MandirCardFollowBtn);
            this.location = itemView.findViewById(R.id.cardFragmentFollowGodMandirDevotee_MandirCardMandirLocation);
        }

        public void onBind(FollowGodMandirDevoteePageMandirItemModel model, RequestManager requestManager,
                           FollowingsService followingsService, String userId,
                           Context context) {
            parent.setTag(this);
            this.followingsService = followingsService;
            this.userId = userId;
            this.mandirId = model.getMandirId();

            this.title.setText(model.getMandirName());
            requestManager
                    .load(model.getImageUrl())
                    .into(image);
            this.location.setText(model.getLocation());

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
                                true, userId, mandirId, FollowingType.MANDIR);
                        setToFollowing();
                    } else {
                        ProxyUtils.updateFollowing(followingsService,
                                false, userId, mandirId, FollowingType.MANDIR);
                        setToNotFollowing();
                    }
                }
            });

            parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, MandirPageActivity.class);
                    i.putExtra(MANDIR_ID_KEY, mandirId);
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
