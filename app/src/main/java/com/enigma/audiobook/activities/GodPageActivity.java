package com.enigma.audiobook.activities;

import static com.enigma.audiobook.proxies.adapters.ModelAdapters.convert;
import static com.enigma.audiobook.utils.Utils.initGlide;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.enigma.audiobook.R;
import com.enigma.audiobook.adapters.GodPageRVAdapter;
import com.enigma.audiobook.adapters.MyFeedRVAdapter;
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
import com.enigma.audiobook.proxies.MyFeedService;
import com.enigma.audiobook.proxies.RetrofitFactory;
import com.enigma.audiobook.recyclers.PlayableFeedBasedRecyclerView;
import com.enigma.audiobook.utils.ALog;
import com.enigma.audiobook.utils.ActivityResultLauncherProvider;
import com.enigma.audiobook.utils.Utils;
import com.google.android.gms.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GodPageActivity extends AppCompatActivity implements ActivityResultLauncherProvider {

    private PlayableFeedBasedRecyclerView recyclerView;
    private GodPageRVAdapter adapter;
    private MediaController mediaController;
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
        setContentView(R.layout.activity_god_page);

        recyclerView = findViewById(R.id.godPageRecyclerView);
        initRecyclerView();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setupImagesPicker();
        setupVideoPicker();
        setupAudioPicker();
    }

    private void setupAudioPicker() {
        pickAudio =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        new ActivityResultCallback<ActivityResult>() {

                            @Override
                            public void onActivityResult(ActivityResult result) {
                                if (result.getResultCode() == RESULT_OK) {
                                    Uri audiUri = result.getData().getData();
                                    if (audiUri != null) {
                                        Optional<PostMessageModel> modelOpt = getPostMessageModel();
                                        if (modelOpt.isPresent()) {
                                            modelOpt.get().clearVideoAudioContent();
                                            modelOpt.get().setMusicUrl(audiUri.toString());
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            }
                        });
    }

    private void setupVideoPicker() {
        pickVideo = this.registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                ALog.i("VideoPicker", "video selected: " + uri);
                Optional<PostMessageModel> modelOpt = getPostMessageModel();
                if (modelOpt.isPresent()) {
                    modelOpt.get().clearVideoAudioContent();
                    modelOpt.get().setVideoUrl(uri.toString());
                    adapter.notifyDataSetChanged();
                }

            } else {
                ALog.i("VideoPicker", "No media selected");
            }

        });
    }

    private Optional<PostMessageModel> getPostMessageModel() {
        return adapter.getCardItems()
                .stream()
                .filter(card -> card.getType() == GodPageRVAdapter.GodPageViewTypes.POST_MESSAGE)
                .findFirst()
                .map(genricObj -> (PostMessageModel) genricObj.getCardItem());
    }

    private void setupImagesPicker() {
        pickMultipleImages = this.registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(10), uris -> {
            if (!uris.isEmpty()) {
                ALog.i("PhotoPicker", "Number of items selected: " + uris.size() + " uris:" + uris);
                List<String> imagesUrl = new ArrayList<>(uris.stream().map(uri -> uri.toString()).collect(Collectors.toList()));
                Optional<PostMessageModel> modelOpt = getPostMessageModel();
                if (modelOpt.isPresent()) {
                    modelOpt.get().clearVideoAudioContent();
                    modelOpt.get().setImagesUrl(imagesUrl);
                    adapter.notifyDataSetChanged();
                }
            } else {
                ALog.i("PhotoPicker", "No media selected");
            }

        });
    }

    private void initRecyclerView() {
        mediaController = new MediaController(this);
        recyclerView.setMediaController(mediaController);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        List<GenericPageCardItemModel<GodPageRVAdapter.GodPageViewTypes>> mediaObjects = new ArrayList<>();
        myFeedService = RetrofitFactory.getInstance().createService(MyFeedService.class);
        Call<FeedPageResponse> feedPageResponseCall = getFeed();
        feedPageResponseCall.enqueue(new Callback<FeedPageResponse>() {
            @Override
            public void onResponse(Call<FeedPageResponse> call, Response<FeedPageResponse> response) {
                ALog.i("TAG","something:"+response.isSuccessful()+"  "+response.message());

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

                adapter = new GodPageRVAdapter(initGlide(GodPageActivity.this),
                        mediaObjects, GodPageActivity.this);
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
                                adapter.notifyDataSetChanged();
                                // adapter.notifyItemRangeInserted(currentSize, moreMediaObjects.size());
                                Toast.makeText(GodPageActivity.this,
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
        GodFeedRequest curatedFeedRequest = new GodFeedRequest();
        curatedFeedRequest.setLimit(2);
        curatedFeedRequest.setGodId("65c234631298c936bf93450a");
        curatedFeedRequest.setForUserId("65c5034dc76eef0b30919614");
        curatedFeedRequest.setCuratedFeedPaginationKey(curatedFeedPaginationKey);
        return myFeedService.getFeedPageOfGod(curatedFeedRequest);
    }

    private GenericPageCardItemModel<GodPageRVAdapter.GodPageViewTypes> getHeader(FeedItemHeader feedItemHeader) {
        GodFeedHeader godFeedHeader = feedItemHeader.getGodFeedHeader();
        return new GenericPageCardItemModel<>(
                new GodPageHeaderModel(godFeedHeader.getName(),
                        godFeedHeader.getImageUrls().get(0),
                        false, String.valueOf(godFeedHeader.getFollowersCount()),
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

        return Optional.of(new GenericPageCardItemModel<>(
                new PostMessageModel(
                        spinnerTags,
                        new ArrayList<>(), "", ""),
                GodPageRVAdapter.GodPageViewTypes.POST_MESSAGE));

    }

    private GenericPageCardItemModel<GodPageRVAdapter.GodPageViewTypes> getFooter() {
        return new GenericPageCardItemModel<>(
                new FeedItemFooterModel(
                ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM_FOOTER);
    }

    private List<GenericPageCardItemModel<GodPageRVAdapter.GodPageViewTypes>> getMediaObjects() {
        List<String> imagesUrl = new ArrayList<>();
        imagesUrl.add("https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/REST+API%2C+Retrofit2%2C+MVVM+Course+SUMMARY.png");
        imagesUrl.add("https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg");
        imagesUrl.add("https://mixkit.imgix.net/videos/preview/mixkit-young-man-at-the-bowling-center-makes-a-shot-49114-0.jpg");
        String detailsHeader = "<h1>Shiva is known as The Destroyer within the Trimurti, the Hindu trinity which also includes Brahma and Vishnu.</h1>  <h2>In the Shaivite tradition, Shiva is the Supreme Lord who creates, protects and transforms the universe.</h2>  <p> In the goddess-oriented Shakta tradition, the Supreme Goddess (Devi) is regarded as the energy and creative power (Shakti) and the equal complementary partner of Shiva.[15][16] Shiva is one of the five equivalent deities in Panchayatana puja of the Smarta tradition of Hinduism</p>";
        String detailsPara = "<p>Shiva is known as The Destroyer within the Trimurti, the Hindu trinity which also includes Brahma and Vishnu.</p>  <p>In the Shaivite tradition, Shiva is the Supreme Lord who creates, protects and transforms the universe.</p>  <p> In the goddess-oriented Shakta tradition, the Supreme Goddess (Devi) is regarded as the energy and creative power (Shakti) and the equal complementary partner of Shiva.[15][16] Shiva is one of the five equivalent deities in Panchayatana puja of the Smarta tradition of Hinduism</p>";
        List<GenericPageCardItemModel<GodPageRVAdapter.GodPageViewTypes>> items = new ArrayList<>();
        items.add(new GenericPageCardItemModel<>(
                new GodPageHeaderModel("Lord Shiva",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        true, "980", false

                ), GodPageRVAdapter.GodPageViewTypes.HEADER));
        items.add(new GenericPageCardItemModel<>(
                new GodPageDetailsModel(
                        detailsHeader
                ), GodPageRVAdapter.GodPageViewTypes.DETAILS));
        items.add(new GenericPageCardItemModel<>(
                new PostMessageModel(
                        Arrays.asList(new PostMessageModel.SpinnerTag("001", "Select God's Tag"), new PostMessageModel.SpinnerTag("123", "Shiva"), new PostMessageModel.SpinnerTag("723", "Vishnu"), new PostMessageModel.SpinnerTag("789", "Kali Ma")),
                        new ArrayList<>(), "", ""), GodPageRVAdapter.GodPageViewTypes.POST_MESSAGE));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card 46453",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        null, null, null, null

                ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card #6573",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        imagesUrl, null, null, null

                ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card 1246543",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        null, null,
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/MVVM+and+LiveData+for+youtube.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/mvvm+and+livedata.png"

                ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card 56734",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        null,
                        "https://s3-ap-southeast-1.amazonaws.com/he-public-data/Afreen%20Afreen%20(DjRaag.Net)2cc6f8b.mp3",
                        null, null

                ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card #678",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        null, null,
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Rest+api+teaser+video.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Rest+API+Integration+with+MVVM.png"

                ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card #456",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        null,
                        "https://s3-ap-southeast-1.amazonaws.com/he-public-data/Aik%20-%20Alif-(Mr-Jatt.com)8ae5316.mp3",
                        null, null

                ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card #35645",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        imagesUrl, null, null, null

                ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemFooterModel(
                ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM_FOOTER));
        return items;
    }

    private List<GenericPageCardItemModel<GodPageRVAdapter.GodPageViewTypes>> loadMoreMediaObjects() {
        List<String> imagesUrl = new ArrayList<>();
        imagesUrl.add("https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/REST+API%2C+Retrofit2%2C+MVVM+Course+SUMMARY.png");
        imagesUrl.add("https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg");
        imagesUrl.add("https://mixkit.imgix.net/videos/preview/mixkit-young-man-at-the-bowling-center-makes-a-shot-49114-0.jpg");
        List<GenericPageCardItemModel<GodPageRVAdapter.GodPageViewTypes>> items = new ArrayList<>();
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card #1212 ",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is another msg from Shiva. Hi",
                        null, null, null, null

                ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card #10234",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is another msg from Shiva. Hi",
                        imagesUrl, null, null, null

                ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Whats going with this card",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is another msg from Shiva. Hi",
                        null, null,
                        "https://assets.mixkit.co/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-large.mp4",
                        "https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg"

                ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM));

        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Whats going with this card #2",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is another msg from Shiva. Hi",
                        null, null,
                        "https://assets.mixkit.co/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-large.mp4",
                        "https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg"

                ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM));

        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Whats going with this card #3",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is another msg from Shiva. Hi",
                        null, null,
                        "https://assets.mixkit.co/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-large.mp4",
                        "https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg"

                ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM));

        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is another msg from Shiva. Hi",
                        null,
                        "https://s3-ap-southeast-1.amazonaws.com/he-public-data/Aik%20-%20Alif-(Mr-Jatt.com)8ae5316.mp3",
                        null, null

                ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemFooterModel(
                ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM_FOOTER));
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