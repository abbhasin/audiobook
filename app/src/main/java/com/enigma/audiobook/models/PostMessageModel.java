package com.enigma.audiobook.models;

import com.enigma.audiobook.backend.models.PostAssociationType;
import com.enigma.audiobook.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class PostMessageModel {
    // static items of a post message based on Activity type ex. God, Mandir etc
    private volatile List<SpinnerTag> spinnerList;
    private volatile String associatedMandirId;
    private volatile String associatedGodId;
    private volatile String associatedInfluencerId;
    private volatile String fromUserId;
    private volatile PostAssociationType associationType;

    // dynamic items of a post message
    private volatile int selectedItemPosition;
    private volatile List<String> imagesUrl;
    private volatile String videoUrl;
    private volatile String musicUrl;
    private volatile String title;
    private volatile String description;

    public PostMessageModel(List<SpinnerTag> spinnerList) {
        List<SpinnerTag> tags = new ArrayList<>();
        tags.add(new SpinnerTag("_001", "Select a tag"));
        tags.addAll(spinnerList);
        this.spinnerList = tags;
        this.imagesUrl = new ArrayList<>();
    }

    public PostMessageModel(PostMessageModel other) {
        // dynamic values
        this.musicUrl = other.musicUrl;
        this.videoUrl = other.videoUrl;
        this.imagesUrl = new ArrayList<>(other.imagesUrl);
        this.title = other.title;
        this.description = other.description;
        this.selectedItemPosition = other.selectedItemPosition;

        // static values
        this.spinnerList = new ArrayList<>(other.spinnerList);
        this.associatedGodId = other.associatedGodId;
        this.associatedInfluencerId = other.associatedInfluencerId;
        this.associatedMandirId = other.associatedMandirId;
        this.associationType = other.associationType;
        this.fromUserId = other.fromUserId;

    }

    public PostMessageType getType() {
        if (!Utils.isEmpty(videoUrl)) {
            return PostMessageType.VIDEO;
        } else if (!Utils.isEmpty(imagesUrl)) {
            return PostMessageType.IMAGES;
        } else if (!Utils.isEmpty(musicUrl)) {
            return PostMessageType.AUDIO;
        }
        return PostMessageType.TEXT;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public enum PostMessageType {
        VIDEO,
        IMAGES,
        AUDIO,
        TEXT
    }

    public boolean isSameSelectedItem(String itemTxt) {
        return spinnerList.get(selectedItemPosition).getText().equals(itemTxt);
    }

    public int getSelectedItemPosition() {
        return selectedItemPosition;
    }

    public void setSelectedItemPosition(int selectedItemPosition) {
        this.selectedItemPosition = selectedItemPosition;
    }

    public SpinnerTag getSelectedItem() {
        return spinnerList.get(selectedItemPosition);
    }

    public void clearVideoAudioContent() {
        imagesUrl.clear();
        videoUrl = null;
        musicUrl = null;
    }

    public void clearTextContent() {
        selectedItemPosition = 0;
        title = null;
        description = null;
    }

    public void setSpinnerList(List<SpinnerTag> spinnerList) {
        this.spinnerList = spinnerList;
    }

    public void setImagesUrl(List<String> imagesUrl) {
        this.imagesUrl = imagesUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setMusicUrl(String musicUrl) {
        this.musicUrl = musicUrl;
    }

    public List<String> getImagesUrl() {
        return imagesUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getMusicUrl() {
        return musicUrl;
    }

    public List<SpinnerTag> getSpinnerList() {
        return spinnerList;
    }

    public String getAssociatedMandirId() {
        return associatedMandirId;
    }

    public void setAssociatedMandirId(String associatedMandirId) {
        this.associatedMandirId = associatedMandirId;
    }

    public String getAssociatedGodId() {
        return associatedGodId;
    }

    public void setAssociatedGodId(String associatedGodId) {
        this.associatedGodId = associatedGodId;
    }

    public String getAssociatedInfluencerId() {
        return associatedInfluencerId;
    }

    public void setAssociatedInfluencerId(String associatedInfluencerId) {
        this.associatedInfluencerId = associatedInfluencerId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public PostAssociationType getAssociationType() {
        return associationType;
    }

    public void setAssociationType(PostAssociationType associationType) {
        this.associationType = associationType;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "PostMessageModel{" +
                "spinnerList=" + spinnerList +
                ", selectedItemPosition=" + selectedItemPosition +
                ", imagesUrl=" + imagesUrl +
                ", videoUrl='" + videoUrl + '\'' +
                ", musicUrl='" + musicUrl + '\'' +
                '}';
    }

    public static class SpinnerTag {
        final String id;
        final String text;

        public SpinnerTag(String id, String text) {
            this.id = id;
            this.text = text;
        }
        public String getId() {
            return id;
        }

        public String getText() {
            return text;
        }
    }
}
