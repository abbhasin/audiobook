package com.enigma.audiobook.models;

import static com.enigma.audiobook.models.FeedItemModel.FeedItemType.IMAGES;
import static com.enigma.audiobook.models.FeedItemModel.FeedItemType.MUSIC;
import static com.enigma.audiobook.models.FeedItemModel.FeedItemType.TEXT_ONLY;
import static com.enigma.audiobook.models.FeedItemModel.FeedItemType.VIDEO;

import com.enigma.audiobook.backend.models.ContentUploadStatus;
import com.enigma.audiobook.backend.models.PostAssociationType;

import java.util.List;

public class FeedItemModel extends FeedItemBaseModel {
    private String id;
    private ContentUploadStatus contentUploadStatus;
    private String title;
    private String description;
    private List<String> imagesUrls;
    private String musicUrl;
    private String videoUrl;
    private String videoThumbnailUrl;

    private PostAssociationType postAssociationType;
    private String godId;
    private String mandirId;
    private String influencerID;

    public FeedItemModel(String id, ContentUploadStatus contentUploadStatus,
                         String from, String fromImgUrl,
                         String title, String description,
                         List<String> imagesUrls, String musicUrl,
                         String videoUrl, String videoThumbnailUrl,
                         PostAssociationType postAssociationType, String godId,
                         String mandirId, String influencerID) {
        super(from, fromImgUrl);
        this.id = id;
        this.contentUploadStatus = contentUploadStatus;
        this.title = title;
        this.description = description;
        this.imagesUrls = imagesUrls;
        this.musicUrl = musicUrl;
        this.videoUrl = videoUrl;
        this.videoThumbnailUrl = videoThumbnailUrl;
        this.postAssociationType = postAssociationType;
        this.godId = godId;
        this.mandirId = mandirId;
        this.influencerID = influencerID;
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

    public ContentUploadStatus getContentUploadStatus() {
        return contentUploadStatus;
    }

    public String getId() {
        return id;
    }

    public PostAssociationType getPostAssociationType() {
        return postAssociationType;
    }

    public String getGodId() {
        return godId;
    }

    public String getMandirId() {
        return mandirId;
    }

    public String getInfluencerID() {
        return influencerID;
    }

    public enum FeedItemType {
        TEXT_ONLY,
        MUSIC,
        VIDEO,
        IMAGES

    }
}
