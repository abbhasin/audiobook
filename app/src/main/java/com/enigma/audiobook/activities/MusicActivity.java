package com.enigma.audiobook.activities;

import static com.enigma.audiobook.utils.Utils.convertMSToTime;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.enigma.audiobook.R;
import com.enigma.audiobook.services.MediaPlayerService;
import com.enigma.audiobook.utils.ALog;

import java.util.Arrays;
import java.util.List;


/**
 * TODO:
 * onError of MediaPlayer causes skipNext infinite loop with seekbar at the end
 * onPause/Play getting unsynchronized with the actual media playing when came back from back activity
 */
public class MusicActivity extends AppCompatActivity implements MediaPlayerService.MediaCallbackListener {

    Button skipPrevious, playPause, skipNext;
    TextView musicLengthProgress, musicLengthTotalTime, textViewFileNameMusic;
    SeekBar seekBarVolume, seekBarMusic;

    String title, musicFile;
    int currentPosition;

    List<String> musics;

    MediaPlayerService musicSrv;
    boolean musicBound = false;

    Intent playIntent = null;
    boolean isPlaying = false;

    boolean isBackPressed = false;

    boolean isOnError = false;

    Handler handlerSeekBarMusic;
    Runnable runnableProgressSeekBarMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        skipPrevious = findViewById(R.id.buttonPrevious);
        playPause = findViewById(R.id.buttonPlayPause);
        skipNext = findViewById(R.id.buttonNext);
        musicLengthProgress = findViewById(R.id.musicLengthProgress);
        musicLengthTotalTime = findViewById(R.id.musicLengthTotalTime);
//        seekBarVolume = findViewById(R.id.volumeSeekBar);
        seekBarMusic = findViewById(R.id.musicSeekBar);

        textViewFileNameMusic = findViewById(R.id.textViewFileNameMusic);


        Intent i = getIntent();
        title = i.getStringExtra("title");
        musicFile = i.getStringExtra("filePath");
        currentPosition = i.getIntExtra("position", 0);
        isPlaying = i.getBooleanExtra("isPlaying", false);
        updateButton(isPlaying);
        musics = Arrays.asList(i.getStringArrayExtra("musics"));

        textViewFileNameMusic.setText(title);

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicBound) {
                    if(musicSrv.isTrackPreparing()) {
                        Toast.makeText(MusicActivity.this,
                                "preparing song, button deselected", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ALog.i("MusicActivity", "trying playing song");
                    isPlaying = !isPlaying;
                    updateButton(isPlaying);
                    musicSrv.processSong(musics.get(currentPosition));
                    ALog.i("MusicActivity", "song is playing");
                }
            }
        });

        skipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicBound) {
                    ALog.i("MusicActivity", "next song");
                    currentPosition = (currentPosition + 1) % musics.size();
                    musicFile = musics.get(currentPosition);
                    title = musicFile.substring(musicFile.lastIndexOf("/") + 1);

                    textViewFileNameMusic.setText(title);
                    musicLengthTotalTime.setText(convertMSToTime(0));
                    musicLengthProgress.setText(convertMSToTime(0));
                    updateButton(true);
                    musicSrv.processSong(musics.get(currentPosition));
                    isPlaying = true;
                    ALog.i("MusicActivity", "next song done");

                }
            }
        });


        skipPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicBound) {
                    ALog.i("MusicActivity", "prev song");
                    if (currentPosition == 0) {
                        currentPosition = musics.size() - 1;
                    } else {
                        currentPosition = (currentPosition - 1) % musics.size();
                    }

                    musicFile = musics.get(currentPosition);
                    title = musicFile.substring(musicFile.lastIndexOf("/") + 1);

                    textViewFileNameMusic.setText(title);
                    musicLengthTotalTime.setText(convertMSToTime(0));
                    musicLengthProgress.setText(convertMSToTime(0));
                    updateButton(true);
                    musicSrv.processSong(musics.get(currentPosition));
                    isPlaying = true;
                    ALog.i("MusicActivity", "prev song done");
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
                if (musicBound && musicSrv.isPlaying()) {
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

    }

    private void updateButton(boolean toPlay) {
        if (toPlay) {
            playPause.setBackgroundResource(R.drawable.pause_circle);
        } else {
            playPause.setBackgroundResource(R.drawable.play_circle);
        }
    }

    @Override
    protected void onRestart() {
        ALog.i("MusicActivity", "onRestart called");
        super.onRestart();
    }

    @Override
    protected void onStart() {
        isBackPressed = false;
        ALog.i("MusicActivity", "onStart called");
        if (playIntent == null) {
            playIntent = new Intent(this, MediaPlayerService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
            Log.e("MusicActivity", "music service bounded");
        }

        handlerSeekBarMusic.post(runnableProgressSeekBarMusic);

//        RuntimePermissionUtility.checkExternalStoragePermission(MusicListActivity.this);
        super.onStart();
    }

    @Override
    protected void onResume() {
        ALog.i("MusicActivity", "onResume called");
        //        updateButton(false);
        super.onResume();
    }

    @Override
    protected void onPause() {
        ALog.i("MusicActivity", "onPause called");
        if(!isBackPressed) {
            ALog.i("MusicActivity", "Pausing music");
            if (musicSrv.isTrackSelected()) {
                musicSrv.processSong(musics.get(currentPosition));
            }
            isPlaying = false;
            updateButton(false);
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        ALog.i("MusicActivity", "onStop called");
        handlerSeekBarMusic.removeCallbacks(runnableProgressSeekBarMusic);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        ALog.i("MusicActivity", "onDestroy called");
        musicSrv.unregisterCallback(this);
        if (!isBackPressed) {
            // we do not stop the music service to allow it be running with MusicListActivity
            stopService(playIntent);
            musicSrv.stopSelf();
        }
        musicSrv = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        ALog.i("MusicActivity", "onBackPressed called");
        isBackPressed = true;
        Intent intent = new Intent();
        intent.putExtra("position", currentPosition);
        intent.putExtra("isPlaying", isPlaying);
        setResult(Activity.RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }

    //***************************************************************
    // Music Service part
    //***************************************************************


    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.MusicBinder binder = (MediaPlayerService.MusicBinder) service;
            musicSrv = binder.getService();
//            isPlaying = musicSrv.isTrackSelected();
//            updateButton(isPlaying);
            musicBound = true;
            musicSrv.registerCallback(MusicActivity.this);


            ALog.i("MusicActivity", "Service connection established");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public void onMediaPlayStart(MediaPlayer mp) {

    }

    @Override
    public void onTrackCompletion(MediaPlayer mp) {
        ALog.i("MusicActivity", "OnTrackCompletion called");
        if(isOnError) {
            isOnError = false;
            ALog.i("MusicActivity", "OnTrackCompletion, not doing next since error occurred");
        } else {
            skipNext.callOnClick();
        }


    }

    @Override
    public void onError() {
        ALog.i("MusicActivity", "onError called");
        isOnError = true;
        isPlaying = false;
        updateButton(isPlaying);
        musicSrv.stopMedia();
    }
}