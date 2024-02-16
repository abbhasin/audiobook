package com.enigma.audiobook.activities;

import static com.enigma.audiobook.proxies.adapters.ModelAdapters.convert;
import static com.enigma.audiobook.utils.Utils.initGlide;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;

import com.enigma.audiobook.R;
import com.enigma.audiobook.adapters.PujariPageRVAdapter;
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
import com.enigma.audiobook.proxies.MyFeedService;
import com.enigma.audiobook.proxies.RetrofitFactory;
import com.enigma.audiobook.recyclers.PlayableFeedBasedRecyclerView;
import com.enigma.audiobook.utils.ALog;
import com.enigma.audiobook.utils.ActivityResultLauncherProvider;
import com.enigma.audiobook.utils.PostAMessageUtils;
import com.enigma.audiobook.utils.Utils;
import com.google.android.gms.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PujariPageActivity extends AppCompatActivity implements ActivityResultLauncherProvider {

    private PlayableFeedBasedRecyclerView recyclerView;
    private PujariPageRVAdapter adapter;
    ActivityResultLauncher<PickVisualMediaRequest> pickMultipleImages;
    ActivityResultLauncher<PickVisualMediaRequest> pickVideo;
    ActivityResultLauncher<Intent> pickAudio;

    private MyFeedService myFeedService;
    private CuratedFeedPaginationKey curatedFeedPaginationKey;
    private boolean isLoading = false;
    private boolean noMorePaginationItems = false;
    int ctr = 0;

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

        recyclerView = findViewById(R.id.pujariPageRecyclerView);
        initRecyclerView();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setupImagesPicker();
        setupVideoPicker();
        setupAudioPicker();
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
        return adapter.getCardItems()
                .stream()
                .filter(card -> card.getType() == PujariPageRVAdapter.PujariPageViewTypes.POST_MESSAGE)
                .findFirst()
                .map(genricObj -> (PostMessageModel) genricObj.getCardItem());
    }

    private void initRecyclerView() {
        MediaController mediaController = new MediaController(this);
        recyclerView.setMediaController(mediaController);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        List<GenericPageCardItemModel<PujariPageRVAdapter.PujariPageViewTypes>> mediaObjects = new ArrayList<>();
        myFeedService = RetrofitFactory.getInstance().createService(MyFeedService.class);
        Call<FeedPageResponse> feedPageResponseCall = getFeed();
        feedPageResponseCall.enqueue(new Callback<FeedPageResponse>() {
            @Override
            public void onResponse(Call<FeedPageResponse> call, Response<FeedPageResponse> response) {
                ALog.i("TAG", "something:" + response.isSuccessful() + "  " + response.message());

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
                adapter = new PujariPageRVAdapter(initGlide(PujariPageActivity.this),
                        mediaObjects, PujariPageActivity.this);
                recyclerView.setAdapter(adapter);
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
                        curatedFeedResponseCall.enqueue(new Callback<FeedPageResponse>() {
                            @Override
                            public void onResponse(Call<FeedPageResponse> call, Response<FeedPageResponse> response) {
                                FeedPageResponse feedPageResponse = response.body();
                                if (CollectionUtils.isEmpty(feedPageResponse.getFeedItems())) {
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
                                adapter.notifyDataSetChanged();
                                // adapter.notifyItemRangeInserted(currentSize, moreMediaObjects.size());

                                curatedFeedPaginationKey = feedPageResponse.getCuratedFeedPaginationKey();
                                Toast.makeText(PujariPageActivity.this,
                                        "More Feed Items added. Please scroll to see more.", Toast.LENGTH_SHORT).show();
                                isLoading = false;
                            }

                            @Override
                            public void onFailure(Call<FeedPageResponse> call, Throwable t) {
                                isLoading = false;
                            }
                        });
                    }
                }
            }
        });
    }

    private Call<FeedPageResponse> getFeed() {
        InfluencerFeedRequest curatedFeedRequest = new InfluencerFeedRequest();
        curatedFeedRequest.setLimit(20);
        curatedFeedRequest.setInfluencerId("");
        curatedFeedRequest.setForUserId("65c5034dc76eef0b30919614");
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

        return Optional.of(new GenericPageCardItemModel<>(
                new PostMessageModel(
                        spinnerTags,
                        new ArrayList<>(), "", ""),
                PujariPageRVAdapter.PujariPageViewTypes.POST_MESSAGE));
    }

    private GenericPageCardItemModel<PujariPageRVAdapter.PujariPageViewTypes> getHeader(FeedItemHeader feedItemHeader) {
        InfluencerFeedHeader influencerFeedHeader = feedItemHeader.getInfluencerFeedHeader();
        return new GenericPageCardItemModel<>(
                new PujariPageHeaderModel(influencerFeedHeader.getName(),
                        influencerFeedHeader.getImageUrls().get(0),
                        true, String.valueOf(influencerFeedHeader.getFollowersCount()),
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

    private List<GenericPageCardItemModel<PujariPageRVAdapter.PujariPageViewTypes>> getMediaObjects() {
        List<String> imagesUrl = new ArrayList<>();
        imagesUrl.add("https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/REST+API%2C+Retrofit2%2C+MVVM+Course+SUMMARY.png");
        imagesUrl.add("https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg");
        imagesUrl.add("https://mixkit.imgix.net/videos/preview/mixkit-young-man-at-the-bowling-center-makes-a-shot-49114-0.jpg");
        String detailsHeader = "<h1>Shiva is known as The Destroyer within the Trimurti, the Hindu trinity which also includes Brahma and Vishnu.</h1>  <h2>In the Shaivite tradition, Shiva is the Supreme Lord who creates, protects and transforms the universe.</h2>  <p> In the goddess-oriented Shakta tradition, the Supreme Goddess (Devi) is regarded as the energy and creative power (Shakti) and the equal complementary partner of Shiva.[15][16] Shiva is one of the five equivalent deities in Panchayatana puja of the Smarta tradition of Hinduism</p>";
        String detailsPara = "<p>Shiva is known as The Destroyer within the Trimurti, the Hindu trinity which also includes Brahma and Vishnu.</p>  <p>In the Shaivite tradition, Shiva is the Supreme Lord who creates, protects and transforms the universe.</p>  <p> In the goddess-oriented Shakta tradition, the Supreme Goddess (Devi) is regarded as the energy and creative power (Shakti) and the equal complementary partner of Shiva.[15][16] Shiva is one of the five equivalent deities in Panchayatana puja of the Smarta tradition of Hinduism</p>";
        List<GenericPageCardItemModel<PujariPageRVAdapter.PujariPageViewTypes>> items = new ArrayList<>();
        items.add(new GenericPageCardItemModel<>(
                new PujariPageHeaderModel("Lord Shiva",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        true, "980", false

                ), PujariPageRVAdapter.PujariPageViewTypes.HEADER));
        items.add(new GenericPageCardItemModel<>(
                new PujariPageDetailsModel(
                        detailsHeader
                ), PujariPageRVAdapter.PujariPageViewTypes.DETAILS));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card 46453",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        null, null, null, null

                ), PujariPageRVAdapter.PujariPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card #6573",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        imagesUrl, null, null, null

                ), PujariPageRVAdapter.PujariPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card 1246543",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        null, null,
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/MVVM+and+LiveData+for+youtube.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/mvvm+and+livedata.png"

                ), PujariPageRVAdapter.PujariPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card 56734",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        null,
                        "https://s3-ap-southeast-1.amazonaws.com/he-public-data/Afreen%20Afreen%20(DjRaag.Net)2cc6f8b.mp3",
                        null, null

                ), PujariPageRVAdapter.PujariPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card #678",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        null, null,
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Rest+api+teaser+video.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Rest+API+Integration+with+MVVM.png"

                ), PujariPageRVAdapter.PujariPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card #456",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        null,
                        "https://s3-ap-southeast-1.amazonaws.com/he-public-data/Aik%20-%20Alif-(Mr-Jatt.com)8ae5316.mp3",
                        null, null

                ), PujariPageRVAdapter.PujariPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card #35645",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        imagesUrl, null, null, null

                ), PujariPageRVAdapter.PujariPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemFooterModel(
                ), PujariPageRVAdapter.PujariPageViewTypes.FEED_ITEM_FOOTER));
        return items;
    }

    private List<GenericPageCardItemModel<PujariPageRVAdapter.PujariPageViewTypes>> loadMoreMediaObjects() {
        List<String> imagesUrl = new ArrayList<>();
        imagesUrl.add("https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/REST+API%2C+Retrofit2%2C+MVVM+Course+SUMMARY.png");
        imagesUrl.add("https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg");
        imagesUrl.add("https://mixkit.imgix.net/videos/preview/mixkit-young-man-at-the-bowling-center-makes-a-shot-49114-0.jpg");
        List<GenericPageCardItemModel<PujariPageRVAdapter.PujariPageViewTypes>> items = new ArrayList<>();
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card #1212 ",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is another msg from Shiva. Hi",
                        null, null, null, null

                ), PujariPageRVAdapter.PujariPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card #10234",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is another msg from Shiva. Hi",
                        imagesUrl, null, null, null

                ), PujariPageRVAdapter.PujariPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Whats going with this card",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is another msg from Shiva. Hi",
                        null, null,
                        "https://assets.mixkit.co/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-large.mp4",
                        "https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg"

                ), PujariPageRVAdapter.PujariPageViewTypes.FEED_ITEM));

        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Whats going with this card #2",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is another msg from Shiva. Hi",
                        null, null,
                        "https://assets.mixkit.co/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-large.mp4",
                        "https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg"

                ), PujariPageRVAdapter.PujariPageViewTypes.FEED_ITEM));

        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Whats going with this card #3",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is another msg from Shiva. Hi",
                        null, null,
                        "https://assets.mixkit.co/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-large.mp4",
                        "https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg"

                ), PujariPageRVAdapter.PujariPageViewTypes.FEED_ITEM));

        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is another msg from Shiva. Hi",
                        null,
                        "https://s3-ap-southeast-1.amazonaws.com/he-public-data/Aik%20-%20Alif-(Mr-Jatt.com)8ae5316.mp3",
                        null, null

                ), PujariPageRVAdapter.PujariPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemFooterModel(
                ), PujariPageRVAdapter.PujariPageViewTypes.FEED_ITEM_FOOTER));
        return items;
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