package com.blck.MusicReleaseTracker.ModelsEnums;

public class TableModel {

    private final String song;
    private final String artist;
    private final String date;

    public TableModel(String song, String artist, String date) {
        this.song = song;
        this.artist = artist;
        this.date = date;
    }

    public String getSong() {
        return song;
    }
    public String getArtist() {
        return artist;
    }
    public String getDate() {
        return date;
    }
}
