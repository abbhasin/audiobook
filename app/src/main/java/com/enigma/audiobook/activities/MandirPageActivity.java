package com.enigma.audiobook.activities;

import static com.enigma.audiobook.proxies.adapters.ModelAdapters.convert;
import static com.enigma.audiobook.utils.Utils.initGlide;

import android.content.Intent;
import android.os.Bundle;
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
import com.enigma.audiobook.proxies.MyFeedService;
import com.enigma.audiobook.proxies.RetrofitFactory;
import com.enigma.audiobook.recyclers.PlayableFeedBasedRecyclerView;
import com.enigma.audiobook.utils.ALog;
import com.enigma.audiobook.utils.ActivityResultLauncherProvider;
import com.enigma.audiobook.utils.PostAMessageUtils;
import com.enigma.audiobook.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MandirPageActivity extends AppCompatActivity implements ActivityResultLauncherProvider {

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

        recyclerView = findViewById(R.id.mandirPageRecyclerView);
        initRecyclerView();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setupImagesPicker();
        setupVideoPicker();
        setupAudioPicker();
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

    private void initRecyclerView() {
        MediaController mediaController = new MediaController(this);
        recyclerView.setMediaController(mediaController);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        List<GenericPageCardItemModel<MandirPageRVAdapter.MandirPageViewTypes>> mediaObjects = new ArrayList<>();
        myFeedService = RetrofitFactory.getInstance().createService(MyFeedService.class);
        Call<FeedPageResponse> feedPageResponseCall = getFeed();
        feedPageResponseCall.enqueue(new Callback<FeedPageResponse>() {
            @Override
            public void onResponse(Call<FeedPageResponse> call, Response<FeedPageResponse> response) {
                ALog.i("TAG", "something:" + response.isSuccessful() + "  " + response.message());

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
                adapter.set(new MandirPageRVAdapter(initGlide(MandirPageActivity.this),
                        mediaObjects, MandirPageActivity.this));
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
                        curatedFeedResponseCall.enqueue(new Callback<FeedPageResponse>() {
                            @Override
                            public void onResponse(Call<FeedPageResponse> call, Response<FeedPageResponse> response) {
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
                                Toast.makeText(MandirPageActivity.this,
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
        MandirFeedRequest curatedFeedRequest = new MandirFeedRequest();
        curatedFeedRequest.setLimit(1);
        curatedFeedRequest.setMandirId("65c3dec10568b52d596ef147");
        curatedFeedRequest.setForUserId("65a7936792bb9e2f44a1ea47");
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

        return Optional.of(new GenericPageCardItemModel<>(
                new PostMessageModel(
                        spinnerTags,
                        new ArrayList<>(), "", ""),
                MandirPageRVAdapter.MandirPageViewTypes.POST_MESSAGE));
    }

    private GenericPageCardItemModel<MandirPageRVAdapter.MandirPageViewTypes> getHeader(FeedItemHeader feedItemHeader) {
        MandirFeedHeader mandirFeedHeader = feedItemHeader.getMandirFeedHeader();
        return new GenericPageCardItemModel<>(
                new MandirPageHeaderModel(mandirFeedHeader.getName(),
                        mandirFeedHeader.getImageUrls().get(0),
                        true, String.valueOf(mandirFeedHeader.getFollowersCount()),
                        mandirFeedHeader.isMyProfilePage(),
                        "A-3/289 Gurudwara Janak Puri, New Delhi - 110058"

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

    private List<GenericPageCardItemModel<MandirPageRVAdapter.MandirPageViewTypes>> getMediaObjects() {
        List<String> imagesUrl = new ArrayList<>();
        imagesUrl.add("https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/REST+API%2C+Retrofit2%2C+MVVM+Course+SUMMARY.png");
        imagesUrl.add("https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg");
        imagesUrl.add("https://mixkit.imgix.net/videos/preview/mixkit-young-man-at-the-bowling-center-makes-a-shot-49114-0.jpg");
        String detailsHeader = "A gurdwara or gurudwara (Punjabi: ਗੁਰਦੁਆਰਾ gurdu'ārā, meaning ‘Door to the Guru’) is a place of assembly and worship for Sikhs. Sikhs also refer to gurdwaras as Gurdwara Sahib. People from all faiths and religions are welcomed in gurdwaras. Each gurdwara has a Darbar Sahib where the Guru Granth Sahib is placed on a takhat (an elevated throne) in a prominent central position. Any congregant (sometimes with specialized training, in which case they are known by the term granthi) may recite, sing, and explain the verses from the Guru Granth Sahib, in the presence of the rest of the congregation";

        List<GenericPageCardItemModel<MandirPageRVAdapter.MandirPageViewTypes>> items = new ArrayList<>();
        items.add(new GenericPageCardItemModel<>(
                new MandirPageHeaderModel("Lord Shiva",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        true, "980", false,
                        "A-3/289 Gurudwara Janak Puri, New Delhi - 110058"

                ), MandirPageRVAdapter.MandirPageViewTypes.HEADER));
        items.add(new GenericPageCardItemModel<>(
                new MandirPageDetailsModel(
                        detailsHeader
                ), MandirPageRVAdapter.MandirPageViewTypes.DETAILS));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card 46453",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        null, null, null, null

                ), MandirPageRVAdapter.MandirPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card #6573",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        imagesUrl, null, null, null

                ), MandirPageRVAdapter.MandirPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card 1246543",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        null, null,
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/MVVM+and+LiveData+for+youtube.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/mvvm+and+livedata.png"

                ), MandirPageRVAdapter.MandirPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card 56734",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        null,
                        "https://s3-ap-southeast-1.amazonaws.com/he-public-data/Afreen%20Afreen%20(DjRaag.Net)2cc6f8b.mp3",
                        null, null

                ), MandirPageRVAdapter.MandirPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card #678",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        null, null,
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Rest+api+teaser+video.mp4",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Rest+API+Integration+with+MVVM.png"

                ), MandirPageRVAdapter.MandirPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card #456",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        null,
                        "https://s3-ap-southeast-1.amazonaws.com/he-public-data/Aik%20-%20Alif-(Mr-Jatt.com)8ae5316.mp3",
                        null, null

                ), MandirPageRVAdapter.MandirPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card #35645",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is Shiva. Hello World",
                        imagesUrl, null, null, null

                ), MandirPageRVAdapter.MandirPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemFooterModel(
                ), MandirPageRVAdapter.MandirPageViewTypes.FEED_ITEM_FOOTER));
        return items;
    }

    private List<GenericPageCardItemModel<MandirPageRVAdapter.MandirPageViewTypes>> loadMoreMediaObjects() {
        List<String> imagesUrl = new ArrayList<>();
        imagesUrl.add("https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/REST+API%2C+Retrofit2%2C+MVVM+Course+SUMMARY.png");
        imagesUrl.add("https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg");
        imagesUrl.add("https://mixkit.imgix.net/videos/preview/mixkit-young-man-at-the-bowling-center-makes-a-shot-49114-0.jpg");
        List<GenericPageCardItemModel<MandirPageRVAdapter.MandirPageViewTypes>> items = new ArrayList<>();
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card #1212 ",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is another msg from Shiva. Hi",
                        null, null, null, null

                ), MandirPageRVAdapter.MandirPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Card #10234",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is another msg from Shiva. Hi",
                        imagesUrl, null, null, null

                ), MandirPageRVAdapter.MandirPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Whats going with this card",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is another msg from Shiva. Hi",
                        null, null,
                        "https://assets.mixkit.co/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-large.mp4",
                        "https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg"

                ), MandirPageRVAdapter.MandirPageViewTypes.FEED_ITEM));

        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Whats going with this card #2",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is another msg from Shiva. Hi",
                        null, null,
                        "https://assets.mixkit.co/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-large.mp4",
                        "https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg"

                ), MandirPageRVAdapter.MandirPageViewTypes.FEED_ITEM));

        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva Whats going with this card #3",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is another msg from Shiva. Hi",
                        null, null,
                        "https://assets.mixkit.co/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-large.mp4",
                        "https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg"

                ), MandirPageRVAdapter.MandirPageViewTypes.FEED_ITEM));

        items.add(new GenericPageCardItemModel<>(
                new FeedItemModel("Lord Shiva",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                        "some title", "This is another msg from Shiva. Hi",
                        null,
                        "https://s3-ap-southeast-1.amazonaws.com/he-public-data/Aik%20-%20Alif-(Mr-Jatt.com)8ae5316.mp3",
                        null, null

                ), MandirPageRVAdapter.MandirPageViewTypes.FEED_ITEM));
        items.add(new GenericPageCardItemModel<>(
                new FeedItemFooterModel(
                ), MandirPageRVAdapter.MandirPageViewTypes.FEED_ITEM_FOOTER));
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