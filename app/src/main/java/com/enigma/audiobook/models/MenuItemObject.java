package com.enigma.audiobook.models;

public class MenuItemObject {
    private String text;
    private String imageUrl;

    private ActivityType activityType;

    public MenuItemObject(String text, String imageUrl, ActivityType activityType) {
        this.text = text;
        this.imageUrl = imageUrl;
        this.activityType = activityType;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public String getText() {
        return text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public enum ActivityType {
        DARSHAN,
        VIDEO_LIST,
        MUSIC_LIST
    }
}
