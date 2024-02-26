package com.enigma.audiobook.models;

public class FollowGodMandirDevoteePageGodItemModel {
        private String godId;
        private String godName;
        private boolean isFollowed;
        private String imageUrl;

        public FollowGodMandirDevoteePageGodItemModel(String godId, String godName, boolean isFollowed, String imageUrl) {
            this.godId = godId;
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

    public String getGodId() {
        return godId;
    }
}
