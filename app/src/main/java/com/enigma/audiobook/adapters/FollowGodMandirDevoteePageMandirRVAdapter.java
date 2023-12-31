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
import com.enigma.audiobook.models.FollowGodMandirDevoteePageMandirItemModel;

import java.util.List;

public class FollowGodMandirDevoteePageMandirRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    RequestManager requestManager;
    List<FollowGodMandirDevoteePageMandirItemModel> cardItems;

    public FollowGodMandirDevoteePageMandirRVAdapter(RequestManager requestManager, List<FollowGodMandirDevoteePageMandirItemModel> cardItems) {
        this.requestManager = requestManager;
        this.cardItems = cardItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FollowGodMandirDevoteePageMandirItemViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.card_follow_god_mandir_devotee_fragment_mandir, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((FollowGodMandirDevoteePageMandirItemViewHolder) holder).onBind(cardItems.get(position), requestManager);
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

        boolean isFollowed;

        public FollowGodMandirDevoteePageMandirItemViewHolder(View itemView) {
            super(itemView);
            this.parent = itemView;
            this.title = itemView.findViewById(R.id.cardFragmentFollowGodMandirDevotee_MandirCardMandirTitle);
            this.image = itemView.findViewById(R.id.cardFragmentFollowGodMandirDevotee_MandirCardMandirImage);
            this.followBtn = itemView.findViewById(R.id.cardFragmentFollowGodMandirDevotee_MandirCardFollowBtn);
            this.location = itemView.findViewById(R.id.cardFragmentFollowGodMandirDevotee_MandirCardMandirLocation);
        }

        public void onBind(FollowGodMandirDevoteePageMandirItemModel model, RequestManager requestManager) {
            parent.setTag(this);
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
