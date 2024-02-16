package com.enigma.audiobook.proxies.adapters;

import com.enigma.audiobook.adapters.MyFeedRVAdapter;
import com.enigma.audiobook.backend.models.Darshan;
import com.enigma.audiobook.backend.models.responses.CuratedFeedResponse;
import com.enigma.audiobook.backend.models.responses.FeedPageResponse;
import com.enigma.audiobook.models.FeedItemModel;
import com.enigma.audiobook.models.GenericPageCardItemModel;
import com.enigma.audiobook.models.ModelClassRetriever;
import com.enigma.audiobook.models.SwipeVideoMediaModel;
import com.google.android.gms.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModelAdapters {

    public static List<SwipeVideoMediaModel> convert(List<Darshan> darshans) {
        if (darshans == null) {
            return new ArrayList<>();
        }
        return darshans.stream()
                .map(darshan -> {
                    SwipeVideoMediaModel svmm =
                            new SwipeVideoMediaModel(
                                    darshan.getTitle(),
                                    darshan.getShortDescription(),
                                    darshan.getVideoUrl(),
                                    darshan.getThumbnailUrl(),
                                    darshan.getGodId(),
                                    darshan.getMandirId());
                    return svmm;
                }).collect(Collectors.toList());
    }

    public static <T extends Enum<T> & ModelClassRetriever> List<GenericPageCardItemModel<T>>
    convert(FeedPageResponse feedPageResponse, T enumInstance) {
        if (feedPageResponse == null || feedPageResponse.getFeedItems() == null) {
            return new ArrayList<>();
        }
        return feedPageResponse.getFeedItems()
                .stream()
                .map(feedItemRes -> {
                    String fromImageUrl = null;
                    if (!CollectionUtils.isEmpty(feedItemRes.getFromImgUrl())) {
                        fromImageUrl = feedItemRes.getFromImgUrl().get(0);
                    }
                    GenericPageCardItemModel<T> feedItem =
                            new GenericPageCardItemModel<>(new FeedItemModel(feedItemRes.getFrom(),
                                    fromImageUrl,
                                    feedItemRes.getPost().getTitle(),
                                    feedItemRes.getPost().getDescription(),
                                    feedItemRes.getPost().getImagesUrl(),
                                    feedItemRes.getPost().getAudioUrl(),
                                    feedItemRes.getPost().getVideoUrl(),
                                    feedItemRes.getPost().getThumbnailUrl()
                            ), enumInstance);
                    return feedItem;
                }).collect(Collectors.toList());
    }
}
