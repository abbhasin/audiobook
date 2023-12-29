package com.enigma.audiobook.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.enigma.audiobook.R;
import com.enigma.audiobook.services.MediaPlayerService;
import com.enigma.audiobook.utils.ALog;
import com.enigma.audiobook.adapters.MusicAdapter;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * TODO:
 * onTrackCompletion
 * onDestory from MusicActivity
 */
public class MusicListActivity extends AppCompatActivity implements MusicAdapter.CardTouchListener,
        MediaPlayerService.MediaCallbackListener {

    private RecyclerView recyclerView;
    TextView musicFileNameBottom;
    Button playPause;

    ConstraintLayout musicBottomSection;

    MusicAdapter adapter;

    List<String> songList;
    int currentSongPosition;
    boolean isPlaying;
    Intent playIntent = null;


    MediaPlayerService musicSrv;
    boolean musicBound = false;

    boolean isLaunchingMusicActivity = false;

    boolean isOnError = false;

    Handler handlerProgressBarMusic;
    Runnable runnableProgressBarMusic;

    ProgressBar progressBar;

    ActivityResultLauncher<Intent> musicActivityResultLauncher;

    static int LAUNCH_MUSIC_ACTIVITY_REQ = 1;

    static String MEDIA_PATH = Environment.getExternalStorageDirectory().getPath() + "/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        recyclerView = findViewById(R.id.musicListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        musicFileNameBottom = findViewById(R.id.textViewFileNameMusicBottomSection);
        playPause = findViewById(R.id.buttonPlayPauseBottomSection);
        progressBar = findViewById(R.id.progressBarBottomSection);
        musicBottomSection = findViewById(R.id.musicBottomSection);
        currentSongPosition = 0;


        if (ContextCompat.checkSelfPermission(MusicListActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MusicListActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            songList = getMusicFiles();
            String musicFile = songList.get(currentSongPosition);
            String title = musicFile.substring(musicFile.lastIndexOf("/")+1);
            musicFileNameBottom.setText(title);

            //getAllAudioFiles();
        }
        adapter = new MusicAdapter(songList, MusicListActivity.this);
        recyclerView.setAdapter(adapter);

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicBound) {
                    if(musicSrv.isTrackPreparing()) {
                        Toast.makeText(MusicListActivity.this,
                                "preparing song, button deselected", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ALog.i("MusicListActivity", "trying playing song");
                    isPlaying = !isPlaying;
                    updateButton(isPlaying);
                    musicSrv.processSong(songList.get(currentSongPosition));
                    ALog.i("MusicListActivity", "song is playing");
                }
            }
        });

        musicActivityResultLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        new ActivityResultCallback<ActivityResult>() {
                            @Override
                            public void onActivityResult(ActivityResult result) {
                                if (result.getResultCode() == Activity.RESULT_OK) {
                                    Intent i = result.getData();
                                    int position = i.getIntExtra("position", 0);
                                    isPlaying = i.getBooleanExtra("isPlaying", false);
                                    currentSongPosition = position;
                                    String musicFile = songList.get(currentSongPosition);
                                    String title = musicFile.substring(musicFile.lastIndexOf("/") + 1);
                                    musicFileNameBottom.setText(title);
                                    updateButton(isPlaying);
                                } else {
                                    musicSrv.stopMedia();
                                    musicSrv.reset();
                                    currentSongPosition = 0;
                                    isPlaying = false;
                                    updateButton(isPlaying);
                                }
                            }
                        });

        musicBottomSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MusicListActivity.this, MusicActivity.class);
                String musicFile = songList.get(currentSongPosition);
                String title = musicFile.substring(musicFile.lastIndexOf("/") + 1);
                i.putExtra("title", title);
                i.putExtra("filePath", musicFile);
                i.putExtra("position", currentSongPosition);
                i.putExtra("musics", songList.toArray(new String[]{}));
                i.putExtra("isPlaying", isPlaying);
                isLaunchingMusicActivity = true;
                musicActivityResultLauncher.launch(i);
            }
        });

        handlerProgressBarMusic = new Handler();
        runnableProgressBarMusic = new Runnable() {

            @Override
            public void run() {
                if (musicBound && musicSrv.isPlaying()) {
                    int maxDuration = musicSrv.getDuration();
                    if (maxDuration > 0) {
                        progressBar.setMax(maxDuration);
                    } else {
                        progressBar.setMax(0);
                    }

                    int currentPos = musicSrv.getCurrentPosition();
                    progressBar.setProgress(currentPos);
                }

                handlerProgressBarMusic.postDelayed(runnableProgressBarMusic, 1000);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MediaPlayerService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
            Log.e("MusicListActivity", "music service bounded");
        }

        handlerProgressBarMusic.post(runnableProgressBarMusic);
//        RuntimePermissionUtility.checkExternalStoragePermission(MusicListActivity.this);
    }

    @Override
    protected void onResume() {
        isLaunchingMusicActivity = false;
        super.onResume();
    }

    @Override
    protected void onPause() {
        ALog.i("MusicListActivity", "onPause called");
        if(!isLaunchingMusicActivity) {
            ALog.i("MusicListActivity", "pausing");
            if (musicSrv.isTrackSelected()) {
                musicSrv.processSong(songList.get(currentSongPosition));
            }
            isPlaying = false;
            updateButton(false);
        } else {
            ALog.i("MusicListActivity", "onPause called, not pausing");
        }

        super.onPause();
    }

    @Override
    protected void onStop() {
        // remove progress bar handler
        ALog.i("MusicListActivity", "onStop called");
        handlerProgressBarMusic.removeCallbacks(runnableProgressBarMusic);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        ALog.i("MusicListActivity", "onDestroy called");
        musicSrv.unregisterCallback(this);
        stopService(playIntent);
        musicSrv.stopSelf();
        musicSrv = null;
        super.onDestroy();
    }

    private List<String> getMusicFiles() {
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
                ALog.i("MusicListActivity", "found file:" + file.toString());
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


    @Override
    public void itemTouch(String musicFile, int position) {
        assert musicFile.equals(songList.get(position));
        playSong(position);
    }

    private void playSong(int position) {
        ALog.i("MusicListActivity", "trying playing song:" + songList.get(position));
        if (musicBound) {
            if (currentSongPosition == position && isPlaying) {
                musicSrv.seekToPosition(0);
            } else {
                currentSongPosition = position;
                String musicFile = songList.get(position);
                String title = musicFile.substring(musicFile.lastIndexOf("/") + 1);
                musicFileNameBottom.setText(title);
                isPlaying = true;
                updateButton(isPlaying);
                musicSrv.processSong(songList.get(position));
            }
            ALog.i("MusicListActivity", "song is playing:" + songList.get(position));
        }
    }

    private void updateButton(boolean toPlay) {
        if (toPlay) {
            playPause.setBackgroundResource(R.drawable.pause_circle);
        } else {
            playPause.setBackgroundResource(R.drawable.play_circle);
        }
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
            musicSrv.registerCallback(MusicListActivity.this);

            ALog.i("MusicListActivity", "Service connection established");
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
        ALog.i("MusicListActivity", "onTrackCompletion called");
        if(isLaunchingMusicActivity) {
            return;
        }

        if(isOnError) {
            ALog.i("MusicListActivity", "onTrackCompletion, not calling next since error occurred");
            isOnError = false;
            return;
        }
        int position = (currentSongPosition + 1) % songList.size();
        playSong(position);
    }

    @Override
    public void onError() {
        ALog.i("MusicListActivity", "onTrackCompletion called");
        isOnError = true;
        isPlaying = false;
        updateButton(isPlaying);
        musicSrv.stopMedia();
        musicSrv.reset();
    }
}