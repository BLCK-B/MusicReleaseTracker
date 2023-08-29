package com.blck.MusicReleaseTracker;

public class TableModel {
   /* private String song;
    private String date;

    public TableModel(String song, String date) {
        this.song = song;
        this.date = date;
    }

    public String getSong() {
        return song;
    }
    public String getDate() {
        return date;
    }*/

    private String song;
    private String artist;
    private String date;

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
