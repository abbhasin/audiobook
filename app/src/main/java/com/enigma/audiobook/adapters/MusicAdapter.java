package com.enigma.audiobook.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.enigma.audiobook.R;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    List<String> musics;
    Context context;

    CardTouchListener cardTouchListener;

    public MusicAdapter(List<String> musics, Context context) {
        this.musics = musics;
        this.context = context;
        this.cardTouchListener = (CardTouchListener) context;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_music, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, @SuppressLint("RecyclerView") int position) {
        //int position = holder.getAdapterPosition();
        String musicFile = musics.get(position);
        Log.i("MusicAdapter", "found music file:" + musicFile);
        String title = musicFile.substring(musicFile.lastIndexOf("/") + 1);
        holder.textViewFileName.setText(title);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardTouchListener.itemTouch(musicFile, position);
//                Intent i = new Intent(context, MusicActivity.class);
//                i.putExtra("title", title);
//                i.putExtra("filePath", musicFile);
//                i.putExtra("position", position);
//                i.putExtra("musics", musics.toArray(new String[]{}));
//                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return musics.size();
    }

    public class MusicViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewFileName;
        private CardView cardView;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewFileName = itemView.findViewById(R.id.musicCardFileNameText);
            cardView = itemView.findViewById(R.id.cardViewMusic);
        }
    }

    public interface CardTouchListener {
        void itemTouch(String musicFile, int position);
    }
}
