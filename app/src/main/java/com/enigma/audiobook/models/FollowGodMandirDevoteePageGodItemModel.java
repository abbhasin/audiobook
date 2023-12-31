package com.enigma.audiobook.models;

public class FollowGodMandirDevoteePageGodItemModel {
        private String godName;
        private boolean isFollowed;
        private String imageUrl;

        public FollowGodMandirDevoteePageGodItemModel(String godName, boolean isFollowed, String imageUrl) {
            this.godName = godName;
            this.isFollowed = isFollowed;
            this.imageUrl = imageUrl;
        }

        public String getGodName() {
            return godName;
        }

        public boolean isFollowed() {
            return isFollowed;
        }

        public String getImageUrl() {
            return imageUrl;
        }
}
