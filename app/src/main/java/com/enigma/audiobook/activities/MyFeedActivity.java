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

    private String userId = "65a7936792bb9e2f44a1ea47";
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

    private List<GenericPageCardItemModel<MyFeedRVAdapter.MyFeedViewTypes>> getMediaObjects() {
        List<String> imagesUrl = new ArrayList<>();
        imagesUrl.add("https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/REST+API%2C+Retrofit2%2C+MVVM+Course+SUMMARY.png");
        imagesUrl.add("https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg");
        imagesUrl.add("https://mixkit.imgix.net/videos/preview/mixkit-young-man-at-the-bowling-center-makes-a-shot-49114-0.jpg");
        String detailsHeader = "<h1>Shiva is known as The Destroyer within the Trimurti, the Hindu trinity which also includes Brahma and Vishnu.</h1>  <h2>In the Shaivite tradition, Shiva is the Supreme Lord who creates, protects and transforms the universe.</h2>  <p> In the goddess-oriented Shakta tradition, the Supreme Goddess (Devi) is regarded as the energy and creative power (Shakti) and the equal complementary partner of Shiva.[15][16] Shiva is one of the five equivalent deities in Panchayatana puja of the Smarta tradition of Hinduism</p>";
        String detailsPara = "<p>Shiva is known as The Destroyer within the Trimurti, the Hindu trinity which also includes Brahma and Vishnu.</p>  <p>In the Shaivite tradition, Shiva is the Supreme Lord who creates, protects and transforms the universe.</p>  <p> In the goddess-oriented Shakta tradition, the Supreme Goddess (Devi) is regarded as the energy and creative power (Shakti) and the equal complementary partner of Shiva.[15][16] Shiva is one of the five equivalent deities in Panchayatana puja of the Smarta tradition of Hinduism</p>";
        List<GenericPageCardItemModel<MyFeedRVAdapter.MyFeedViewTypes>> items = new ArrayList<>();
        items.add(new GenericPageCardItemModel<>(
                new MyFeedHeaderModel(21
                ), MyFeedRVAdapter.MyFeedViewTypes.HEADER));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("test_123", ContentUploadStatus.PROCESSED, "Lord Shiva Card 46453",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        null, null, null, null,

                        PostAssociationType.GOD, "godId", "mandirId", "influencerID"), MyFeedRVAdapter.MyFeedViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("test_123", ContentUploadStatus.PROCESSED, "Lord Shiva Card #6573",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        imagesUrl, null, null, null,

                        PostAssociationType.GOD, "godId", "mandirId", "influencerID"), MyFeedRVAdapter.MyFeedViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("test_123", ContentUploadStatus.PROCESSED, "Lord Shiva Card 1246543",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        null, null,
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/MVVM+and+LiveData+for+youtube.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/mvvm+and+livedata.png",

                        PostAssociationType.GOD, "godId", "mandirId", "influencerID"), MyFeedRVAdapter.MyFeedViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("test_123", ContentUploadStatus.PROCESSED, "Lord Shiva Card 56734",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        null,
                        "https://s3-ap-southeast-1.amazonaws.com/he-public-data/Afreen%20Afreen%20(DjRaag.Net)2cc6f8b.mp3",
                        null, null,

                        PostAssociationType.GOD, "godId", "mandirId", "influencerID"), MyFeedRVAdapter.MyFeedViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("test_123", ContentUploadStatus.PROCESSED, "Lord Shiva Card #678",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        null, null,
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Rest+api+teaser+video.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Rest+API+Integration+with+MVVM.png",

                        PostAssociationType.GOD, "godId", "mandirId", "influencerID"), MyFeedRVAdapter.MyFeedViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("test_123", ContentUploadStatus.PROCESSED, "Lord Shiva Card #456",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        null,
                        "https://s3-ap-southeast-1.amazonaws.com/he-public-data/Aik%20-%20Alif-(Mr-Jatt.com)8ae5316.mp3",
                        null, null,

                        PostAssociationType.GOD, "godId", "mandirId", "influencerID"), MyFeedRVAdapter.MyFeedViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("test_123", ContentUploadStatus.PROCESSED, "Lord Shiva Card #35645",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        imagesUrl, null, null, null,

                        PostAssociationType.GOD, "godId", "mandirId", "influencerID"), MyFeedRVAdapter.MyFeedViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemFooterModel(
                ), MyFeedRVAdapter.MyFeedViewTypes.FEED_ITEM_FOOTER));
        return items;
    }

    private List<GenericPageCardItemModel<MyFeedRVAdapter.MyFeedViewTypes>> loadMoreMediaObjects() {
        List<String> imagesUrl = new ArrayList<>();
        imagesUrl.add("https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/REST+API%2C+Retrofit2%2C+MVVM+Course+SUMMARY.png");
        imagesUrl.add("https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg");
        imagesUrl.add("https://mixkit.imgix.net/videos/preview/mixkit-young-man-at-the-bowling-center-makes-a-shot-49114-0.jpg");
        List<GenericPageCardItemModel<MyFeedRVAdapter.MyFeedViewTypes>> items = new ArrayList<>();
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("test_123", ContentUploadStatus.PROCESSED, "Lord Shiva Card #1212 ",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is another msg from Shiva. Hi",
                        null, null, null, null,

                        PostAssociationType.GOD, "godId", "mandirId", "influencerID"), MyFeedRVAdapter.MyFeedViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("test_123", ContentUploadStatus.PROCESSED, "Lord Shiva Card #10234",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is another msg from Shiva. Hi",
                        imagesUrl, null, null, null,

                        PostAssociationType.GOD, "godId", "mandirId", "influencerID"), MyFeedRVAdapter.MyFeedViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("test_123", ContentUploadStatus.PROCESSED, "Lord Shiva Whats going with this card",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is another msg from Shiva. Hi",
                        null, null,
                        "https://assets.mixkit.co/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-large.mp4",
                        "https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg",

                        PostAssociationType.GOD, "godId", "mandirId", "influencerID"), MyFeedRVAdapter.MyFeedViewTypes.FEED_ITEM));

        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("test_123", ContentUploadStatus.PROCESSED, "Lord Shiva Whats going with this card #2",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is another msg from Shiva. Hi",
                        null, null,
                        "https://assets.mixkit.co/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-large.mp4",
                        "https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg",

                        PostAssociationType.GOD, "godId", "mandirId", "influencerID"), MyFeedRVAdapter.MyFeedViewTypes.FEED_ITEM));

        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("test_123", ContentUploadStatus.PROCESSED, "Lord Shiva Whats going with this card #3",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is another msg from Shiva. Hi",
                        null, null,
                        "https://assets.mixkit.co/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-large.mp4",
                        "https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg",

                        PostAssociationType.GOD, "godId", "mandirId", "influencerID"), MyFeedRVAdapter.MyFeedViewTypes.FEED_ITEM));

        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("test_123", ContentUploadStatus.PROCESSED, "Lord Shiva",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is another msg from Shiva. Hi",
                        null,
                        "https://s3-ap-southeast-1.amazonaws.com/he-public-data/Aik%20-%20Alif-(Mr-Jatt.com)8ae5316.mp3",
                        null, null,

                        PostAssociationType.GOD, "godId", "mandirId", "influencerID"), MyFeedRVAdapter.MyFeedViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemFooterModel(
                ), MyFeedRVAdapter.MyFeedViewTypes.FEED_ITEM_FOOTER));
        return items;
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