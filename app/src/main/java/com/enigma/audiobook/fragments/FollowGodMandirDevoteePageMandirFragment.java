package com.enigma.audiobook.fragments;

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
import com.enigma.audiobook.adapters.FollowGodMandirDevoteePageGodRVAdapter;
import com.enigma.audiobook.adapters.FollowGodMandirDevoteePageMandirRVAdapter;
import com.enigma.audiobook.backend.models.responses.GodForUser;
import com.enigma.audiobook.backend.models.responses.MandirForUser;
import com.enigma.audiobook.models.FollowGodMandirDevoteePageGodItemModel;
import com.enigma.audiobook.models.FollowGodMandirDevoteePageMandirItemModel;
import com.enigma.audiobook.proxies.GodService;
import com.enigma.audiobook.proxies.MandirService;
import com.enigma.audiobook.proxies.RetrofitFactory;
import com.enigma.audiobook.proxies.adapters.ModelAdapters;
import com.enigma.audiobook.utils.ALog;
import com.enigma.audiobook.utils.Utils;
import com.google.android.gms.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FollowGodMandirDevoteePageMandirFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FollowGodMandirDevoteePageMandirFragment extends Fragment {
    private static final String TAG = "FollowGodMandirDevoteePageMandirFragment";
    private RecyclerView recyclerView;
    private FollowGodMandirDevoteePageMandirRVAdapter adapter;
    private RequestManager requestManager;

    private MandirService mandirService;

    private boolean isLoading = false;
    private boolean noMorePaginationItems = false;
    private MandirForUser lastMandirForPagination;

    public FollowGodMandirDevoteePageMandirFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static FollowGodMandirDevoteePageMandirFragment newInstance() {
        FollowGodMandirDevoteePageMandirFragment fragment = new FollowGodMandirDevoteePageMandirFragment();
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
        recyclerView = view.findViewById(R.id.fragmentFollowGodMandirAndDevotees_MandirRV);
        initRecyclerView();
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        List<FollowGodMandirDevoteePageMandirItemModel> mediaObjects = new ArrayList<>();

        mandirService = RetrofitFactory.getInstance().createService(MandirService.class);
        Call<List<MandirForUser>> mandirsForUser = mandirService.getMandirsForUser(20, "65a7936792bb9e2f44a1ea47");
        mandirsForUser.enqueue(new Callback<List<MandirForUser>>() {
            @Override
            public void onResponse(Call<List<MandirForUser>> call, Response<List<MandirForUser>> response) {
                List<MandirForUser> mandirForUsers = response.body();
                mediaObjects.addAll(ModelAdapters.convertMandirsForUser(mandirForUsers));

                adapter = new FollowGodMandirDevoteePageMandirRVAdapter(initGlide(getContext()), mediaObjects);
                recyclerView.setAdapter(adapter);

                if (!mandirForUsers.isEmpty()) {
                    lastMandirForPagination = mandirForUsers.get(mandirForUsers.size() - 1);
                }
            }

            @Override
            public void onFailure(Call<List<MandirForUser>> call, Throwable t) {

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

                        Call<List<MandirForUser>> mandirsForUser =
                                mandirService.getMandirsForUserNext(
                                        20, "65a7936792bb9e2f44a1ea47",
                                        getLastMandirIdForPagination()
                                );
                        mandirsForUser.enqueue(new Callback<List<MandirForUser>>() {
                            @Override
                            public void onResponse(Call<List<MandirForUser>> call, Response<List<MandirForUser>> response) {
                                List<MandirForUser> mandirForUsers = response.body();
                                if (CollectionUtils.isEmpty(mandirForUsers)) {
                                    Toast.makeText(getContext(),
                                            "All Mandirs added. Thank You for Viewing!", Toast.LENGTH_SHORT).show();
                                    noMorePaginationItems = true;
                                    return;
                                }
                                List<FollowGodMandirDevoteePageMandirItemModel>
                                        newMediaObjects =
                                        ModelAdapters.convertMandirsForUser(mandirForUsers);

                                mediaObjects.addAll(newMediaObjects);
                                adapter.notifyDataSetChanged();

                                if (!mandirForUsers.isEmpty()) {
                                    lastMandirForPagination = mandirForUsers.get(mandirForUsers.size() - 1);
                                }

                                Toast.makeText(getContext(),
                                        "More Mandirs added. Please scroll to see more.", Toast.LENGTH_SHORT).show();
                                isLoading = false;
                            }

                            @Override
                            public void onFailure(Call<List<MandirForUser>> call, Throwable t) {
                                isLoading = false;
                            }
                        });
                    }
                }
            }
        });
    }

    private String getLastMandirIdForPagination() {
        if (lastMandirForPagination == null) {
            return null;
        }
        return lastMandirForPagination.getMandir().getMandirId();
    }

    private List<FollowGodMandirDevoteePageMandirItemModel> getMediaObjects() {
        FollowGodMandirDevoteePageMandirItemModel[] MEDIA_OBJECTS = {
                new FollowGodMandirDevoteePageMandirItemModel("Gurdwara XYZ", false,
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "JanakPuri, New Delhi"
                ),
                new FollowGodMandirDevoteePageMandirItemModel("Ganesh Mandir", false,
                        "https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg",
                        "Naraina Vihar, New Delhi"
                ),
                new FollowGodMandirDevoteePageMandirItemModel("Durga Ma Mandir", false,
                        "https://mixkit.imgix.net/videos/preview/mixkit-young-man-at-the-bowling-center-makes-a-shot-49114-0.jpg",
                        "Gurgaon Sector 47, Haryana"
                ),
                new FollowGodMandirDevoteePageMandirItemModel("Vishnu Mandir", false,
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Rest+API+Integration+with+MVVM.png",
                        "Noida Sector 23, UP"
                )
        };
        return new ArrayList<>(Arrays.asList(MEDIA_OBJECTS));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_follow_god_mandnir_devotee_mandir, container, false);
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