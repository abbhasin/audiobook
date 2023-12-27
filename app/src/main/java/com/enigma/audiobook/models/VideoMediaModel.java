package com.enigma.audiobook.models;

public class VideoMediaModel {
    private String title;
    private String description;
    private String thumbnail;
    private String videoUrl;

    public VideoMediaModel(String title, String description, String video_url, String thumbnail) {
        this.title = title;
        this.description = description;
        this.videoUrl = video_url;
        this.thumbnail = thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getVideoUrl() {
        return videoUrl;
    }
}
