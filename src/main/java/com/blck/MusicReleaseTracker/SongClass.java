package com.blck.MusicReleaseTracker;

import java.util.List;

public class SongClass {
    private String songName;
    private String songArtist;
    private String songDate;
    private String songType;


    public SongClass(String songName, String songArtist, String songDate, String songType) {
        this.songName = songName;
        this.songArtist = songArtist;
        this.songDate = songDate;
        this.songType = songType;
    }
    public SongClass(String songName, String songArtist, String songDate) {
        this.songName = songName;
        this.songArtist = songArtist;
        this.songDate = songDate;
    }

    public String getName() {
        return songName;
    }
    public String getArtist() {
        return songArtist;
    }
    public String getDate() {
        return songDate;
    }
    public String getType() {
        return songType;
    }

    @Override
    public String toString() {
        return songName +" "+ songArtist +" "+ songDate +" "+ songType;
    }

}
