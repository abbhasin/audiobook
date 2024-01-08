package com.enigma.audiobook.recyclers.controllers;

import static com.enigma.audiobook.utils.Utils.addTryCatch;
import static com.enigma.audiobook.utils.Utils.convertMSToTime;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.enigma.audiobook.R;
import com.enigma.audiobook.services.MediaPlayerService;
import com.enigma.audiobook.utils.ALog;

public class PlayableMusicViewController {

    private static final String TAG = "PlayableMusicViewController";
    private Context context;
    private Button musicPlayPauseBtn;
    private SeekBar musicSeekBar;
    private TextView musicLengthTotalTime, musicLengthProgressTime;
    private MediaPlayerService musicSrv;

    Handler handlerSeekBarMusic;
    Runnable runnableProgressSeekBarMusic;
    private boolean musicBound = false;
    boolean isMusicPlaying = false;

    View.OnClickListener onClickListenerOnReset;

    public void init(Context context_, boolean musicBound_, MediaPlayerService musicSrv_) {
        musicBound = musicBound_;
        musicSrv = musicSrv_;
        context = context_;
        handlerSeekBarMusic = new Handler();
    }

    public void playMusic(Button musicPlayPauseBtn_, SeekBar musicSeekBar_,
                          TextView musicLengthTotalTime_, TextView musicLengthProgressTime_,
                          String musicUrl_) {

        playMusic(musicPlayPauseBtn_, musicSeekBar_, musicLengthTotalTime_,
                musicLengthProgressTime_, musicUrl_, null);
    }

    public void playMusic(Button musicPlayPauseBtn_, SeekBar musicSeekBar_,
                          TextView musicLengthTotalTime_, TextView musicLengthProgressTime_,
                          String musicUrl_, View.OnClickListener onClickListenerOnReset_) {
        onClickListenerOnReset = onClickListenerOnReset_;
        musicPlayPauseBtn = musicPlayPauseBtn_;
        musicSeekBar = musicSeekBar_;
        musicLengthProgressTime = musicLengthProgressTime_;
        musicLengthTotalTime = musicLengthTotalTime_;

        musicPlayPauseBtn.setClickable(true);
        musicSeekBar.setClickable(true);
        musicPlayPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicBound) {
                    if (musicSrv.isTrackPreparing()) {
                        Toast.makeText(context,
                                "preparing song, button deselected", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ALog.i(TAG, "trying playing song");
                    isMusicPlaying = !isMusicPlaying;
                    updateButton(isMusicPlaying);
                    musicSrv.processSong(musicUrl_);
                    ALog.i(TAG, "song is playing");
                }
            }
        });

        musicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    musicSrv.seekToPosition(progress);
                    musicSeekBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        runnableProgressSeekBarMusic = new Runnable() {
            @Override
            public void run() {
                if (musicBound && musicSrv.isPlaying()) {
                    int maxDuration = musicSrv.getDuration();
                    if (maxDuration > 0) {
                        musicLengthTotalTime.setText(convertMSToTime(maxDuration));
                        musicSeekBar.setMax(maxDuration);
                    } else {
                        musicLengthTotalTime.setText(convertMSToTime(0));
                        musicSeekBar.setMax(0);
                    }

                    int currentPos = musicSrv.getCurrentPosition();
                    musicLengthProgressTime.setText(convertMSToTime(currentPos));
                    musicSeekBar.setProgress(currentPos);
                }

                handlerSeekBarMusic.postDelayed(runnableProgressSeekBarMusic, 1000);
            }
        };
        handlerSeekBarMusic.post(runnableProgressSeekBarMusic);
        musicPlayPauseBtn.callOnClick();

    }

    public void resetMusicFeed() {
        resetMusicFeed(true);
    }

    public void resetMusicFeed(boolean resetClickables) {
        if (musicPlayPauseBtn != null) {
            addTryCatch(() -> {
                isMusicPlaying = false;
                updateButton(isMusicPlaying);
                if (onClickListenerOnReset != null) {
                    musicPlayPauseBtn.setOnClickListener(onClickListenerOnReset);
                } else if (resetClickables) {
                    musicPlayPauseBtn.setClickable(false);
                    musicSeekBar.setClickable(false);
                }

                musicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                musicSrv.stopMedia();
                musicSrv.reset();
                musicPlayPauseBtn = null;
                musicSeekBar = null;
                musicLengthProgressTime = null;
                musicLengthTotalTime = null;
                handlerSeekBarMusic.removeCallbacks(runnableProgressSeekBarMusic);
                runnableProgressSeekBarMusic = null;
                onClickListenerOnReset = null;
            }, TAG);
        }
    }

    private void updateButton(boolean toPlay) {
        if (toPlay) {
            musicPlayPauseBtn.setBackgroundResource(R.drawable.pause_circle);
        } else {
            musicPlayPauseBtn.setBackgroundResource(R.drawable.play_circle);
        }
    }
}
