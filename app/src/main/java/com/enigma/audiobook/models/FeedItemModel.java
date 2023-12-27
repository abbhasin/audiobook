package com.enigma.audiobook.models;

import static com.enigma.audiobook.models.FeedItemModel.FeedItemType.IMAGES;
import static com.enigma.audiobook.models.FeedItemModel.FeedItemType.MUSIC;
import static com.enigma.audiobook.models.FeedItemModel.FeedItemType.TEXT_ONLY;
import static com.enigma.audiobook.models.FeedItemModel.FeedItemType.VIDEO;

import java.util.List;

public class FeedItemModel extends FeedItemBaseModel {
    private String title;
    private String description;
    private List<String> imagesUrls;
    private String musicUrl;
    private String videoUrl;
    private String videoThumbnailUrl;

    public FeedItemModel(String from, String fromImgUrl, String title, String description, List<String> imagesUrls, String musicUrl, String videoUrl, String videoThumbnailUrl) {
        super(from, fromImgUrl);
        this.title = title;
        this.description = description;
        this.imagesUrls = imagesUrls;
        this.musicUrl = musicUrl;
        this.videoUrl = videoUrl;
        this.videoThumbnailUrl = videoThumbnailUrl;
    }

    public FeedItemType getType() {
        if (videoUrl != null && !videoUrl.isEmpty()) {
            return VIDEO;
        } else if (musicUrl != null && !musicUrl.isEmpty()) {
            return MUSIC;
        } else if (imagesUrls != null && !imagesUrls.isEmpty()) {
            return IMAGES;
        }
        return TEXT_ONLY;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getImagesUrls() {
        return imagesUrls;
    }

    public String getMusicUrl() {
        return musicUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getVideoThumbnailUrl() {
        return videoThumbnailUrl;
    }

    public enum FeedItemType {
        TEXT_ONLY,
        MUSIC,
        VIDEO,
        IMAGES

    }
}
