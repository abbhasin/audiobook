package com.enigma.audiobook.models;

public class FollowGodMandirDevoteeFragmentsModel {
    public enum FragmentType {
        GOD,
        MANDIR,
        DEVOTEE
    }

    private FragmentType type;

    public FollowGodMandirDevoteeFragmentsModel(FragmentType type) {
        this.type = type;
    }

    public FragmentType getType() {
        return type;
    }
}
