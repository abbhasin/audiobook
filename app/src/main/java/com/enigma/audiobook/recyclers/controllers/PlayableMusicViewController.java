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
import com.enigma.audiobook.backend.models.ContentUploadStatus;
import com.enigma.audiobook.proxies.ViewsService;
import com.enigma.audiobook.services.MediaPlayerService;
import com.enigma.audiobook.utils.ALog;
import com.enigma.audiobook.utils.RetryHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlayableMusicViewController {

    private static final String TAG = "PlayableMusicViewController";
    private static final int BASE_VIEW_HEARTBEAT_TIME_SEC = 5;
    static String fromUserId;
    private Context context;
    private Button musicPlayPauseBtn;
    private SeekBar musicSeekBar;
    private TextView musicLengthTotalTime, musicLengthProgressTime;
    private MediaPlayerService musicSrv;

    Handler handlerSeekBarMusic;
    Runnable runnableProgressSeekBarMusic;
    Handler handlerViewDuration;
    Runnable runnableViewDuration;
    int viewDurationCount = 0;
    private boolean musicBound = false;
    boolean isMusicPlaying = false;

    View.OnClickListener onClickListenerOnReset;

    String id;
    ContentUploadStatus contentUploadStatus;
    ViewsService viewsService;

    public void init(Context context_, boolean musicBound_, MediaPlayerService musicSrv_,
                     ViewsService viewsService) {
        musicBound = musicBound_;
        musicSrv = musicSrv_;
        context = context_;
        this.viewsService = viewsService;
        handlerSeekBarMusic = new Handler();
        handlerViewDuration = new Handler();
    }

    public static void setFromUserId(String userId) {
        fromUserId = userId;
    }

    public void playMusic(Button musicPlayPauseBtn_, SeekBar musicSeekBar_,
                          TextView musicLengthTotalTime_, TextView musicLengthProgressTime_,
                          String musicUrl_, String id, ContentUploadStatus contentUploadStatus) {
        this.id = id;
        this.contentUploadStatus = contentUploadStatus;
        playMusicInternal(musicPlayPauseBtn_, musicSeekBar_, musicLengthTotalTime_,
                musicLengthProgressTime_, musicUrl_, null);
    }

    public void playMusic(Button musicPlayPauseBtn_, SeekBar musicSeekBar_,
                          TextView musicLengthTotalTime_, TextView musicLengthProgressTime_,
                          String musicUrl_, View.OnClickListener onClickListenerOnReset_) {
        this.id = null;
        this.contentUploadStatus = null;
        playMusicInternal(musicPlayPauseBtn_, musicSeekBar_, musicLengthTotalTime_,
                musicLengthProgressTime_, musicUrl_, onClickListenerOnReset_);
    }

    public void playMusicInternal(Button musicPlayPauseBtn_, SeekBar musicSeekBar_,
                                  TextView musicLengthTotalTime_, TextView musicLengthProgressTime_,
                                  String musicUrl_, View.OnClickListener onClickListenerOnReset_) {
        resetMusicFeedInternal(true, false);
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
                addTryCatch(() -> {
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
                }, TAG);
            }
        });

        musicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    addTryCatch(() -> {
                        musicSrv.seekToPosition(progress);
                        musicSeekBar.setProgress(progress);
                    }, TAG);
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
                addTryCatch(() -> {
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
                }, TAG);
            }
        };
        handlerSeekBarMusic.post(runnableProgressSeekBarMusic);

        runnableViewDuration = new Runnable() {
            @Override
            public void run() {
                addTryCatch(() -> {
                    if (musicBound && musicSrv.isPlaying()) {
                        int maxDuration = musicSrv.getDuration();
                        if (maxDuration > 0) {
                            addViewing(maxDuration,
                                    viewDurationCount * BASE_VIEW_HEARTBEAT_TIME_SEC * 1000);
                            viewDurationCount++;
                        }
                    }
                    handlerViewDuration.postDelayed(runnableViewDuration,
                            BASE_VIEW_HEARTBEAT_TIME_SEC * 1000L);
                }, TAG);

            }
        };
        handlerViewDuration.post(runnableViewDuration);

        musicPlayPauseBtn.callOnClick();

    }

    private void addViewing(int maxDurationMS, int viewDurationMS) {
        addTryCatch(() -> {
            if (id != null && maxDurationMS > 0) {
                com.enigma.audiobook.backend.models.View view =
                        new com.enigma.audiobook.backend.models.View();
                view.setPostId(this.id);
                view.setUserId(this.fromUserId);
                view.setViewDurationSec(viewDurationMS / 1000);
                view.setTotalLengthSec(maxDurationMS / 1000);
                Call<Void> call = viewsService.addViewing(view);
                RetryHelper.enqueueWithRetry(call, 1, new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!response.isSuccessful()) {
                            ALog.i(TAG, "ERROR: unable to post viewing:" + response);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        ALog.e(TAG, "unable to post viewing:", t);
                    }
                });
            }
        }, TAG);
    }

    private void addViewing() {

    }

    public void resetMusicFeed() {
        resetMusicFeedInternal(true, true);
    }

    public void resetMusicFeedInternal(boolean resetClickables, boolean resetPostBasedVars) {
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

                addTryCatch(() -> {
                    musicSrv.stopMedia();
                    musicSrv.reset();
                }, TAG);

                musicPlayPauseBtn = null;
                musicSeekBar = null;
                musicLengthProgressTime = null;
                musicLengthTotalTime = null;
                handlerSeekBarMusic.removeCallbacks(runnableProgressSeekBarMusic);
                runnableProgressSeekBarMusic = null;
                handlerViewDuration.removeCallbacks(runnableViewDuration);
                viewDurationCount = 0;
                runnableViewDuration = null;
                onClickListenerOnReset = null;
                if (resetPostBasedVars) {
                    id = null;
                    contentUploadStatus = null;
                }
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
