package com.enigma.audiobook.adapters;

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
import com.enigma.audiobook.activities.MusicListActivity;
import com.enigma.audiobook.activities.VideoListActivity;
import com.enigma.audiobook.models.MenuItemObject;

import java.util.List;

public class MenuItemCardViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    RequestManager requestManager;
    List<MenuItemObject> menuItemObjects;
    Context context;

    public MenuItemCardViewAdapter(RequestManager requestManager, List<MenuItemObject> menuItemObjects, Context context) {
        this.requestManager = requestManager;
        this.menuItemObjects = menuItemObjects;
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
        ((MenuItemCardViewHolder) holder).onBind(menuItemObjects.get(position), requestManager);
        ((MenuItemCardViewHolder) holder).cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchActivity(((MenuItemCardViewHolder) holder).activityType);
            }
        });
    }

    @Override
    public int getItemCount() {
        return menuItemObjects.size();
    }

    private void launchActivity(MenuItemObject.ActivityType type) {
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
            default:
                throw new IllegalStateException("ActivityType not supported:" + type);
        }

    }

    public static class MenuItemCardViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private ImageView image;
        private MenuItemObject.ActivityType activityType;
        private CardView cardView;

        public MenuItemCardViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.cardViewMenuItemText);
            image = itemView.findViewById(R.id.cardViewMenuItemImage);
            cardView = itemView.findViewById(R.id.cardViewMenuItem);
        }

        public void onBind(MenuItemObject menuItemObject, RequestManager requestManager) {
            this.textView.setText(menuItemObject.getText());
            this.activityType = menuItemObject.getActivityType();
            requestManager
                    .load(menuItemObject.getImageUrl())
                    .into(image);
        }
    }
}
