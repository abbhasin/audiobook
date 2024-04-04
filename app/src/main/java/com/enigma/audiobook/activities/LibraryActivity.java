package com.enigma.audiobook.activities;

import static com.enigma.audiobook.utils.Utils.initGlide;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.WindowManager;

import com.enigma.audiobook.R;
import com.enigma.audiobook.adapters.LibraryPageRVAdapter;
import com.enigma.audiobook.models.LibraryPageAlbumsModel;
import com.enigma.audiobook.models.MenuItemModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LibraryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        recyclerView = findViewById(R.id.libraryPageRV);
        initRecyclerView();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        List<LibraryPageAlbumsModel> models = getModels();
        LibraryPageRVAdapter adapter = new LibraryPageRVAdapter(this, initGlide(this), models);
        recyclerView.setAdapter(adapter);
    }

    private List<LibraryPageAlbumsModel> getModels() {
        LibraryPageAlbumsModel.AlbumItem[] ALBUM_ITEMS = {
                new LibraryPageAlbumsModel.AlbumItem(
                        "",
                        "japs"),
                new LibraryPageAlbumsModel.AlbumItem(
                        "",
                        "mantras"),
                new LibraryPageAlbumsModel.AlbumItem(
                        "",
                        "japs"),
                new LibraryPageAlbumsModel.AlbumItem(
                        "",
                        "mantras")
        };

        LibraryPageAlbumsModel[] MEDIA_OBJECTS = {
                new LibraryPageAlbumsModel("Hanuman Ji's Albums",
                        new ArrayList<>(Arrays.asList(ALBUM_ITEMS))
                ),
                new LibraryPageAlbumsModel("Shiva's Albums",
                        new ArrayList<>(Arrays.asList(ALBUM_ITEMS))
                ),
                new LibraryPageAlbumsModel("Vishnu's Albums",
                        new ArrayList<>(Arrays.asList(ALBUM_ITEMS))
                ),
                new LibraryPageAlbumsModel("Brahma's Albums",
                        new ArrayList<>(Arrays.asList(ALBUM_ITEMS))
                )
        };
        return new ArrayList<>(Arrays.asList(MEDIA_OBJECTS));
    }
}