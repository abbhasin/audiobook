package com.enigma.audiobook.activities;

import static com.enigma.audiobook.utils.Utils.initGlide;

import android.os.Bundle;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.enigma.audiobook.R;
import com.enigma.audiobook.adapters.GodPageRVAdapter;
import com.enigma.audiobook.models.FeedItemModel;
import com.enigma.audiobook.models.GenericPageCardItemModel;
import com.enigma.audiobook.models.GodPageDetailsModel;
import com.enigma.audiobook.models.GodPageHeaderModel;
import com.enigma.audiobook.models.SwipeVideoMediaModel;
import com.enigma.audiobook.models.VideoMediaModel;
import com.enigma.audiobook.recyclers.PlayableFeedBasedRecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GodPageActivity extends AppCompatActivity {

    private PlayableFeedBasedRecyclerView recyclerView;
    private boolean isLoading = false;
    int ctr = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_god_page);

        recyclerView = findViewById(R.id.godPageRecyclerView);
        initRecyclerView();
    }

    private void initRecyclerView() {
        MediaController mediaController = new MediaController(this);
        recyclerView.setMediaController(mediaController);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        List<GenericPageCardItemModel<GodPageRVAdapter.GodPageViewTypes>> mediaObjects = getMediaObjects();
        recyclerView.setMediaObjects(mediaObjects);

        GodPageRVAdapter adapter = new GodPageRVAdapter(initGlide(this), mediaObjects);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading) {
                    if (linearLayoutManager != null &&
                            linearLayoutManager.findLastCompletelyVisibleItemPosition() ==
                                    mediaObjects.size() - 1) {
                        if (ctr == 0) {
                            isLoading = true;

                            List<GenericPageCardItemModel<GodPageRVAdapter.GodPageViewTypes>>
                                    moreMediaObjects = loadMoreMediaObjects();
                            int currentSize = mediaObjects.size();
                            mediaObjects.addAll(moreMediaObjects);
//                            adapter.notifyDataSetChanged();
                            adapter.notifyItemRangeInserted(currentSize, moreMediaObjects.size());
                            Toast.makeText(GodPageActivity.this,
                                    "More Feed Items added. Please scroll to see more.", Toast.LENGTH_SHORT).show();

                            isLoading = false;
                            ctr++;
                        } else {
                            Toast.makeText(GodPageActivity.this,
                                    "You have visited all Feed Items for this page! Thank You! :)", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    private List<GenericPageCardItemModel<GodPageRVAdapter.GodPageViewTypes>> getMediaObjects() {
        List<String> imagesUrl = new ArrayList<>();
        imagesUrl.add("https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/REST+API%2C+Retrofit2%2C+MVVM+Course+SUMMARY.png");
        imagesUrl.add("https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg");
        imagesUrl.add("https://mixkit.imgix.net/videos/preview/mixkit-young-man-at-the-bowling-center-makes-a-shot-49114-0.jpg");


        GenericPageCardItemModel[] MEDIA_OBJECTS = {
                new GenericPageCardItemModel<>(
                        new GodPageHeaderModel("Lord Shiva",
                        "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                                true, "980", false

                ), GodPageRVAdapter.GodPageViewTypes.HEADER),
                new GenericPageCardItemModel<>(
                        new GodPageDetailsModel("Shiva is known as The Destroyer within the Trimurti, the Hindu trinity which also includes Brahma and Vishnu.\n" +
                                "\n" +
                                "In the Shaivite tradition, Shiva is the Supreme Lord who creates, protects and transforms the universe.\n" +
                                "\n" +
                                " In the goddess-oriented Shakta tradition, the Supreme Goddess (Devi) is regarded as the energy and creative power (Shakti) and the equal complementary partner of Shiva.[15][16] Shiva is one of the five equivalent deities in Panchayatana puja of the Smarta tradition of Hinduism"

                        ), GodPageRVAdapter.GodPageViewTypes.DETAILS),
                new GenericPageCardItemModel<>(
                        new FeedItemModel("Lord Shiva",
                                "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                                "some title", "This is Shiva. Hello World",
                                null, null, null, null

                        ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM),
                new GenericPageCardItemModel<>(
                        new FeedItemModel("Lord Shiva",
                                "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                                "some title", "This is Shiva. Hello World",
                                imagesUrl, null, null, null

                        ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM),
                new GenericPageCardItemModel<>(
                        new FeedItemModel("Lord Shiva",
                                "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                                "some title", "This is Shiva. Hello World",
                                null, null,
                                "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/MVVM+and+LiveData+for+youtube.mp4",
                                "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/mvvm+and+livedata.png"

                        ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM),
                new GenericPageCardItemModel<>(
                        new FeedItemModel("Lord Shiva",
                                "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                                "some title", "This is Shiva. Hello World",
                                null,
                                "https://s3-ap-southeast-1.amazonaws.com/he-public-data/Afreen%20Afreen%20(DjRaag.Net)2cc6f8b.mp3",
                                null, null

                        ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM),
                new GenericPageCardItemModel<>(
                        new FeedItemModel("Lord Shiva",
                                "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                                "some title", "This is Shiva. Hello World",
                                null, null,
                                "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Rest+api+teaser+video.mp4",
                                "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Rest+API+Integration+with+MVVM.png"

                        ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM),
                new GenericPageCardItemModel<>(
                        new FeedItemModel("Lord Shiva",
                                "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                                "some title", "This is Shiva. Hello World",
                                null,
                                "https://s3-ap-southeast-1.amazonaws.com/he-public-data/Aik%20-%20Alif-(Mr-Jatt.com)8ae5316.mp3",
                                null, null

                        ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM),
                new GenericPageCardItemModel<>(
                        new FeedItemModel("Lord Shiva",
                                "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                                "some title", "This is Shiva. Hello World",
                                imagesUrl, null, null, null

                        ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM)
        };
        return new ArrayList<>(Arrays.asList(MEDIA_OBJECTS));
    }

    private List<GenericPageCardItemModel<GodPageRVAdapter.GodPageViewTypes>> loadMoreMediaObjects() {
        List<String> imagesUrl = new ArrayList<>();
        imagesUrl.add("https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/REST+API%2C+Retrofit2%2C+MVVM+Course+SUMMARY.png");
        imagesUrl.add("https://mixkit.imgix.net/videos/preview/mixkit-reflection-effect-of-a-young-woman-dancing-in-rollerblades-49092-0.jpg");
        imagesUrl.add("https://mixkit.imgix.net/videos/preview/mixkit-young-man-at-the-bowling-center-makes-a-shot-49114-0.jpg");

        GenericPageCardItemModel[] MEDIA_OBJECTS = {
                new GenericPageCardItemModel<>(
                        new FeedItemModel("Lord Shiva",
                                "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                                "some title", "This is another msg from Shiva. Hi",
                                null, null, null, null

                        ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM),
                new GenericPageCardItemModel<>(
                        new FeedItemModel("Lord Shiva",
                                "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                                "some title", "This is another msg from Shiva. Hi",
                                imagesUrl, null, null, null

                        ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM),
                new GenericPageCardItemModel<>(
                        new FeedItemModel("Lord Shiva",
                                "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                                "some title", "This is another msg from Shiva. Hi",
                                null, null,
                                "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Rest+api+teaser+video.mp4",
                                "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Rest+API+Integration+with+MVVM.png"

                        ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM),
                new GenericPageCardItemModel<>(
                        new FeedItemModel("Lord Shiva",
                                "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.png",
                                "some title", "This is another msg from Shiva. Hi",
                                null,
                                "https://s3-ap-southeast-1.amazonaws.com/he-public-data/Aik%20-%20Alif-(Mr-Jatt.com)8ae5316.mp3",
                                null, null

                        ), GodPageRVAdapter.GodPageViewTypes.FEED_ITEM)
        };
        return new ArrayList<>(Arrays.asList(MEDIA_OBJECTS));
    }
}