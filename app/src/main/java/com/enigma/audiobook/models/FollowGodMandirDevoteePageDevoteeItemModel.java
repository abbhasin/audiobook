package com.enigma.audiobook.models;

public class FollowGodMandirDevoteePageDevoteeItemModel {

    private String devoteeName;
    private boolean isFollowed;
    private String imageUrl;
    private int numPosts;

    public FollowGodMandirDevoteePageDevoteeItemModel(String devoteeName, boolean isFollowed, String imageUrl, int numPosts) {
        this.devoteeName = devoteeName;
        this.isFollowed = isFollowed;
        this.imageUrl = imageUrl;
        this.numPosts = numPosts;
    }

    public String getDevoteeName() {
        return devoteeName;
    }

    public boolean isFollowed() {
        return isFollowed;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getNumPosts() {
        return numPosts;
    }
}
