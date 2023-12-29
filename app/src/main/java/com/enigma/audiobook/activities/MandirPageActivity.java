package com.enigma.audiobook.activities;

import static com.enigma.audiobook.utils.Utils.initGlide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;

import com.enigma.audiobook.R;
import com.enigma.audiobook.adapters.MandirPageRVAdapter;
import com.enigma.audiobook.adapters.MandirPageRVAdapter;
import com.enigma.audiobook.adapters.MandirPageRVAdapter;
import com.enigma.audiobook.models.FeedItemFooterModel;
import com.enigma.audiobook.models.FeedItemModel;
import com.enigma.audiobook.models.GenericPageCardItemModel;
import com.enigma.audiobook.models.GodPageDetailsModel;
import com.enigma.audiobook.models.GodPageHeaderModel;
import com.enigma.audiobook.models.MandirPageDetailsModel;
import com.enigma.audiobook.models.MandirPageHeaderModel;
import com.enigma.audiobook.recyclers.PlayableFeedBasedRecyclerView;
import com.enigma.audiobook.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MandirPageActivity extends AppCompatActivity {

    private PlayableFeedBasedRecyclerView recyclerView;
    private boolean isLoading = false;
    int ctr = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mandir_page);

        recyclerView = findViewById(R.id.mandirPageRecyclerView);
        initRecyclerView();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void initRecyclerView() {
        MediaController mediaController = new MediaController(this);
        recyclerView.setMediaController(mediaController);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        List<GenericPageCardItemModel<MandirPageRVAdapter.MandirPageViewTypes>> mediaObjects = getMediaObjects();
//        mediaObjects.addAll(loadMoreMediaObjects());
        recyclerView.setMediaObjects(mediaObjects);

        MandirPageRVAdapter adapter = new MandirPageRVAdapter(initGlide(this), mediaObjects);
        recyclerView.setAdapter(adapter);

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

                if (!isLoading) {
                    if (linearLayoutManager != null &&
                            linearLayoutManager.findLastCompletelyVisibleItemPosition() ==
                                    mediaObjects.size() - 2) {
                        if (ctr == 0) {
                            isLoading = true;

                            List<GenericPageCardItemModel<MandirPageRVAdapter.MandirPageViewTypes>>
                                    moreMediaObjects = loadMoreMediaObjects();
//                            int currentSize = mediaObjects.size();
                            mediaObjects.remove(mediaObjects.size() - 1);
                            mediaObjects.addAll(moreMediaObjects);
                            adapter.notifyDataSetChanged();
//                            adapter.notifyItemRangeInserted(currentSize, moreMediaObjects.size());
                            Toast.makeText(MandirPageActivity.this,
                                    "More Feed Items added. Please scroll to see more.", Toast.LENGTH_SHORT).show();

                            isLoading = false;
                            ctr++;
                        } else {
                            Toast.makeText(MandirPageActivity.this,
                                    "You have visited all Feed Items for this page! Thank You! :)", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
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