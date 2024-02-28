package com.enigma.audiobook.adapters;

import static com.enigma.audiobook.activities.GodPageActivity.GOD_ID_KEY;
import static com.enigma.audiobook.activities.MandirPageActivity.MANDIR_ID_KEY;
import static com.enigma.audiobook.activities.PujariPageActivity.INFLUENCER_ID_KEY;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.enigma.audiobook.R;
import com.enigma.audiobook.activities.DarshanActivity;
import com.enigma.audiobook.activities.FirebaseUIActivity;
import com.enigma.audiobook.activities.FollowMandirAndDevoteesActivity;
import com.enigma.audiobook.activities.GodPageActivity;
import com.enigma.audiobook.activities.LibraryActivity;
import com.enigma.audiobook.activities.MandirPageActivity;
import com.enigma.audiobook.activities.MusicListActivity;
import com.enigma.audiobook.activities.MyFeedActivity;
import com.enigma.audiobook.activities.PujariPageActivity;
import com.enigma.audiobook.activities.TestCrashActivity;
import com.enigma.audiobook.activities.VideoListActivity;
import com.enigma.audiobook.models.MenuItemModel;

import java.util.List;

public class MenuItemCardViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    RequestManager requestManager;
    List<MenuItemModel> menuItemModels;
    Context context;

    public MenuItemCardViewAdapter(RequestManager requestManager, List<MenuItemModel> menuItemModels, Context context) {
        this.requestManager = requestManager;
        this.menuItemModels = menuItemModels;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MenuItemCardViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.card_menu_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((MenuItemCardViewHolder) holder).onBind(menuItemModels.get(position), requestManager);
        ((MenuItemCardViewHolder) holder).cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchActivity(((MenuItemCardViewHolder) holder).activityType);
            }
        });
    }

    @Override
    public int getItemCount() {
        return menuItemModels.size();
    }

    private void launchActivity(MenuItemModel.ActivityType type) {
        Intent i;
        switch (type) {
            case DARSHAN:
                i = new Intent(context, DarshanActivity.class);
                context.startActivity(i);
                return;
            case MUSIC_LIST:
                i = new Intent(context, MusicListActivity.class);
                context.startActivity(i);
                return;
            case VIDEO_LIST:
                i = new Intent(context, VideoListActivity.class);
                context.startActivity(i);
                return;
            case GOD_PAGE:
                i = new Intent(context, GodPageActivity.class);
                i.putExtra(GOD_ID_KEY, "65c234631298c936bf93450a");
                context.startActivity(i);
                return;
            case MANDIR_PAGE:
                i = new Intent(context, MandirPageActivity.class);
                i.putExtra(MANDIR_ID_KEY, "65c3dec10568b52d596ef147");
                context.startActivity(i);
                return;
            case PUJARI_PAGE:
                i = new Intent(context, PujariPageActivity.class);
                i.putExtra(INFLUENCER_ID_KEY, "65c5034dc76eef0b30919614");
                context.startActivity(i);
                return;
            case MY_FEED:
                i = new Intent(context, MyFeedActivity.class);
                context.startActivity(i);
                return;
            case FOLLOW_GOD_MANDIR_DEVOTEE:
                i = new Intent(context, FollowMandirAndDevoteesActivity.class);
                context.startActivity(i);
                return;
            case LIBRARY:
                i = new Intent(context, LibraryActivity.class);
                context.startActivity(i);
                return;
            case TEST_CRASH:
                i = new Intent(context, TestCrashActivity.class);
                context.startActivity(i);
                return;
            case SIGN_IN:
                i = new Intent(context, FirebaseUIActivity.class);
                context.startActivity(i);
                return;
            default:
                throw new IllegalStateException("ActivityType not supported:" + type);
        }

    }

    public static class MenuItemCardViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private ImageView image;
        private MenuItemModel.ActivityType activityType;
        private CardView cardView;

        public MenuItemCardViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.cardViewMenuItemText);
            image = itemView.findViewById(R.id.cardViewMenuItemImage);
            cardView = itemView.findViewById(R.id.cardViewMenuItem);
        }

        public void onBind(MenuItemModel menuItemModel, RequestManager requestManager) {
            this.textView.setText(menuItemModel.getText());
            this.activityType = menuItemModel.getActivityType();
            requestManager
                    .load(menuItemModel.getImageUrl())
                    .into(image);
        }
    }
}
