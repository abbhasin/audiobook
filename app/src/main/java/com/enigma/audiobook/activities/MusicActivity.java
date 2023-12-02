package com.enigma.audiobook.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

    Handler handlerSeekBarMusic;
    Runnable runnableProgressSeekBarMusic;

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
                    title = musicFile.substring(musicFile.lastIndexOf("/") + 1);

                    textViewFileNameMusic.setText(title);
                    musicLengthTotalTime.setText(convertMSToTime(0));
                    musicLengthProgress.setText(convertMSToTime(0));
                    updateButton(true);
                    musicSrv.processSong(musics.get(position));
                    isPlaying = true;
                    Log.i("MusicActivity", "next song done");

                }
            }
        });


        skipPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicBound) {
                    Log.i("MusicActivity", "prev song");
                    if (position == 0) {
                        position = musics.size() - 1;
                    } else {
                        position = (position - 1) % musics.size();
                    }

                    musicFile = musics.get(position);
                    title = musicFile.substring(musicFile.lastIndexOf("/") + 1);

                    textViewFileNameMusic.setText(title);
                    musicLengthTotalTime.setText(convertMSToTime(0));
                    musicLengthProgress.setText(convertMSToTime(0));
                    updateButton(true);
                    musicSrv.processSong(musics.get(position));
                    isPlaying = true;
                    Log.i("MusicActivity", "prev song done");
                }
            }
        });

        seekBarMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    musicSrv.seekToPosition(progress);
                    seekBarMusic.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        handlerSeekBarMusic = new Handler();
        runnableProgressSeekBarMusic = new Runnable() {

            @Override
            public void run() {
                if (musicBound && musicSrv.isTrackPlaying()) {
                    int maxDuration = musicSrv.getDuration();
                    if (maxDuration > 0) {
                        musicLengthTotalTime.setText(convertMSToTime(maxDuration));
                        seekBarMusic.setMax(maxDuration);
                    } else {
                        musicLengthTotalTime.setText(convertMSToTime(0));
                        seekBarMusic.setMax(0);
                    }

                    int currentPos = musicSrv.getCurrentPosition();
                    musicLengthProgress.setText(convertMSToTime(currentPos));
                    seekBarMusic.setProgress(currentPos);
                }

                handlerSeekBarMusic.postDelayed(runnableProgressSeekBarMusic, 1000);
            }
        };
        handlerSeekBarMusic.post(runnableProgressSeekBarMusic);

    }

    private String convertMSToTime(int ms) {
        int sec = ms / 1000;
        int mins = sec / 60;
        int secToShow = sec % 60;
        return String.format("%02d:%02d", mins, secToShow);
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
    protected void onResume() {
        super.onResume();
        updateButton(false);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (musicSrv.isTrackPlaying()) {
            musicSrv.processSong(musics.get(position));
        }
        isPlaying = false;

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
            musicSrv = binder.getService();
            musicBound = true;
            Log.i("MusicActivity", "Service connection established");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

}