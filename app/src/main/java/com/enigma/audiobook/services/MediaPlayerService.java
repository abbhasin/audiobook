package com.enigma.audiobook.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {


    private MediaPlayer player;
    private String currentSong = "noSong";
    private int pauseAt;
    private boolean songPaused;
    private final IBinder musicBind = new MusicBinder();
    private boolean isPlaying = false;


    //***************************************************************************
    // Service Callback methods
    //***************************************************************************

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }


    @Override
    public void onCreate() {

        super.onCreate();
        player = new MediaPlayer();
        initMusicPlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            stopMedia();
            player.release();
        }
        //removeAudioFocus();
    }


    public class MusicBinder extends Binder {

        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }


    //*********************************************************
    //       Public Methods to utilsed by other Components
    //**********************************************************


    public void processSong(String url) {
        if (!isSameSongRequest(url)) {
            playSong(url);
        } else {
            if (songPaused) {
                resumeSong();
            } else {
                pauseSong();
            }
        }
    }


    public boolean isTrackPlaying() {
        return isPlaying;  //player.isPlaying();
    }


    //*****************************************************
    //   My Player state methods
    //  Not exposed to Activity
    //******************************************************


    private void initMusicPlayer() {

        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);

        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        AudioAttributes.Builder builder = new AudioAttributes.Builder();
        builder.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
        builder.setUsage(AudioAttributes.USAGE_MEDIA);
        player.setAudioAttributes(builder.build());

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    private void playSong(String url) {

        isPlaying = true;
        currentSong = url;

        Uri trackUri = Uri.parse(url);
        player.reset();

        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MPS", "Error setting data source", e);
        }
        player.prepareAsync();
    }


    private void pauseSong() {
        if (!isPlaying) {
            return;
        }

        isPlaying = false;
        songPaused = true;
        player.pause();
        pauseAt = player.getCurrentPosition();
    }

    private void resumeSong() {
        if (isPlaying) {
            return;
        }

        isPlaying = true;
        songPaused = false;
        player.seekTo(pauseAt);
        player.start();
    }

    private void stopMedia() {
        if (player == null) return;
        if (player.isPlaying()) {
            player.stop();
        }
    }

    private boolean isSameSongRequest(String url) {
        return url.equals(currentSong);
    }


    //****************************************************************
    //          MusicPlayer API Callback methods
    //*****************************************************************

    @Override
    public void onCompletion(MediaPlayer mp) {

        Log.e("MPS", "onCompletion Called");
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e("MPS", "error");
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.e("MPS", "on prepared called");
        mp.start();
        Log.e("MPS", "on prepared called, started media player");
    }
}