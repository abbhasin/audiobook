package com.enigma.audiobook.models;

public class FollowGodMandirDevoteeFragmentsModel {
    public enum FragmentType {
        GOD("Gods"),
        MANDIR("Temples"),
        DEVOTEE("Gurus");

        private final String showName;

        FragmentType(String showName) {
            this.showName = showName;
        }

        public String getShowName() {
            return showName;
        }
    }

    private FragmentType type;
    private boolean onlyFollowed;

    public FollowGodMandirDevoteeFragmentsModel(FragmentType type, boolean onlyFollowed) {
        this.type = type;
        this.onlyFollowed = onlyFollowed;
    }

    public FragmentType getType() {
        return type;
    }

    public boolean isOnlyFollowed() {
        return onlyFollowed;
    }

}
