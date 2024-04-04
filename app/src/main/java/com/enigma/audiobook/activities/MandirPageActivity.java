package com.enigma.audiobook.activities;

import static com.enigma.audiobook.proxies.adapters.ModelAdapters.convert;
import static com.enigma.audiobook.proxies.adapters.ModelAdapters.getLocation;
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
import com.enigma.audiobook.adapters.MandirPageRVAdapter;
import com.enigma.audiobook.backend.models.ContentUploadStatus;
import com.enigma.audiobook.backend.models.PostAssociationType;
import com.enigma.audiobook.backend.models.requests.MandirFeedRequest;
import com.enigma.audiobook.backend.models.responses.CuratedFeedPaginationKey;
import com.enigma.audiobook.backend.models.responses.FeedItemHeader;
import com.enigma.audiobook.backend.models.responses.FeedPageResponse;
import com.enigma.audiobook.backend.models.responses.MandirFeedHeader;
import com.enigma.audiobook.models.FeedItemFooterModel;
import com.enigma.audiobook.models.FeedItemModel;
import com.enigma.audiobook.models.GenericPageCardItemModel;
import com.enigma.audiobook.models.MandirPageDetailsModel;
import com.enigma.audiobook.models.MandirPageHeaderModel;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MandirPageActivity extends AppCompatActivity implements ActivityResultLauncherProvider,
        PostMessageServiceProvider {
    public static final String MANDIR_ID_KEY = "mandirId";
    private static final String TAG = "MandirPageActivity";

    private String mandirId;
    private String userId;
    private PlayableFeedBasedRecyclerView recyclerView;
    private AtomicReference<MandirPageRVAdapter> adapter = new AtomicReference<>();
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
        setContentView(R.layout.activity_mandir_page);
        ALog.i(TAG, "Mandir Page Activity called");

        Intent intent = getIntent();
        mandirId = intent.getStringExtra(MANDIR_ID_KEY);

        userId = SharedPreferencesHandler.getUserId(this).get();

        setupPostMessageService();

        recyclerView = findViewById(R.id.mandirPageRecyclerView);
//        initRecyclerView();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setupImagesPicker();
        setupVideoPicker();
        setupAudioPicker();
    }

    private void setupPostMessageService() {
        ALog.i(TAG, "initializing Post Msg Service");
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
                PostAMessageUtils.setupAudioPicker(MandirPageActivity.this, adapter,
                        this::getPostMessageModel);
    }

    private void setupVideoPicker() {
        pickVideo = PostAMessageUtils.setupVideoPicker(MandirPageActivity.this, adapter,
                this::getPostMessageModel);
    }

    private void setupImagesPicker() {
        pickMultipleImages = PostAMessageUtils.setupImagesPicker(MandirPageActivity.this, adapter,
                this::getPostMessageModel);
    }

    private Optional<PostMessageModel> getPostMessageModel() {
        return adapter.get().getCardItems()
                .stream()
                .filter(card -> card.getType() == MandirPageRVAdapter.MandirPageViewTypes.POST_MESSAGE)
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

        List<GenericPageCardItemModel<MandirPageRVAdapter.MandirPageViewTypes>> mediaObjects = new ArrayList<>();
        myFeedService = RetrofitFactory.getInstance().createService(MyFeedService.class);
        Call<FeedPageResponse> feedPageResponseCall = getFeed();
        RetryHelper.enqueueWithRetry(feedPageResponseCall,
                new Callback<FeedPageResponse>() {
                    @Override
                    public void onResponse(Call<FeedPageResponse> call, Response<FeedPageResponse> response) {
                        if(!response.isSuccessful()) {
                            Toast.makeText(MandirPageActivity.this,
                                    "Unable to fetch details. Please check internet connection & try again later!",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        FeedPageResponse feedPageResponse = response.body();

                        List<GenericPageCardItemModel<MandirPageRVAdapter.MandirPageViewTypes>> newMediaObjects =
                                convert(feedPageResponse, MandirPageRVAdapter.MandirPageViewTypes.FEED_ITEM);

                        mediaObjects.add(getHeader(feedPageResponse.getFeedItemHeader()));
                        mediaObjects.add(getDetails(feedPageResponse.getFeedItemHeader()));
                        getPostAMessage(feedPageResponse.getFeedItemHeader()).ifPresent(mediaObjects::add);
                        mediaObjects.addAll(newMediaObjects);
                        mediaObjects.add(getFooter());

                        curatedFeedPaginationKey = feedPageResponse.getCuratedFeedPaginationKey();

                        recyclerView.setMediaObjects(mediaObjects);
                        adapter.set(new MandirPageRVAdapter(
                                initGlide(MandirPageActivity.this),
                                mediaObjects,
                                MandirPageActivity.this,
                                followingsService,
                                userId,
                                mandirId));
                        recyclerView.setAdapter(adapter.get());
                    }

                    @Override
                    public void onFailure(Call<FeedPageResponse> call, Throwable t) {
                        ALog.e("error", "", t);
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
                                        if(!response.isSuccessful()) {
                                            Toast.makeText(MandirPageActivity.this,
                                                    "Unable to fetch details. Please check internet connection & try again later!",
                                                    Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        FeedPageResponse feedPageResponse = response.body();
                                        if (Utils.isEmpty(feedPageResponse.getFeedItems())) {
                                            Toast.makeText(MandirPageActivity.this,
                                                    "No more Feed Items. Thank You for Viewing!", Toast.LENGTH_SHORT).show();
                                            noMorePaginationItems = true;
                                            return;
                                        }
                                        List<GenericPageCardItemModel<MandirPageRVAdapter.MandirPageViewTypes>> newMediaObjects =
                                                convert(feedPageResponse, MandirPageRVAdapter.MandirPageViewTypes.FEED_ITEM);
                                        // int currentSize = mediaObjects.size();
                                        mediaObjects.remove(mediaObjects.size() - 1);
                                        mediaObjects.addAll(newMediaObjects);
                                        mediaObjects.add(getFooter());
                                        adapter.get().notifyDataSetChanged();
                                        // adapter.notifyItemRangeInserted(currentSize, moreMediaObjects.size());

                                        curatedFeedPaginationKey = feedPageResponse.getCuratedFeedPaginationKey();
//                                        Toast.makeText(MandirPageActivity.this,
//                                                "More Feed Items added. Please scroll to see more.", Toast.LENGTH_SHORT).show();
                                        isLoading = false;
                                    }

                                    @Override
                                    public void onFailure(Call<FeedPageResponse> call, Throwable t) {
                                        isLoading = false;
                                        Toast.makeText(MandirPageActivity.this,
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
        MandirFeedRequest curatedFeedRequest = new MandirFeedRequest();
        curatedFeedRequest.setLimit(50);
        curatedFeedRequest.setMandirId(mandirId);
        curatedFeedRequest.setForUserId(userId);
        curatedFeedRequest.setCuratedFeedPaginationKey(curatedFeedPaginationKey);
        return myFeedService.getFeedPageOfMandir(curatedFeedRequest);
    }

    private Optional<GenericPageCardItemModel<MandirPageRVAdapter.MandirPageViewTypes>> getPostAMessage(FeedItemHeader feedItemHeader) {
        MandirFeedHeader mandirFeedHeader = feedItemHeader.getMandirFeedHeader();
        if (!mandirFeedHeader.isMyProfilePage()) {
            return Optional.empty();
        }

        List<PostMessageModel.SpinnerTag> spinnerTags =
                mandirFeedHeader.getPostAMessageInfo().getTags()
                        .stream()
                        .map(t -> new PostMessageModel.SpinnerTag(t.getId(), t.getName()))
                        .collect(Collectors.toList());

        PostMessageModel postMessageModel = new PostMessageModel(
                spinnerTags);
        postMessageModel.setAssociatedMandirId(mandirId);
        postMessageModel.setAssociationType(PostAssociationType.MANDIR);
        postMessageModel.setFromUserId(userId);

        return Optional.of(new GenericPageCardItemModel<>(
                postMessageModel,
                MandirPageRVAdapter.MandirPageViewTypes.POST_MESSAGE));
    }

    private GenericPageCardItemModel<MandirPageRVAdapter.MandirPageViewTypes> getHeader(FeedItemHeader feedItemHeader) {
        MandirFeedHeader mandirFeedHeader = feedItemHeader.getMandirFeedHeader();
        return new GenericPageCardItemModel<>(
                new MandirPageHeaderModel(mandirFeedHeader.getName(),
                        mandirFeedHeader.getImageUrls().get(0),
                        mandirFeedHeader.isCurrentUserFollowing(),
                        String.valueOf(mandirFeedHeader.getFollowersCount()),
                        mandirFeedHeader.isMyProfilePage(),
                        getLocation(mandirFeedHeader.getAddress(), true)

                ), MandirPageRVAdapter.MandirPageViewTypes.HEADER);
    }

    private GenericPageCardItemModel<MandirPageRVAdapter.MandirPageViewTypes> getDetails(FeedItemHeader feedItemHeader) {
        MandirFeedHeader mandirFeedHeader = feedItemHeader.getMandirFeedHeader();
        return new GenericPageCardItemModel<>(
                new MandirPageDetailsModel(
                        mandirFeedHeader.getDescription()
                ), MandirPageRVAdapter.MandirPageViewTypes.DETAILS);
    }

    private GenericPageCardItemModel<MandirPageRVAdapter.MandirPageViewTypes> getFooter() {
        return new GenericPageCardItemModel<>(
                new FeedItemFooterModel(
                ), MandirPageRVAdapter.MandirPageViewTypes.FEED_ITEM_FOOTER);
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