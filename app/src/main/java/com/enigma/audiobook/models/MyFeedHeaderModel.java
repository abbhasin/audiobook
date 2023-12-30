package com.enigma.audiobook.models;

public class MyFeedHeaderModel {

    private int followingCount;

    public MyFeedHeaderModel(int followingCount) {
        this.followingCount = followingCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }
}
