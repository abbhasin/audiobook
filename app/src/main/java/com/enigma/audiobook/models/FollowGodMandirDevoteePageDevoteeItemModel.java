package com.enigma.audiobook.models;

public class FollowGodMandirDevoteePageDevoteeItemModel {

    private String influencerId;
    private String devoteeName;
    private boolean isFollowed;
    private String imageUrl;
    private int numPosts;

    public FollowGodMandirDevoteePageDevoteeItemModel(
            String influencerId, String devoteeName, boolean isFollowed, String imageUrl, int numPosts) {
        this.influencerId = influencerId;
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

    public String getInfluencerId() {
        return influencerId;
    }
}
