package com.enigma.audiobook.fragments;

import static com.enigma.audiobook.activities.FollowMandirAndDevoteesActivity.ONLY_FOLLOWED_KEY;
import static com.enigma.audiobook.utils.Utils.initGlide;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.enigma.audiobook.R;
import com.enigma.audiobook.adapters.FollowGodMandirDevoteePageDevoteeRVAdapter;
import com.enigma.audiobook.backend.models.responses.InfluencerForUser;
import com.enigma.audiobook.models.FollowGodMandirDevoteePageDevoteeItemModel;
import com.enigma.audiobook.proxies.FollowingsService;
import com.enigma.audiobook.proxies.InfluencerService;
import com.enigma.audiobook.proxies.RetrofitFactory;
import com.enigma.audiobook.proxies.adapters.ModelAdapters;
import com.enigma.audiobook.utils.ALog;
import com.enigma.audiobook.utils.RetryHelper;
import com.enigma.audiobook.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FollowGodMandirDevoteePageDevoteeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FollowGodMandirDevoteePageDevoteeFragment extends Fragment {
    private static final String TAG = "FollowGodMandirDevoteePageDevoteeFragment";

    private String userId = "65a7936792bb9e2f44a1ea47";
    private RecyclerView recyclerView;
    private FollowGodMandirDevoteePageDevoteeRVAdapter adapter;
    private RequestManager requestManager;

    private InfluencerService influencerService;
    private boolean onlyFollowed = false;

    private boolean isLoading = false;
    private boolean noMorePaginationItems = false;
    private InfluencerForUser lastInfluencerForPagination;

    public FollowGodMandirDevoteePageDevoteeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static FollowGodMandirDevoteePageDevoteeFragment newInstance(boolean onlyFollowed) {
        FollowGodMandirDevoteePageDevoteeFragment fragment = new FollowGodMandirDevoteePageDevoteeFragment();
        Bundle args = new Bundle();
        args.putBoolean(ONLY_FOLLOWED_KEY, onlyFollowed);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ALog.i(TAG, "onCreate called");
        if (getArguments() != null) {
            onlyFollowed = getArguments().getBoolean(ONLY_FOLLOWED_KEY);
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

        FollowingsService followingsService = RetrofitFactory.getInstance().createService(FollowingsService.class);

        List<FollowGodMandirDevoteePageDevoteeItemModel> mediaObjects = new ArrayList<>();

        influencerService = RetrofitFactory.getInstance().createService(InfluencerService.class);
        Call<List<InfluencerForUser>> infleuncersForUser = getInfluencersForUser();

        RetryHelper.enqueueWithRetry(infleuncersForUser,
                new Callback<List<InfluencerForUser>>() {
                    @Override
                    public void onResponse(Call<List<InfluencerForUser>> call, Response<List<InfluencerForUser>> response) {
                        if (!response.isSuccessful()) {
                            Toast.makeText(getContext(),
                                    "Unable to fetch details. Please check internet connection & try again later!",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<InfluencerForUser> influencersForUser = response.body();
                        mediaObjects.addAll(ModelAdapters.convertInfluencersForUser(influencersForUser));

                        adapter = new FollowGodMandirDevoteePageDevoteeRVAdapter(
                                initGlide(getContext()), mediaObjects,
                                followingsService, userId, getContext());
                        recyclerView.setAdapter(adapter);
                        if (!influencersForUser.isEmpty()) {
                            lastInfluencerForPagination = influencersForUser.get(influencersForUser.size() - 1);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<InfluencerForUser>> call, Throwable t) {
                        Toast.makeText(getContext(),
                                "Unable to fetch details. Please check internet connection & try again later!",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (Utils.isEmpty(mediaObjects)) {
                    return;
                }

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading && !noMorePaginationItems) {
                    if (linearLayoutManager != null &&
                            linearLayoutManager.findLastCompletelyVisibleItemPosition() ==
                                    mediaObjects.size() - 2) {
                        isLoading = true;
                        Call<List<InfluencerForUser>> infleuncersForUser =
                                getInfluencersForUserPaginated();

                        RetryHelper.enqueueWithRetry(infleuncersForUser,
                                new Callback<List<InfluencerForUser>>() {
                                    @Override
                                    public void onResponse(Call<List<InfluencerForUser>> call, Response<List<InfluencerForUser>> response) {
                                        if (!response.isSuccessful()) {
                                            Toast.makeText(getContext(),
                                                    "Unable to fetch details. Please check internet connection & try again later!",
                                                    Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        List<InfluencerForUser> influencersForUser = response.body();
                                        if (Utils.isEmpty(influencersForUser)) {
                                            Toast.makeText(getContext(),
                                                    "All Influencers added. Thank You for Viewing!", Toast.LENGTH_SHORT).show();
                                            noMorePaginationItems = true;
                                            return;
                                        }
                                        List<FollowGodMandirDevoteePageDevoteeItemModel>
                                                newMediaObjects =
                                                ModelAdapters.convertInfluencersForUser(influencersForUser);

                                        mediaObjects.addAll(newMediaObjects);
                                        adapter.notifyDataSetChanged();

                                        if (!influencersForUser.isEmpty()) {
                                            lastInfluencerForPagination = influencersForUser.get(influencersForUser.size() - 1);
                                        }

                                        Toast.makeText(getContext(),
                                                "More Influencers added. Please scroll to see more.", Toast.LENGTH_SHORT).show();
                                        isLoading = false;
                                    }

                                    @Override
                                    public void onFailure(Call<List<InfluencerForUser>> call, Throwable t) {
                                        isLoading = false;
                                        Toast.makeText(getContext(),
                                                "Unable to fetch details. Please check internet connection & try again later!",
                                                Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }
                }
            }
        });
    }

    private Call<List<InfluencerForUser>> getInfluencersForUserPaginated() {
        if (onlyFollowed) {
            // TODO: change
            return influencerService.getInfleuncersForUserNext(
                    20,
                    userId,
                    getLastInfluencerIdForPagination()
            );
        } else {
            return influencerService.getInfleuncersForUserNext(
                    20,
                    userId,
                    getLastInfluencerIdForPagination()
            );
        }
    }

    private Call<List<InfluencerForUser>> getInfluencersForUser() {
        if (onlyFollowed) {
            // TODO: change
            return influencerService.getInfleuncersForUser(20, userId);
            ;
        } else {
            return influencerService.getInfleuncersForUser(20, "65a7936792bb9e2f44a1ea47");
        }
    }

    private String getLastInfluencerIdForPagination() {
        if (lastInfluencerForPagination == null) {
            return null;
        }
        return lastInfluencerForPagination.getInfluencer().getUserId();
    }

    private List<FollowGodMandirDevoteePageDevoteeItemModel> getMediaObjects() {
        FollowGodMandirDevoteePageDevoteeItemModel[] MEDIA_OBJECTS = {
                new FollowGodMandirDevoteePageDevoteeItemModel("influencerId", "Ashok Acharya", false,
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        97
                ),
                new FollowGodMandirDevoteePageDevoteeItemModel("influencerId", "Anil Shaktimaan", false,
                        "https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg",
                        83
                ),
                new FollowGodMandirDevoteePageDevoteeItemModel("influencerId", "Prerna Kerlaiya", false,
                        "https://mixkit.imgix.net/videos/preview/mixkit-young-man-at-the-bowling-center-makes-a-shot-49114-0.jpg",
                        79
                ),
                new FollowGodMandirDevoteePageDevoteeItemModel("influencerId", "Vishnu Bajrag", false,
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