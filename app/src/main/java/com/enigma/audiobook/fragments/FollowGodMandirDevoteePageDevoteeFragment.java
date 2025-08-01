package com.enigma.audiobook.fragments;

import static com.enigma.audiobook.activities.FollowMandirAndDevoteesActivity.ONLY_FOLLOWED_KEY;
import static com.enigma.audiobook.utils.Utils.initGlide;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import com.enigma.audiobook.utils.SharedPreferencesHandler;
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

    private String userId;
    private RecyclerView recyclerView;
    private FollowGodMandirDevoteePageDevoteeRVAdapter adapter;
    FollowingsService followingsService;
    private RequestManager requestManager;
    private ProgressBar progressBar;

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
        userId = SharedPreferencesHandler.getUserId(getContext()).get();
        recyclerView = view.findViewById(R.id.fragmentFollowGodMandirAndDevotees_DevoteesRV);

        progressBar = view.findViewById(R.id.fragmentFollowGodMandirAndDevotees_DevoteesRV_ProgressBar);

        initRecyclerView();
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        followingsService = RetrofitFactory.getInstance().createService(FollowingsService.class);

        influencerService = RetrofitFactory.getInstance().createService(InfluencerService.class);

        adapter = new FollowGodMandirDevoteePageDevoteeRVAdapter(
                initGlide(getContext()),
                new ArrayList<>(),
                followingsService, userId, getContext());

        recyclerView.setAdapter(adapter);
    }

    private void refresh() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        adapter.getCardItems().clear();
        adapter.notifyDataSetChanged();
        noMorePaginationItems = false;
        isLoading = false;
        lastInfluencerForPagination = null;

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

                        adapter.getCardItems().addAll(ModelAdapters.convertInfluencersForUser(influencersForUser));
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);

                        if (!influencersForUser.isEmpty()) {
                            lastInfluencerForPagination = influencersForUser.get(influencersForUser.size() - 1);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<InfluencerForUser>> call, Throwable t) {
                        Toast.makeText(getContext(),
                                "Unable to fetch details. Please check internet connection & try again later!",
                                Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
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

                if (Utils.isEmpty(adapter.getCardItems())) {
                    return;
                }

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading && !noMorePaginationItems) {
                    if (linearLayoutManager != null &&
                            linearLayoutManager.findLastCompletelyVisibleItemPosition() ==
                                    adapter.getCardItems().size() - 2) {
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

                                        adapter.getCardItems().addAll(newMediaObjects);
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
        return influencerService.getInfleuncersForUserNext(
                20,
                userId,
                getLastInfluencerIdForPagination(),
                onlyFollowed
        );
    }

    private Call<List<InfluencerForUser>> getInfluencersForUser() {
        return influencerService.getInfleuncersForUser(20, userId, onlyFollowed);
    }

    private String getLastInfluencerIdForPagination() {
        if (lastInfluencerForPagination == null) {
            return null;
        }
        return lastInfluencerForPagination.getInfluencer().getUserId();
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
        refresh();
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