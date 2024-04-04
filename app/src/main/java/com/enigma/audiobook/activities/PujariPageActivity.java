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
import com.enigma.audiobook.adapters.PujariPageRVAdapter;
import com.enigma.audiobook.backend.models.ContentUploadStatus;
import com.enigma.audiobook.backend.models.PostAssociationType;
import com.enigma.audiobook.backend.models.requests.InfluencerFeedRequest;
import com.enigma.audiobook.backend.models.responses.CuratedFeedPaginationKey;
import com.enigma.audiobook.backend.models.responses.FeedItemHeader;
import com.enigma.audiobook.backend.models.responses.FeedPageResponse;
import com.enigma.audiobook.backend.models.responses.InfluencerFeedHeader;
import com.enigma.audiobook.models.FeedItemFooterModel;
import com.enigma.audiobook.models.FeedItemModel;
import com.enigma.audiobook.models.GenericPageCardItemModel;
import com.enigma.audiobook.models.PostMessageModel;
import com.enigma.audiobook.models.PujariPageDetailsModel;
import com.enigma.audiobook.models.PujariPageHeaderModel;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PujariPageActivity extends AppCompatActivity implements ActivityResultLauncherProvider,
        PostMessageServiceProvider {
    private static final String TAG = "PujariPageActivity";
    public static String INFLUENCER_ID_KEY = "influencerId";

    private String influencerId;
    private String userId;
    private PlayableFeedBasedRecyclerView recyclerView;
    private AtomicReference<PujariPageRVAdapter> adapter = new AtomicReference<>();
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pujari_page);

        Intent intent = getIntent();
        influencerId = intent.getStringExtra(INFLUENCER_ID_KEY);

        userId = SharedPreferencesHandler.getUserId(this).get();

        setupPostMessageService();

        recyclerView = findViewById(R.id.pujariPageRecyclerView);
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
            ALog.i(TAG, "Post Msg Service initialized");
        }
    }

    @Override
    public PostMessageService getPostMessageService() {
        return postMessageService;
    }

    @Override
    public boolean isServiceBound() {
        return postMsgServiceBound;
    }

    private void setupAudioPicker() {
        pickAudio =
                PostAMessageUtils.setupAudioPicker(PujariPageActivity.this, adapter,
                        this::getPostMessageModel);
    }

    private void setupVideoPicker() {
        pickVideo = PostAMessageUtils.setupVideoPicker(PujariPageActivity.this, adapter,
                this::getPostMessageModel);
    }

    private void setupImagesPicker() {
        pickMultipleImages = PostAMessageUtils.setupImagesPicker(PujariPageActivity.this, adapter,
                this::getPostMessageModel);
    }

    private Optional<PostMessageModel> getPostMessageModel() {
        return adapter.get().getCardItems()
                .stream()
                .filter(card -> card.getType() == PujariPageRVAdapter.PujariPageViewTypes.POST_MESSAGE)
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

            ALog.i(TAG, "Post Msg Service connection established");
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
        MediaController mediaController = new MediaController(this);
        recyclerView.setMediaController(mediaController, userId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        FollowingsService followingsService = RetrofitFactory.getInstance().createService(FollowingsService.class);

        List<GenericPageCardItemModel<PujariPageRVAdapter.PujariPageViewTypes>> mediaObjects = new ArrayList<>();
        myFeedService = RetrofitFactory.getInstance().createService(MyFeedService.class);
        Call<FeedPageResponse> feedPageResponseCall = getFeed();
        RetryHelper.enqueueWithRetry(feedPageResponseCall,
                new Callback<FeedPageResponse>() {
                    @Override
                    public void onResponse(Call<FeedPageResponse> call, Response<FeedPageResponse> response) {
                        if (!response.isSuccessful()) {
                            Toast.makeText(PujariPageActivity.this,
                                    "Unable to fetch details. Please check internet connection & try again later!",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        FeedPageResponse feedPageResponse = response.body();

                        List<GenericPageCardItemModel<PujariPageRVAdapter.PujariPageViewTypes>> newMediaObjects =
                                convert(feedPageResponse, PujariPageRVAdapter.PujariPageViewTypes.FEED_ITEM);

                        mediaObjects.add(getHeader(feedPageResponse.getFeedItemHeader()));
                        mediaObjects.add(getDetails(feedPageResponse.getFeedItemHeader()));
                        getPostAMessage(feedPageResponse.getFeedItemHeader()).ifPresent(mediaObjects::add);
                        mediaObjects.addAll(newMediaObjects);
                        mediaObjects.add(getFooter());

                        curatedFeedPaginationKey = feedPageResponse.getCuratedFeedPaginationKey();

                        recyclerView.setMediaObjects(mediaObjects);
                        adapter.set(new PujariPageRVAdapter(
                                initGlide(PujariPageActivity.this),
                                mediaObjects,
                                PujariPageActivity.this,
                                followingsService,
                                userId,
                                influencerId
                                ));
                        recyclerView.setAdapter(adapter.get());
                    }

                    @Override
                    public void onFailure(Call<FeedPageResponse> call, Throwable t) {
                        ALog.e("error", "", t);
                        Toast.makeText(PujariPageActivity.this,
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
                                            Toast.makeText(PujariPageActivity.this,
                                                    "Unable to fetch details. Please check internet connection & try again later!",
                                                    Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        FeedPageResponse feedPageResponse = response.body();
                                        if (Utils.isEmpty(feedPageResponse.getFeedItems())) {
                                            Toast.makeText(PujariPageActivity.this,
                                                    "No more Feed Items. Thank You for Viewing!", Toast.LENGTH_SHORT).show();
                                            noMorePaginationItems = true;
                                            return;
                                        }
                                        List<GenericPageCardItemModel<PujariPageRVAdapter.PujariPageViewTypes>> newMediaObjects =
                                                convert(feedPageResponse, PujariPageRVAdapter.PujariPageViewTypes.FEED_ITEM);
                                        // int currentSize = mediaObjects.size();
                                        mediaObjects.remove(mediaObjects.size() - 1);
                                        mediaObjects.addAll(newMediaObjects);
                                        mediaObjects.add(getFooter());
                                        adapter.get().notifyDataSetChanged();
                                        // adapter.notifyItemRangeInserted(currentSize, moreMediaObjects.size());

                                        curatedFeedPaginationKey = feedPageResponse.getCuratedFeedPaginationKey();
//                                        Toast.makeText(PujariPageActivity.this,
//                                                "More Feed Items added. Please scroll to see more.", Toast.LENGTH_SHORT).show();
                                        isLoading = false;
                                    }

                                    @Override
                                    public void onFailure(Call<FeedPageResponse> call, Throwable t) {
                                        isLoading = false;
                                        Toast.makeText(PujariPageActivity.this,
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
        InfluencerFeedRequest curatedFeedRequest = new InfluencerFeedRequest();
        curatedFeedRequest.setLimit(50);
        curatedFeedRequest.setInfluencerId(influencerId);
        curatedFeedRequest.setForUserId(userId);
        curatedFeedRequest.setCuratedFeedPaginationKey(curatedFeedPaginationKey);
        return myFeedService.getFeedPageOfInfluencer(curatedFeedRequest);
    }

    private Optional<GenericPageCardItemModel<PujariPageRVAdapter.PujariPageViewTypes>>
    getPostAMessage(FeedItemHeader feedItemHeader) {
        InfluencerFeedHeader influencerFeedHeader = feedItemHeader.getInfluencerFeedHeader();
        if (!influencerFeedHeader.isMyProfilePage()) {
            return Optional.empty();
        }

        List<PostMessageModel.SpinnerTag> spinnerTags =
                influencerFeedHeader.getPostAMessageInfo().getTags()
                        .stream()
                        .map(t -> new PostMessageModel.SpinnerTag(t.getId(), t.getName()))
                        .collect(Collectors.toList());

        PostMessageModel postMessageModel = new PostMessageModel(
                spinnerTags);
        postMessageModel.setAssociatedInfluencerId(influencerId);
        postMessageModel.setAssociationType(PostAssociationType.INFLUENCER);
        postMessageModel.setFromUserId(userId);

        return Optional.of(new GenericPageCardItemModel<>(
                postMessageModel,
                PujariPageRVAdapter.PujariPageViewTypes.POST_MESSAGE));
    }

    private GenericPageCardItemModel<PujariPageRVAdapter.PujariPageViewTypes> getHeader(FeedItemHeader feedItemHeader) {
        InfluencerFeedHeader influencerFeedHeader = feedItemHeader.getInfluencerFeedHeader();
        return new GenericPageCardItemModel<>(
                new PujariPageHeaderModel(influencerFeedHeader.getName(),
                        influencerFeedHeader.getImageUrls().get(0),
                        influencerFeedHeader.isCurrentUserFollowing(),
                        String.valueOf(influencerFeedHeader.getFollowersCount()),
                        influencerFeedHeader.isMyProfilePage()

                ), PujariPageRVAdapter.PujariPageViewTypes.HEADER);
    }

    private GenericPageCardItemModel<PujariPageRVAdapter.PujariPageViewTypes> getDetails(FeedItemHeader feedItemHeader) {
        InfluencerFeedHeader influencerFeedHeader = feedItemHeader.getInfluencerFeedHeader();
        return new GenericPageCardItemModel<>(
                new PujariPageDetailsModel(
                        influencerFeedHeader.getDescription()
                ), PujariPageRVAdapter.PujariPageViewTypes.DETAILS);
    }

    private GenericPageCardItemModel<PujariPageRVAdapter.PujariPageViewTypes> getFooter() {
        return new GenericPageCardItemModel<>(
                new FeedItemFooterModel(
                ), PujariPageRVAdapter.PujariPageViewTypes.FEED_ITEM_FOOTER);
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