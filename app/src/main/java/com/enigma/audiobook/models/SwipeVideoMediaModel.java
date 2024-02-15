package com.enigma.audiobook.models;

import java.util.Objects;

public class SwipeVideoMediaModel {
    private String title;
    private String description;
    private String thumbnail;
    private String videoUrl;

    private String godId;
    private String mandirId;

    public SwipeVideoMediaModel(String title, String description, String video_url, String thumbnail, String godId, String mandirId) {
        this.title = title;
        this.description = description;
        this.videoUrl = video_url;
        this.thumbnail = thumbnail;
        this.godId = godId;
        this.mandirId = mandirId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SwipeVideoMediaModel that = (SwipeVideoMediaModel) o;
        return title.equals(that.title) && description.equals(that.description) && thumbnail.equals(that.thumbnail) && videoUrl.equals(that.videoUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, thumbnail, videoUrl);
    }
}
