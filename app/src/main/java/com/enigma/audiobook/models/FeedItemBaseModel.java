package com.enigma.audiobook.models;

public class FeedItemBaseModel {
    private String from;
    private String fromImgUrl;

    public FeedItemBaseModel(String from, String fromImgUrl) {
        this.from = from;
        this.fromImgUrl = fromImgUrl;
    }

    public String getFrom() {
        return from;
    }

    public String getFromImgUrl() {
        return fromImgUrl;
    }
}
