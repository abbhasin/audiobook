package com.enigma.audiobook.services;

import static android.media.MediaPlayer.SEEK_CLOSEST;

import static com.enigma.audiobook.services.MediaPlayerService.PlayerState.NOT_PLAYING;
import static com.enigma.audiobook.services.MediaPlayerService.PlayerState.PLAYING;
import static com.enigma.audiobook.services.MediaPlayerService.PlayerState.PREPARED;
import static com.enigma.audiobook.services.MediaPlayerService.PlayerState.PREPARING;

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

import com.enigma.audiobook.utils.ALog;

import java.util.HashSet;
import java.util.Set;

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {


    private MediaPlayer player;
    private String currentSong = "noSong";
    private int pauseAt;
    private boolean songPaused;
    private final IBinder musicBind = new MusicBinder();
    private boolean isPlaying = false;
    
    private PlayerState playerState = NOT_PLAYING;

    private Set<MediaCallbackListener> callbacks;


    //***************************************************************************
    // Service Callback methods
    //***************************************************************************

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    /**
     * Note: We are not releasing the media player resources on service unbinding.
     * We should call stopService(playIntent) on the service to invoke the onDestroy
     * method which will stop the playback and release the resources.
     *
     * @param intent The Intent that was used to bind to this service,
     * as given to {@link android.content.Context#bindService
     * Context.bindService}.  Note that any extras that were included with
     * the Intent at that point will <em>not</em> be seen here.
     *
     * @return
     */
    @Override
    public boolean onUnbind(Intent intent) {
//        player.stop();
//        player.release();
        return false;
    }


    @Override
    public void onCreate() {

        super.onCreate();
        player = new MediaPlayer();
        initMusicPlayer();
        callbacks = new HashSet<>();
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            stopMedia();
            player.release();
        }
        //removeAudioFocus();
        super.onDestroy();
    }


    public class MusicBinder extends Binder {

        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }


    //*********************************************************
    //       Public Methods to utilsed by other Components
    //**********************************************************
    enum PlayerState {
        NOT_PLAYING(false),
        PREPARING(true),
        PREPARED(true),
        PLAYING(true);

        private boolean isTrackSelected;
        PlayerState(boolean trackSelected) {
            this.isTrackSelected = trackSelected;
        }

        public boolean isTrackSelected(){
            return isTrackSelected;
        }
    }

    public void processSong(String url) {
        if (!isSameSongRequest(url)) {
            playSong(url);
        } else {
            if(playerState == PLAYING) {
                pauseSong();
            } else {
                resumeSong();
            }
//            if (songPaused) {
//            if (!isPlaying) {
//                resumeSong();
//            } else {
//                pauseSong();
//            }
        }
    }
    
   


    public boolean isTrackSelected() {
        //return isPlaying;  //player.isPlaying();
        return playerState.isTrackSelected;
    }
    
    public boolean isTrackPreparing() {
        return playerState == PREPARING;
    }

    public boolean isPlaying(){
        return playerState == PLAYING;
    }

    public void seekToPosition(int positionMS) {
        if(isTrackSelected() && player.isPlaying()) {
            player.seekTo(positionMS, SEEK_CLOSEST);
        }
    }

    public int getDuration() {
        if(isTrackSelected() && player.isPlaying()) {
            return player.getDuration();
        }
        return -11;
    }

    public int getCurrentPosition() {
        if(isTrackSelected() && player.isPlaying()) {
            return player.getCurrentPosition();
        }
        return 0;
    }

    public void registerCallback(MediaCallbackListener callback){
        callbacks.add(callback);
    }

    public void unregisterCallback(MediaCallbackListener callback) {
        callbacks.remove(callback);
    }

    public interface MediaCallbackListener {
        void onTrackCompletion();
        void onError();
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
        ALog.i("MPS", "playing song:" + url);
        isPlaying = true;
        
//        songPaused = false;
        currentSong = url;

        Uri trackUri = Uri.parse(url);
        player.reset();

        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            ALog.e("MPS", "Error setting data source", e);
        }
        playerState = PREPARING;
        player.prepareAsync();
    }


    private void pauseSong() {
//        if (!isPlaying) {
//            return;
//        }

        if (playerState != PLAYING) {
            return;
        }

        isPlaying = false;
        playerState = PREPARED;
//        songPaused = true;
        player.pause();
        pauseAt = player.getCurrentPosition();
    }

    private void resumeSong() {
//        if (isPlaying) {
//            return;
//        }
        if (playerState != PREPARED) {
            return;
        }

        isPlaying = true;
//        songPaused = false;
        playerState = PLAYING;
        player.seekTo(pauseAt);
        player.start();
    }

    public void stopMedia() {
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

        ALog.i("MPS", "onCompletion Called");
        for (MediaCallbackListener callback : callbacks) {
            callback.onTrackCompletion();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        ALog.i("MPS", "error");
        for (MediaCallbackListener callback : callbacks) {
            callback.onError();
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        ALog.i("MPS", "on prepared called");
        playerState = PREPARED;
        mp.start();
        playerState = PLAYING;
        ALog.i("MPS", "on prepared called, started media player");
    }
}