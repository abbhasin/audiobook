package com.enigma.audiobook.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.enigma.audiobook.R;
import com.enigma.audiobook.models.LibraryPageAlbumsModel;

import java.util.List;

public class LibraryPageRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private RequestManager requestManager;
    private List<LibraryPageAlbumsModel> libraryPageAlbumsModels;

    public LibraryPageRVAdapter(Context context, RequestManager requestManager, List<LibraryPageAlbumsModel> libraryPageAlbumsModels) {
        this.context = context;
        this.requestManager = requestManager;
        this.libraryPageAlbumsModels = libraryPageAlbumsModels;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LibraryPageAlbumsViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.card_library_page_albums, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((LibraryPageAlbumsViewHolder) holder).onBind(libraryPageAlbumsModels.get(position),
                requestManager, context);
    }

    @Override
    public int getItemCount() {
        return libraryPageAlbumsModels.size();
    }

    public static class LibraryPageAlbumsViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        RecyclerView childRV;
        RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();

        public LibraryPageAlbumsViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.libraryPageAlbumsTitle);
            childRV = itemView.findViewById(R.id.libraryPageAlbumsChildRV);
        }

        public void onBind(LibraryPageAlbumsModel libraryPageAlbumsModel, RequestManager requestManager, Context context) {
            title.setText(libraryPageAlbumsModel.getTitle());
            setupAlbumsChildRV(libraryPageAlbumsModel, requestManager, context);
        }

        private void setupAlbumsChildRV(LibraryPageAlbumsModel libraryPageAlbumsModel, RequestManager requestManager, Context context) {
            LinearLayoutManager layoutManager
                    = new LinearLayoutManager(
                    childRV.getContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false);

            layoutManager.setInitialPrefetchItemCount(libraryPageAlbumsModel.getAlbumItems().size());

            LibraryPageAlbumsChildRVAdapter childItemAdapter
                    = new LibraryPageAlbumsChildRVAdapter(
                    context, requestManager, libraryPageAlbumsModel.getAlbumItems());
            childRV.setLayoutManager(layoutManager);
            childRV.setAdapter(childItemAdapter);
            childRV.setRecycledViewPool(viewPool);
        }
    }
}
