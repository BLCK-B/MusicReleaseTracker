package com.blck.MusicReleaseTracker.ModelsEnums;

public record TableModel(String song, String artist, String date) {
    //records are final (read-only) classes, they can’t be extended
}
