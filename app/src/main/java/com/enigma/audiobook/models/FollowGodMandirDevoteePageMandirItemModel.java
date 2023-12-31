package com.enigma.audiobook.models;

public class FollowGodMandirDevoteePageMandirItemModel {

    private String mandirName;
    private boolean isFollowed;
    private String imageUrl;
    private String location;

    public FollowGodMandirDevoteePageMandirItemModel(String mandirName, boolean isFollowed, String imageUrl, String location) {
        this.mandirName = mandirName;
        this.isFollowed = isFollowed;
        this.imageUrl = imageUrl;
        this.location = location;
    }

    public String getMandirName() {
        return mandirName;
    }

    public boolean isFollowed() {
        return isFollowed;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getLocation() {
        return location;
    }
}
