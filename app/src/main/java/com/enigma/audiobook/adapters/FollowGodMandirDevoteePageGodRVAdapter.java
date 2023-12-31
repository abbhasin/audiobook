package com.enigma.audiobook.adapters;

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
import com.enigma.audiobook.models.FollowGodMandirDevoteePageGodItemModel;

import java.util.List;

public class FollowGodMandirDevoteePageGodRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    RequestManager requestManager;
    List<FollowGodMandirDevoteePageGodItemModel> cardItems;

    public FollowGodMandirDevoteePageGodRVAdapter(RequestManager requestManager, List<FollowGodMandirDevoteePageGodItemModel> cardItems) {
        this.requestManager = requestManager;
        this.cardItems = cardItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FollowGodMandirDevoteePageGodItemViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.card_follow_god_mandir_devotee_fragment_god, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((FollowGodMandirDevoteePageGodItemViewHolder) holder).onBind(cardItems.get(position), requestManager);
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

        boolean isFollowed;

        public FollowGodMandirDevoteePageGodItemViewHolder(View itemView) {
            super(itemView);
            this.parent = itemView;
            this.title = itemView.findViewById(R.id.cardFragmentFollowGodMandirDevotee_GodCardTitle);
            this.image = itemView.findViewById(R.id.cardFragmentFollowGodMandirDevotee_GodCardGodImage);
            this.followBtn = itemView.findViewById(R.id.cardFragmentFollowGodMandirDevotee_GodCardFollowBtn);
        }

        public void onBind(FollowGodMandirDevoteePageGodItemModel model, RequestManager requestManager) {
            parent.setTag(this);
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
                        setToFollowing();
                    } else {
                        setToNotFollowing();
                    }
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
