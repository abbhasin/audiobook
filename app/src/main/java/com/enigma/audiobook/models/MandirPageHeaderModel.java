package com.enigma.audiobook.models;

public class MandirPageHeaderModel {

    private String title;
    private String imageUrl;
    private boolean isFollowed;
    private String followerCountTxt;
    private boolean isMyProfilePage;
    private String address;

    public MandirPageHeaderModel(String title, String imageUrl, boolean isFollowed, String followerCountTxt, boolean isMyProfilePage, String address) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.isFollowed = isFollowed;
        this.followerCountTxt = followerCountTxt;
        this.isMyProfilePage = isMyProfilePage;
        this.address = address;
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

    public String getAddress() {
        return address;
    }
}
