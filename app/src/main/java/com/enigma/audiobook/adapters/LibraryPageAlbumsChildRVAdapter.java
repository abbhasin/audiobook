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
import com.enigma.audiobook.activities.MusicListActivity;
import com.enigma.audiobook.activities.PujariPageActivity;
import com.enigma.audiobook.models.LibraryPageAlbumsModel;

import java.util.List;

public class LibraryPageAlbumsChildRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    RequestManager requestManager;
    List<LibraryPageAlbumsModel.AlbumItem> albumItems;

    public LibraryPageAlbumsChildRVAdapter(Context context, RequestManager requestManager, List<LibraryPageAlbumsModel.AlbumItem> albumItems) {
        this.context = context;
        this.requestManager = requestManager;
        this.albumItems = albumItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LibraryPageAlbumItemViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.card_library_page_albums_child, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((LibraryPageAlbumItemViewHolder) holder).onBind(albumItems.get(position),
                requestManager, context);
    }

    @Override
    public int getItemCount() {
        return albumItems.size();
    }

    public static class LibraryPageAlbumItemViewHolder extends RecyclerView.ViewHolder {

        TextView text;
        ImageView image;
        CardView card;

        public LibraryPageAlbumItemViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.cardLibraryPageAlbumsChildText);
            image = itemView.findViewById(R.id.cardLibraryPageAlbumsChildImage);
            card = itemView.findViewById(R.id.cardLibraryPageAlbumsChild);

        }

        public void onBind(LibraryPageAlbumsModel.AlbumItem albumItem, RequestManager requestManager, Context context) {
            text.setText(albumItem.getText());
            requestManager
                    .load(albumItem.getImageUrl())
                    .into(image);
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, MusicListActivity.class);
                    context.startActivity(i);
                }
            });
        }
    }
}
