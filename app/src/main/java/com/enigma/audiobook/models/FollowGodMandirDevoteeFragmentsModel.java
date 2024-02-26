package com.enigma.audiobook.models;

public class FollowGodMandirDevoteeFragmentsModel {
    public enum FragmentType {
        GOD,
        MANDIR,
        DEVOTEE
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
