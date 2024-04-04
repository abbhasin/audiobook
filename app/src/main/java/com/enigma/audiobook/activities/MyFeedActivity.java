package com.enigma.audiobook.activities;

import static com.enigma.audiobook.proxies.adapters.ModelAdapters.convert;
import static com.enigma.audiobook.utils.Utils.initGlide;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.enigma.audiobook.R;
import com.enigma.audiobook.adapters.MyFeedRVAdapter;
import com.enigma.audiobook.backend.models.ContentUploadStatus;
import com.enigma.audiobook.backend.models.PostAssociationType;
import com.enigma.audiobook.backend.models.requests.CuratedFeedRequest;
import com.enigma.audiobook.backend.models.responses.CuratedFeedPaginationKey;
import com.enigma.audiobook.backend.models.responses.FeedItemHeader;
import com.enigma.audiobook.backend.models.responses.FeedPageResponse;
import com.enigma.audiobook.models.FeedItemFooterModel;
import com.enigma.audiobook.models.FeedItemModel;
import com.enigma.audiobook.models.GenericPageCardItemModel;
import com.enigma.audiobook.models.MyFeedHeaderModel;
import com.enigma.audiobook.proxies.MyFeedService;
import com.enigma.audiobook.proxies.RetrofitFactory;
import com.enigma.audiobook.recyclers.PlayableFeedBasedRecyclerView;
import com.enigma.audiobook.utils.NavigationUtils;
import com.enigma.audiobook.utils.RetryHelper;
import com.enigma.audiobook.utils.SharedPreferencesHandler;
import com.enigma.audiobook.utils.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFeedActivity extends AppCompatActivity {

    private String userId;
    private PlayableFeedBasedRecyclerView recyclerView;
    private MyFeedRVAdapter adapter;
    private ProgressBar progressBar;
    private MyFeedService myFeedService;
    private CuratedFeedPaginationKey curatedFeedPaginationKey;
    private BottomNavigationView bottomNavigationView;
    private boolean isLoading = false;
    private boolean noMorePaginationItems = false;
    int ctr = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_feed);

        userId = SharedPreferencesHandler.getUserId(this).get();

        setupNavigation();
        progressBar = findViewById(R.id.myFeedProgressBar);
        recyclerView = findViewById(R.id.myFeedRecyclerView);
        initRecyclerView();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void setupNavigation() {
        bottomNavigationView = NavigationUtils.setupNavigationDrawer(
                this,
                R.id.myFeedBottomNavigation,
                R.id.menuItemMyFeed);
    }

    private void initRecyclerView() {
        MediaController mediaController = new MediaController(this);
        recyclerView.setMediaController(mediaController, userId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        myFeedService = RetrofitFactory.getInstance().createService(MyFeedService.class);

        List<GenericPageCardItemModel<MyFeedRVAdapter.MyFeedViewTypes>> mediaObjects = new ArrayList<>();
        recyclerView.setMediaObjects(mediaObjects);

        adapter = new MyFeedRVAdapter(initGlide(MyFeedActivity.this),
                mediaObjects,
                MyFeedActivity.this);

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
        curatedFeedPaginationKey = null;


        Call<FeedPageResponse> feedPageResponseCall = getFeed();
        RetryHelper.enqueueWithRetry(feedPageResponseCall,
                new Callback<FeedPageResponse>() {
                    @Override
                    public void onResponse(Call<FeedPageResponse> call, Response<FeedPageResponse> response) {
                        if (!response.isSuccessful()) {
                            Toast.makeText(MyFeedActivity.this,
                                    "Unable to fetch details. Please check internet connection & try again later!",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        FeedPageResponse feedPageResponse = response.body();
                        List<GenericPageCardItemModel<MyFeedRVAdapter.MyFeedViewTypes>> newMediaObjects =
                                convert(feedPageResponse, MyFeedRVAdapter.MyFeedViewTypes.FEED_ITEM);

                        adapter.getCardItems().add(getHeader(feedPageResponse.getFeedItemHeader()));
                        adapter.getCardItems().addAll(newMediaObjects);
                        adapter.getCardItems().add(getFooter());

                        curatedFeedPaginationKey = feedPageResponse.getCuratedFeedPaginationKey();
                        adapter.notifyDataSetChanged();

                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(Call<FeedPageResponse> call, Throwable t) {
                        Toast.makeText(MyFeedActivity.this,
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

                        Call<FeedPageResponse> curatedFeedResponseCall = getFeed();
                        RetryHelper.enqueueWithRetry(curatedFeedResponseCall,
                                new Callback<FeedPageResponse>() {
                                    @Override
                                    public void onResponse(Call<FeedPageResponse> call, Response<FeedPageResponse> response) {
                                        if (!response.isSuccessful()) {
                                            Toast.makeText(MyFeedActivity.this,
                                                    "Unable to fetch details. Please check internet connection & try again later!",
                                                    Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        FeedPageResponse feedPageResponse = response.body();
                                        if (Utils.isEmpty(feedPageResponse.getFeedItems())) {
                                            Toast.makeText(MyFeedActivity.this,
                                                    "No more Feed Items. Thank You for Viewing!", Toast.LENGTH_SHORT).show();
                                            noMorePaginationItems = true;
                                            return;
                                        }
                                        List<GenericPageCardItemModel<MyFeedRVAdapter.MyFeedViewTypes>> newMediaObjects =
                                                convert(feedPageResponse, MyFeedRVAdapter.MyFeedViewTypes.FEED_ITEM);
                                        // int currentSize = mediaObjects.size();
                                        adapter.getCardItems().remove(adapter.getCardItems().size() - 1);
                                        adapter.getCardItems().addAll(newMediaObjects);
                                        adapter.getCardItems().add(getFooter());
                                        adapter.notifyDataSetChanged();
                                        // adapter.notifyItemRangeInserted(currentSize, moreMediaObjects.size());

                                        curatedFeedPaginationKey = feedPageResponse.getCuratedFeedPaginationKey();
//                                        Toast.makeText(MyFeedActivity.this,
//                                                "More Feed Items added. Please scroll to see more.", Toast.LENGTH_SHORT).show();
                                        isLoading = false;
                                    }

                                    @Override
                                    public void onFailure(Call<FeedPageResponse> call, Throwable t) {
                                        isLoading = false;
                                        Toast.makeText(MyFeedActivity.this,
                                                "Unable to fetch details. Please check internet connection & try again later!",
                                                Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }
                }
            }
        });
    }

    private Call<FeedPageResponse> getFeed() {
        CuratedFeedRequest curatedFeedRequest = new CuratedFeedRequest();
        curatedFeedRequest.setLimit(100);
        curatedFeedRequest.setUserId(userId);
        curatedFeedRequest.setCuratedFeedPaginationKey(curatedFeedPaginationKey);
        return myFeedService.getFeedPage(curatedFeedRequest);
    }

    private GenericPageCardItemModel<MyFeedRVAdapter.MyFeedViewTypes> getHeader(FeedItemHeader feedItemHeader) {
        return new GenericPageCardItemModel<>(
                new MyFeedHeaderModel(feedItemHeader.getMyFeedHeader().getFollowingsCount()
                ), MyFeedRVAdapter.MyFeedViewTypes.HEADER);
    }

    private GenericPageCardItemModel<MyFeedRVAdapter.MyFeedViewTypes> getFooter() {
        return new GenericPageCardItemModel<>(
                new FeedItemFooterModel(
                ), MyFeedRVAdapter.MyFeedViewTypes.FEED_ITEM_FOOTER);
    }

    @Override
    protected void onStart() {
        super.onStart();
        refresh();
        recyclerView.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        NavigationUtils.setMenuItemChecked(bottomNavigationView, R.id.menuItemMyFeed);
    }

    @Override
    protected void onPause() {
        super.onPause();
        recyclerView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recyclerView.onDestroy();
    }
}