package com.enigma.audiobook.proxies.adapters;

import com.enigma.audiobook.backend.models.Address;
import com.enigma.audiobook.backend.models.Darshan;
import com.enigma.audiobook.backend.models.PostAssociationType;
import com.enigma.audiobook.backend.models.responses.FeedPageResponse;
import com.enigma.audiobook.backend.models.responses.GodForUser;
import com.enigma.audiobook.backend.models.responses.InfluencerForUser;
import com.enigma.audiobook.backend.models.responses.MandirForUser;
import com.enigma.audiobook.backend.models.responses.MorePages;
import com.enigma.audiobook.models.FeedItemModel;
import com.enigma.audiobook.models.FollowGodMandirDevoteePageDevoteeItemModel;
import com.enigma.audiobook.models.FollowGodMandirDevoteePageGodItemModel;
import com.enigma.audiobook.models.FollowGodMandirDevoteePageMandirItemModel;
import com.enigma.audiobook.models.GenericPageCardItemModel;
import com.enigma.audiobook.models.ModelClassRetriever;
import com.enigma.audiobook.models.MorePageModel;
import com.enigma.audiobook.models.SwipeVideoMediaModel;
import com.enigma.audiobook.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
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
                                    darshan.getMandirId(),
                                    darshan.getDarshanId());
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
                    if (!Utils.isEmpty(feedItemRes.getFromImgUrl())) {
                        fromImageUrl = feedItemRes.getFromImgUrl().get(0);
                    }
                    GenericPageCardItemModel<T> feedItem =
                            new GenericPageCardItemModel<>(
                                    new FeedItemModel(
                                            feedItemRes.getPost().getPostId(),
                                            feedItemRes.getPost().getContentUploadStatus(),
                                            feedItemRes.getFrom(),
                                            fromImageUrl,
                                            feedItemRes.getPost().getTitle(),
                                            feedItemRes.getPost().getDescription(),
                                            feedItemRes.getPost().getImagesUrl(),
                                            feedItemRes.getPost().getAudioUrl(),
                                            feedItemRes.getPost().getVideoUrl(),
                                            feedItemRes.getPost().getThumbnailUrl(),
                                            feedItemRes.getPost().getAssociationType(),
                                            feedItemRes.getPost().getAssociatedGodId(),
                                            feedItemRes.getPost().getAssociatedMandirId(),
                                            feedItemRes.getPost().getAssociatedInfluencerId()),
                                    enumInstance);
                    return feedItem;
                }).collect(Collectors.toList());
    }

    public static List<FollowGodMandirDevoteePageGodItemModel> convertGodsForUser(List<GodForUser> godForUsers) {
        return godForUsers.stream()
                .map(godForUser ->
                        new FollowGodMandirDevoteePageGodItemModel(
                                godForUser.getGod().getGodId(),
                                godForUser.getGod().getGodName(),
                                godForUser.isFollowed(),
                                godForUser.getGod().getImageUrl().get(0)
                        ))
                .collect(Collectors.toList());
    }

    public static List<FollowGodMandirDevoteePageMandirItemModel> convertMandirsForUser(List<MandirForUser> mandirForUsers) {
        return mandirForUsers.stream()
                .map(mandirForUser ->
                        new FollowGodMandirDevoteePageMandirItemModel(
                                mandirForUser.getMandir().getMandirId(),
                                mandirForUser.getMandir().getName(),
                                mandirForUser.isFollowed(),
                                mandirForUser.getMandir().getImageUrl().get(0),
                                getLocation(mandirForUser.getMandir().getAddress())
                        ))
                .collect(Collectors.toList());
    }

    public static String getLocation(Address address) {
        return getLocation(address, false);
    }

    public static String getLocation(Address address, boolean fullAddress) {
        if (address == null) {
            return "";
        }
        StringJoiner joiner = new StringJoiner(", ");
        if (!Utils.isEmpty(address.getStreet())) {
            joiner.add(address.getStreet());
        }

        if (!Utils.isEmpty(address.getLocality())) {
            joiner.add(address.getLocality());
        }

        if (fullAddress && !Utils.isEmpty(address.getPincode())) {
            joiner.add(address.getPincode());
        }

        if (!Utils.isEmpty(address.getCity())) {
            joiner.add(address.getCity());
        }

        if (!Utils.isEmpty(address.getState())) {
            joiner.add(address.getState());
        }

        return joiner.toString();
    }

    public static List<FollowGodMandirDevoteePageDevoteeItemModel>
    convertInfluencersForUser(List<InfluencerForUser> influencersForUser) {
        return influencersForUser.stream()
                .map(influencerForUser ->
                        new FollowGodMandirDevoteePageDevoteeItemModel(
                                influencerForUser.getInfluencer().getUserId(),
                                influencerForUser.getInfluencer().getName(),
                                influencerForUser.isFollowed(),
                                influencerForUser.getInfluencer().getImageUrl().get(0),
                                influencerForUser.getNumOfPosts()
                        ))
                .collect(Collectors.toList());
    }

    public static List<MorePageModel> convertMorePages(MorePages morePages) {
        if (morePages == null || Utils.isEmpty(morePages.getPage())) {
            return Collections.emptyList();
        }

        return morePages.getPage()
                .stream()
                .map(MorePageModel::new)
                .collect(Collectors.toList());
    }
}
