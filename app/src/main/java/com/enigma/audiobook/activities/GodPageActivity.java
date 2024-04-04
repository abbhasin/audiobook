package com.enigma.audiobook.activities;

import static com.enigma.audiobook.proxies.adapters.ModelAdapters.convert;
import static com.enigma.audiobook.utils.Utils.initGlide;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.enigma.audiobook.R;
import com.enigma.audiobook.adapters.GodPageRVAdapter;
import com.enigma.audiobook.backend.models.ContentUploadStatus;
import com.enigma.audiobook.backend.models.PostAssociationType;
import com.enigma.audiobook.backend.models.requests.GodFeedRequest;
import com.enigma.audiobook.backend.models.responses.CuratedFeedPaginationKey;
import com.enigma.audiobook.backend.models.responses.FeedItemHeader;
import com.enigma.audiobook.backend.models.responses.FeedPageResponse;
import com.enigma.audiobook.backend.models.responses.GodFeedHeader;
import com.enigma.audiobook.models.FeedItemFooterModel;
import com.enigma.audiobook.models.FeedItemModel;
import com.enigma.audiobook.models.GenericPageCardItemModel;
import com.enigma.audiobook.models.GodPageDetailsModel;
import com.enigma.audiobook.models.GodPageHeaderModel;
import com.enigma.audiobook.models.PostMessageModel;
import com.enigma.audiobook.proxies.FollowingsService;
import com.enigma.audiobook.proxies.MyFeedService;
import com.enigma.audiobook.proxies.RetrofitFactory;
import com.enigma.audiobook.recyclers.PlayableFeedBasedRecyclerView;
import com.enigma.audiobook.services.PostMessageService;
import com.enigma.audiobook.utils.ALog;
import com.enigma.audiobook.utils.ActivityResultLauncherProvider;
import com.enigma.audiobook.utils.PostAMessageUtils;
import com.enigma.audiobook.utils.PostMessageServiceProvider;
import com.enigma.audiobook.utils.RetryHelper;
import com.enigma.audiobook.utils.SharedPreferencesHandler;
import com.enigma.audiobook.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GodPageActivity extends AppCompatActivity implements ActivityResultLauncherProvider, PostMessageServiceProvider {

    public static final String GOD_ID_KEY = "godId";
    private String godId;
    private String userId;
    private PlayableFeedBasedRecyclerView recyclerView;
    private AtomicReference<GodPageRVAdapter> adapter = new AtomicReference<>();
    private MediaController mediaController;
    ActivityResultLauncher<PickVisualMediaRequest> pickMultipleImages;
    ActivityResultLauncher<PickVisualMediaRequest> pickVideo;
    ActivityResultLauncher<Intent> pickAudio;

    private MyFeedService myFeedService;
    private CuratedFeedPaginationKey curatedFeedPaginationKey;
    private boolean isLoading = false;
    private boolean noMorePaginationItems = false;
    int ctr = 0;

    Intent postMsgServiceIntent = null;
    PostMessageService postMessageService;
    boolean postMsgServiceBound = false;

    @Override
    public ActivityResultLauncher<PickVisualMediaRequest> getPickVideoLauncher() {
        return pickVideo;
    }

    @Override
    public ActivityResultLauncher<PickVisualMediaRequest> getPickImagesLauncher() {
        return pickMultipleImages;
    }

    @Override
    public ActivityResultLauncher<Intent> getPickAudioLauncher() {
        return pickAudio;
    }

    @Override
    public PostMessageService getPostMessageService() {
        return postMessageService;
    }

    @Override
    public boolean isServiceBound() {
        return postMsgServiceBound;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_god_page);

        Intent intent = getIntent();
        godId = intent.getStringExtra(GOD_ID_KEY);
        userId = SharedPreferencesHandler.getUserId(this).get();

        setupPostMessageService();

        recyclerView = findViewById(R.id.godPageRecyclerView);
//        initRecyclerView();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setupImagesPicker();
        setupVideoPicker();
        setupAudioPicker();
    }

    private void setupPostMessageService() {
        if (postMsgServiceIntent == null) {
            postMsgServiceIntent = new Intent(this, PostMessageService.class);
            bindService(postMsgServiceIntent, postMsgServiceConnection, Context.BIND_AUTO_CREATE);
            startService(postMsgServiceIntent);
            ALog.i("GodPageActivity", "Post Msg Service initialized");
        }
    }

    private void setupAudioPicker() {
        pickAudio =
                PostAMessageUtils.setupAudioPicker(GodPageActivity.this, adapter,
                        this::getPostMessageModel);
    }

    private void setupVideoPicker() {
        pickVideo = PostAMessageUtils.setupVideoPicker(GodPageActivity.this, adapter,
                this::getPostMessageModel);
    }

    private void setupImagesPicker() {
        pickMultipleImages = PostAMessageUtils.setupImagesPicker(GodPageActivity.this, adapter,
                this::getPostMessageModel);
    }

    private Optional<PostMessageModel> getPostMessageModel() {
        return adapter.get().getCardItems()
                .stream()
                .filter(card -> card.getType() == GodPageRVAdapter.GodPageViewTypes.POST_MESSAGE)
                .findFirst()
                .map(genricObj -> (PostMessageModel) genricObj.getCardItem());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PostAMessageUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    private ServiceConnection postMsgServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PostMessageService.PostMessageSrvBinder binder = (PostMessageService.PostMessageSrvBinder) service;
            postMessageService = binder.getService();
            postMsgServiceBound = true;

            ALog.i("GodPageActivity", "Post Msg Service connection established");
            onServiceBound();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            postMsgServiceBound = false;
        }
    };

    private void onServiceBound() {
        initRecyclerView();
    }

    private void initRecyclerView() {
        mediaController = new MediaController(this);
        recyclerView.setMediaController(mediaController, userId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        FollowingsService followingsService = RetrofitFactory.getInstance().createService(FollowingsService.class);

        List<GenericPageCardItemModel<GodPageRVAdapter.GodPageViewTypes>> mediaObjects = new ArrayList<>();
        myFeedService = RetrofitFactory.getInstance().createService(MyFeedService.class);
        Call<FeedPageResponse> feedPageResponseCall = getFeed();
        RetryHelper.enqueueWithRetry(feedPageResponseCall,
                new Callback<FeedPageResponse>() {
                    @Override
                    public void onResponse(Call<FeedPageResponse> call, Response<FeedPageResponse> response) {
                        if (!response.isSuccessful()) {
                            Toast.makeText(GodPageActivity.this,
                                    "Unable to fetch details. Please check internet connection & try again later!",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        FeedPageResponse feedPageResponse = response.body();

                        List<GenericPageCardItemModel<GodPageRVAdapter.GodPageViewTypes>> newMediaObjects =
                                convert(feedPageResponse, GodPageRVAdapter.GodPageViewTypes.FEED_ITEM);

                        mediaObjects.add(getHeader(feedPageResponse.getFeedItemHeader()));
                        mediaObjects.add(getDetails(feedPageResponse.getFeedItemHeader()));
                        getPostAMessage(feedPageResponse.getFeedItemHeader()).ifPresent(mediaObjects::add);
                        mediaObjects.addAll(newMediaObjects);
                        mediaObjects.add(getFooter());

                        curatedFeedPaginationKey = feedPageResponse.getCuratedFeedPaginationKey();
                        recyclerView.setMediaObjects(mediaObjects);

                        adapter.set(new GodPageRVAdapter(
                                initGlide(GodPageActivity.this),
                                mediaObjects,
                                GodPageActivity.this,
                                followingsService,
                                userId,
                                godId
                        ));
                        recyclerView.setAdapter(adapter.get());
                    }

                    @Override
                    public void onFailure(Call<FeedPageResponse> call, Throwable t) {
                        ALog.e("error", "", t);
                        Toast.makeText(GodPageActivity.this,
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

                        Call<FeedPageResponse> curatedFeedResponseCall = getFeed();
                        RetryHelper.enqueueWithRetry(curatedFeedResponseCall,
                                new Callback<FeedPageResponse>() {
                                    @Override
                                    public void onResponse(Call<FeedPageResponse> call, Response<FeedPageResponse> response) {
                                        if (!response.isSuccessful()) {
                                            Toast.makeText(GodPageActivity.this,
                                                    "Unable to fetch details. Please check internet connection & try again later!",
                                                    Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        FeedPageResponse feedPageResponse = response.body();
                                        if (Utils.isEmpty(feedPageResponse.getFeedItems())) {
                                            Toast.makeText(GodPageActivity.this,
                                                    "No more Feed Items. Thank You for Viewing!", Toast.LENGTH_SHORT).show();
                                            noMorePaginationItems = true;
                                            return;
                                        }
                                        List<GenericPageCardItemModel<GodPageRVAdapter.GodPageViewTypes>>
                                                newMediaObjects =
                                                convert(feedPageResponse, GodPageRVAdapter.GodPageViewTypes.FEED_ITEM);
                                        // int currentSize = mediaObjects.size();
                                        mediaObjects.remove(mediaObjects.size() - 1);
                                        mediaObjects.addAll(newMediaObjects);
                                        mediaObjects.add(getFooter());
                                        adapter.get().notifyDataSetChanged();
                                        // adapter.notifyItemRangeInserted(currentSize, moreMediaObjects.size());

                                        curatedFeedPaginationKey = feedPageResponse.getCuratedFeedPaginationKey();
//                                        Toast.makeText(GodPageActivity.this,
//                                                "More Feed Items added. Please scroll to see more.", Toast.LENGTH_SHORT).show();
                                        isLoading = false;
                                    }

                                    @Override
                                    public void onFailure(Call<FeedPageResponse> call, Throwable t) {
                                        Toast.makeText(GodPageActivity.this,
                                                "Unable to fetch details. Please check internet connection & try again later!",
                                                Toast.LENGTH_SHORT).show();
                                        isLoading = false;
                                    }
                                });
                    }
                }
            }
        });
    }

    private Call<FeedPageResponse> getFeed() {
        GodFeedRequest curatedFeedRequest = new GodFeedRequest();
        curatedFeedRequest.setLimit(50);
        curatedFeedRequest.setGodId(godId);
        curatedFeedRequest.setForUserId(userId);
        curatedFeedRequest.setCuratedFeedPaginationKey(curatedFeedPaginationKey);
        return myFeedService.getFeedPageOfGod(curatedFeedRequest);
    }

    private GenericPageCardItemModel<GodPageRVAdapter.GodPageViewTypes> getHeader(FeedItemHeader feedItemHeader) {
        GodFeedHeader godFeedHeader = feedItemHeader.getGodFeedHeader();
        return new GenericPageCardItemModel<>(
                new GodPageHeaderModel(godFeedHeader.getName(),
                        godFeedHeader.getImageUrls().get(0),
                        godFeedHeader.isCurrentUserFollowing(),
                        String.valueOf(godFeedHeader.getFollowersCount()),
                        godFeedHeader.isMyProfilePage()

                ), GodPageRVAdapter.GodPageViewTypes.HEADER);
    }

    private GenericPageCardItemModel<GodPageRVAdapter.GodPageViewTypes> getDetails(FeedItemHeader feedItemHeader) {
        GodFeedHeader godFeedHeader = feedItemHeader.getGodFeedHeader();
        return new GenericPageCardItemModel<>(
                new GodPageDetailsModel(
                        godFeedHeader.getDescription()
                ), GodPageRVAdapter.GodPageViewTypes.DETAILS);
    }

    private Optional<GenericPageCardItemModel<GodPageRVAdapter.GodPageViewTypes>> getPostAMessage(FeedItemHeader feedItemHeader) {
        GodFeedHeader godFeedHeader = feedItemHeader.getGodFeedHeader();
        if (!godFeedHeader.isMyProfilePage()) {
            return Optional.empty();
        }

        List<PostMessageModel.SpinnerTag> spinnerTags =
                godFeedHeader.getPostAMessageInfo().getTags()
                        .stream()
                        .map(t -> new PostMessageModel.SpinnerTag(t.getId(), t.getName()))
                        .collect(Collectors.toList());
        PostMessageModel postMessageModel = new PostMessageModel(
                spinnerTags);
        postMessageModel.setAssociatedGodId(godId);
        postMessageModel.setAssociationType(PostAssociationType.GOD);
        postMessageModel.setFromUserId(userId);

        return Optional.of(new GenericPageCardItemModel<>(
                postMessageModel,
                GodPageRVAdapter.GodPageViewTypes.POST_MESSAGE));

    }

    private GenericPageCardItemModel<GodPageRVAdapter.GodPageViewTypes> getFooter() {
        return new GenericPageCardItemModel<>(
                new FeedItemFooterModel(
                ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM_FOOTER);
    }

    @Override
    protected void onStart() {
        super.onStart();
        recyclerView.onStart();
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