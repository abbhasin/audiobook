package com.enigma.audiobook.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.enigma.audiobook.utils.Utils.addTryCatch;
import static com.enigma.audiobook.utils.Utils.convertMSToTime;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.enigma.audiobook.R;
import com.enigma.audiobook.utils.ALog;
import com.enigma.audiobook.views.FixedVideoView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SwipeVideoCardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SwipeVideoCardFragment extends Fragment {


    private static final String TAG = "SwipeVideoCardFragment";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String THUMBNAIL = "thumbnail";
    private static final String VIDEO_URL = "videoUrl";

    private String title;
    private String description;
    private String thumbnail;
    private String videoUrl;

    private ImageView thumbnailImg;
    private FixedVideoView videoView;
    private TextView headingTxt, descriptionTxt;
    private ProgressBar progressBar;
    private TextView videoTimeLeft;

    private RequestManager requestManager;
    private Runnable runnableProgressTimeLeft;
    private Handler handlerProgressTimeLeft;

    private boolean isPaused = false;
    private int pauseAt = 0;

    public SwipeVideoCardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SwipeVideoCardFragment.
     */
    public static SwipeVideoCardFragment newInstance(String title, String description,
                                                     String thumbnail, String videoUrl) {
        SwipeVideoCardFragment fragment = new SwipeVideoCardFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(DESCRIPTION, description);
        args.putString(THUMBNAIL, thumbnail);
        args.putString(VIDEO_URL, videoUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ALog.i(TAG, "onCreate called");
        if (getArguments() != null) {
            title = getArguments().getString(TITLE);
            description = getArguments().getString(DESCRIPTION);
            thumbnail = getArguments().getString(THUMBNAIL);
            videoUrl = getArguments().getString(VIDEO_URL);
        }
        requestManager = initGlide();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        thumbnailImg = view.findViewById(R.id.cardFragmentSwipeVideoThumbnail);
        videoView = view.findViewById(R.id.cardFragmentSwipeVideoView);
        progressBar = view.findViewById(R.id.cardFragmentSwipeVideoProgressBar);
        headingTxt = view.findViewById(R.id.cardFragmentSwipeVideoHeading);
        descriptionTxt = view.findViewById(R.id.cardFragmentSwipeVideoDescription);
        videoTimeLeft = view.findViewById(R.id.cardFragmentSwipeVideoViewTimeLeft);


        headingTxt.setText(title);
        descriptionTxt.setText(description);
        requestManager
                .load(thumbnail)
                .into(thumbnailImg);

        handlerProgressTimeLeft = new Handler();
        runnableProgressTimeLeft = new Runnable() {
            @Override
            public void run() {
                if (videoView.isPlaying()) {
                    int maxDuration = videoView.getDuration();
                    int currentPos = videoView.getCurrentPosition();
                    
                    videoTimeLeft.setText(convertMSToTime(maxDuration - currentPos));
                }

                handlerProgressTimeLeft.postDelayed(runnableProgressTimeLeft, 1000);
            }
        };
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_swipe_video_card, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ALog.i(TAG, "onStart called");
        handlerProgressTimeLeft.post(runnableProgressTimeLeft);
    }

    @Override
    public void onResume() {
        super.onResume();
        ALog.i(TAG, "onResume called");
        if(isPaused) {
            progressBar.setVisibility(VISIBLE);
            isPaused = false;
            videoView.resume();
            videoView.seekTo(pauseAt);
            pauseAt = 0;
            ALog.i(TAG, "onResume videoView resume");
        } else {
            ALog.i(TAG, "onResume videoView start");
            startVideo();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        ALog.i(TAG, "onPause called");
        isPaused = true;
        pauseAt = videoView.getCurrentPosition();
        videoView.pause();

    }

    @Override
    public void onStop() {
        super.onStop();
        ALog.i(TAG, "onStop called");
        handlerProgressTimeLeft.removeCallbacks(runnableProgressTimeLeft);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ALog.i(TAG, "onDestroy called");
        resetVideoView();
    }

    private void startVideo() {
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                ALog.i(TAG, "swipe video view started playing at position");
                addTryCatch(() -> {
                    thumbnailImg.setVisibility(GONE);
                    if (progressBar != null) {
                        progressBar.setVisibility(GONE);
                    }
                    mp.setLooping(true);
                    videoView.start();
                }, TAG);
            }
        });
        progressBar.setVisibility(VISIBLE);

        videoView.setVisibility(VISIBLE);
        videoView.setVideoURI(Uri.parse(videoUrl));
    }

    private void resetVideoView() {
        ALog.i(TAG, "resetting video view at position");
        if (videoView != null) {
            addTryCatch(() -> {
                videoView.stopPlayback();
                videoView.setVisibility(GONE);
                progressBar.setVisibility(GONE);
                thumbnailImg.setVisibility(VISIBLE);
            }, TAG);
        }
    }

    private RequestManager initGlide() {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.background_card_view)
                .error(R.drawable.background_card_view);

        return Glide.with(this)
                .setDefaultRequestOptions(options);
    }

}