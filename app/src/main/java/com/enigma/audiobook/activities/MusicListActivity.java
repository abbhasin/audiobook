package com.enigma.audiobook.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.enigma.audiobook.R;
import com.enigma.audiobook.utils.MusicAdapter;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class MusicListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    MusicAdapter adapter;

    List<String> songList;
    static String MEDIA_PATH = Environment.getExternalStorageDirectory().getPath() + "/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        recyclerView = findViewById(R.id.musicListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        if (ContextCompat.checkSelfPermission(MusicListActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MusicListActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            songList = getMusicFiles();
                    //getAllAudioFiles();
        }
        adapter = new MusicAdapter(songList, MusicListActivity.this);
        recyclerView.setAdapter(adapter);
    }

    private List<String> getMusicFiles(){
        return Arrays.asList("https://s3-ap-southeast-1.amazonaws.com/he-public-data/Afreen%20Afreen%20(DjRaag.Net)2cc6f8b.mp3",
                "https://s3-ap-southeast-1.amazonaws.com/he-public-data/Aik%20-%20Alif-(Mr-Jatt.com)8ae5316.mp3",
                "https://s3-ap-southeast-1.amazonaws.com/he-public-data/Tajdar%20E%20Haram-(Mr-Jatt.com)7f311d4.mp3");
    }

    private void getAllAudioFiles() {
        if (MEDIA_PATH != null) {
            File mainFile = new File(MEDIA_PATH);
            File[] files = mainFile.listFiles();
            Queue<File> queue = new LinkedBlockingQueue<>();
            queue.addAll(Arrays.asList(files));

            while (!queue.isEmpty()) {
                File file = queue.poll();
                Log.i("MusicListActivity", "found file:" + file.toString());
                if (file.isDirectory()) {
                    queue.addAll(Arrays.asList(file.listFiles()));
                    continue;
                } else {
                    String path = file.getAbsolutePath();
                    if (path.endsWith(".mp3")) {

                        songList.add(path);
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            songList = getMusicFiles();
            //getAllAudioFiles();
        }
    }
}