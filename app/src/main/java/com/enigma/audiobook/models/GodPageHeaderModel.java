package com.enigma.audiobook.models;

public class GodPageHeaderModel {

    private String title;
    private String imageUrl;
    private boolean isFollowed;
    private String followerCountTxt;
    private boolean isMyProfilePage;

    public GodPageHeaderModel(String title, String imageUrl, boolean isFollowed, String followerCountTxt, boolean isMyProfilePage) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.isFollowed = isFollowed;
        this.followerCountTxt = followerCountTxt;
        this.isMyProfilePage = isMyProfilePage;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isFollowed() {
        return isFollowed;
    }

    public String getFollowerCountTxt() {
        return followerCountTxt;
    }

    public boolean isMyProfilePage() {
        return isMyProfilePage;
    }
}
