package com.enigma.audiobook.models;

public class MenuItemModel {
    private String text;
    private String imageUrl;

    private ActivityType activityType;

    public MenuItemModel(String text, String imageUrl, ActivityType activityType) {
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
        MUSIC_LIST,
        GOD_PAGE,
        MANDIR_PAGE,
        PUJARI_PAGE,
        MY_FEED,
        FOLLOW_GOD_MANDIR_DEVOTEE,
        LIBRARY,
        TEST_CRASH,
        SIGN_IN
    }
}
