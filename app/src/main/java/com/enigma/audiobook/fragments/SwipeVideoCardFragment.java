package com.enigma.audiobook.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.enigma.audiobook.activities.MandirPageActivity.MANDIR_ID_KEY;
import static com.enigma.audiobook.utils.Utils.addTryCatch;
import static com.enigma.audiobook.utils.Utils.convertMSToTime;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.RequestManager;
import com.enigma.audiobook.R;
import com.enigma.audiobook.activities.MandirPageActivity;
import com.enigma.audiobook.utils.ALog;
import com.enigma.audiobook.utils.SharedPreferencesHandler;
import com.enigma.audiobook.utils.Utils;
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
    private static final String GOD_ID = "godId";
    private static final String MANDIR_ID = "mandirId";
    private static final String DARSHAN_ID = "darshanId";

    private String title;
    private String description;
    private String thumbnail;
    private String videoUrl;
    private String godId;
    private String mandirId;
    private String darshanId;
    private String userId;


    private ImageView thumbnailImg;
    private FixedVideoView videoView;
    private TextView headingTxt, descriptionTxt;
    private ProgressBar progressBar;
    private TextView videoTimeLeft;
    private Button mandirInfoBtn;

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
                                                     String thumbnail, String videoUrl,
                                                     String godId, String mandirId,
                                                     String darshanId) {
        SwipeVideoCardFragment fragment = new SwipeVideoCardFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(DESCRIPTION, description);
        args.putString(THUMBNAIL, thumbnail);
        args.putString(VIDEO_URL, videoUrl);
        args.putString(GOD_ID, godId);
        args.putString(MANDIR_ID, mandirId);
        args.putString(DARSHAN_ID, darshanId);
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
            godId = getArguments().getString(GOD_ID);
            mandirId = getArguments().getString(MANDIR_ID);
            darshanId = getArguments().getString(DARSHAN_ID);
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
        mandirInfoBtn = view.findViewById(R.id.cardFragmentSwipeVideoButtonMandirInfo);

        userId = SharedPreferencesHandler.getUserId(getContext()).get();

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

        mandirInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ALog.i(TAG, "calling mandir page activity");
                Intent i = new Intent(getContext(), MandirPageActivity.class);
                i.putExtra(MANDIR_ID_KEY, mandirId);
                requireContext().startActivity(i);
            }
        });
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
        if (isPaused) {
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
        return Utils.initGlide(this.getContext());
    }

}