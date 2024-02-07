package com.blck.MusicReleaseTracker.Simple;

public record TableModel(String song, String artist, String date) {
    //records are final (read-only) classes, they can’t be extended
}
