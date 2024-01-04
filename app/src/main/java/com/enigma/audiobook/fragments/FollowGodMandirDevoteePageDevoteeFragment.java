package com.enigma.audiobook.fragments;

import static com.enigma.audiobook.utils.Utils.initGlide;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.enigma.audiobook.R;
import com.enigma.audiobook.adapters.FollowGodMandirDevoteePageDevoteeRVAdapter;
import com.enigma.audiobook.models.FollowGodMandirDevoteePageDevoteeItemModel;
import com.enigma.audiobook.utils.ALog;
import com.enigma.audiobook.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FollowGodMandirDevoteePageDevoteeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FollowGodMandirDevoteePageDevoteeFragment extends Fragment {
    private static final String TAG = "FollowGodMandirDevoteePageDevoteeFragment";
    private RecyclerView recyclerView;
    private RequestManager requestManager;

    public FollowGodMandirDevoteePageDevoteeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     */
    public static FollowGodMandirDevoteePageDevoteeFragment newInstance() {
        FollowGodMandirDevoteePageDevoteeFragment fragment = new FollowGodMandirDevoteePageDevoteeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ALog.i(TAG, "onCreate called");
        if (getArguments() != null) {
        }
        requestManager = Utils.initGlide(getContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.fragmentFollowGodMandirAndDevotees_DevoteesRV);
        initRecyclerView();
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        List<FollowGodMandirDevoteePageDevoteeItemModel> mediaObjects = getMediaObjects();

        FollowGodMandirDevoteePageDevoteeRVAdapter adapter = new FollowGodMandirDevoteePageDevoteeRVAdapter(initGlide(getContext()), mediaObjects);
        recyclerView.setAdapter(adapter);
    }

    private List<FollowGodMandirDevoteePageDevoteeItemModel> getMediaObjects() {
        FollowGodMandirDevoteePageDevoteeItemModel[] MEDIA_OBJECTS = {
                new FollowGodMandirDevoteePageDevoteeItemModel("Ashok Acharya", false,
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        97
                ),
                new FollowGodMandirDevoteePageDevoteeItemModel("Anil Shaktimaan", false,
                        "https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg",
                        83
                ),
                new FollowGodMandirDevoteePageDevoteeItemModel("Prerna Kerlaiya", false,
                        "https://mixkit.imgix.net/videos/preview/mixkit-young-man-at-the-bowling-center-makes-a-shot-49114-0.jpg",
                        79
                ),
                new FollowGodMandirDevoteePageDevoteeItemModel("Vishnu Bajrag", false,
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Rest+API+Integration+with+MVVM.png",
                        69
                )
        };
        return new ArrayList<>(Arrays.asList(MEDIA_OBJECTS));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_follow_god_mandnir_devotee_devotee, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ALog.i(TAG, "onStart called");
    }

    @Override
    public void onResume() {
        super.onResume();
        ALog.i(TAG, "onResume called");
    }

    @Override
    public void onPause() {
        super.onPause();
        ALog.i(TAG, "onPause called");
    }

    @Override
    public void onStop() {
        super.onStop();
        ALog.i(TAG, "onStop called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ALog.i(TAG, "onDestroy called");
    }

}