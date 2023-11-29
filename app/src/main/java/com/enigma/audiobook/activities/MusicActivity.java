package com.enigma.audiobook.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.enigma.audiobook.R;
import com.enigma.audiobook.services.MediaPlayerService;

import java.util.Arrays;
import java.util.List;

public class MusicActivity extends AppCompatActivity {

    Button skipPrevious, playPause, skipNext;
    TextView musicLengthProgress, musicLengthTotalTime, textViewFileNameMusic;
    SeekBar seekBarVolume, seekBarMusic;

    String title, musicFile;
    int position;

    List<String> musics;

    MediaPlayerService musicSrv;
    boolean musicBound = false;

    Intent playIntent = null;
    boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        skipPrevious = findViewById(R.id.buttonPrevious);
        playPause = findViewById(R.id.buttonPlayPause);
        skipNext = findViewById(R.id.buttonNext);
        musicLengthProgress = findViewById(R.id.musicLengthProgress);
        musicLengthTotalTime = findViewById(R.id.musicLengthTotalTime);
        seekBarVolume = findViewById(R.id.volumeSeekBar);
        seekBarMusic = findViewById(R.id.musicSeekBar);

        textViewFileNameMusic = findViewById(R.id.textViewFileNameMusic);


        Intent i = getIntent();
        title = i.getStringExtra("title");
        musicFile = i.getStringExtra("filePath");
        position = i.getIntExtra("position", 0);
        musics = Arrays.asList(i.getStringArrayExtra("musics"));

        textViewFileNameMusic.setText(title);

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicBound) {
                    Log.i("MusicActivity", "trying playing song");
                    isPlaying = !isPlaying;
                    updateButton(isPlaying);
                    musicSrv.processSong(musics.get(position));
                    Log.i("MusicActivity", "song is playing");
                }
            }
        });

        skipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicBound) {
                    Log.i("MusicActivity", "next song");
                    position = (position + 1) % musics.size();
                    musicFile = musics.get(position);
                    title = musicFile.substring(musicFile.lastIndexOf("/"));

                    textViewFileNameMusic.setText(title);
                    updateButton(true);
                    musicSrv.processSong(musics.get(position));
                    Log.i("MusicActivity", "next song done");

                }
            }
        });

    }

    private void updateButton(boolean toPlay) {
        if (toPlay) {
            playPause.setBackgroundResource(R.drawable.pause_circle);
        } else {
            playPause.setBackgroundResource(R.drawable.play_circle);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MediaPlayerService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
            Log.e("MusicActivity", "music service bounded");
        }

//        RuntimePermissionUtility.checkExternalStoragePermission(MusicListActivity.this);
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv.stopSelf();
        musicSrv = null;
        super.onDestroy();
    }

    //***************************************************************
    // Music Service part
    //***************************************************************


    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.MusicBinder binder = (MediaPlayerService.MusicBinder) service;
            //get service
            musicSrv = binder.getService();
            //pass list
            //musicSrv.setList(new ArrayList<Music>());
            musicBound = true;
            Log.i("MusicActivity", "Service connection established");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

}