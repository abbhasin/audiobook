package com.enigma.audiobook.models;

import java.util.List;

public class LibraryPageAlbumsModel {

    private String title;
    private List<AlbumItem> albumItems;

    public LibraryPageAlbumsModel(String title, List<AlbumItem> albumItems) {
        this.title = title;
        this.albumItems = albumItems;
    }

    public List<AlbumItem> getAlbumItems() {
        return albumItems;
    }

    public String getTitle() {
        return title;
    }

    public static class AlbumItem {
        private String imageUrl;
        private String text;

        public AlbumItem(String imageUrl, String text) {
            this.imageUrl = imageUrl;
            this.text = text;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getText() {
            return text;
        }
    }
}
