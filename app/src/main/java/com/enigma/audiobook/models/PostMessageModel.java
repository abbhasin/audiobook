package com.enigma.audiobook.models;

import com.enigma.audiobook.utils.Utils;

import java.util.List;

public class PostMessageModel {
    private List<SpinnerTag> spinnerList;
    private List<String> imagesUrl;
    private String videoUrl;
    private String musicUrl;

    public PostMessageModel(List<SpinnerTag> spinnerList) {
        this.spinnerList = spinnerList;
    }

    public PostMessageModel(List<SpinnerTag> spinnerList, List<String> imagesUrl, String videoUrl, String musicUrl) {
        this.spinnerList = spinnerList;
        this.imagesUrl = imagesUrl;
        this.videoUrl = videoUrl;
        this.musicUrl = musicUrl;
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

    public enum PostMessageType {
        VIDEO,
        IMAGES,
        AUDIO,
        TEXT
    }

    public void clearVideoAudioContent() {
        imagesUrl.clear();
        videoUrl = null;
        musicUrl = null;
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

    public static class SpinnerTag {
        String id;
        String text;

        public SpinnerTag(String id, String text) {
            this.id = id;
            this.text = text;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setText(String text) {
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
