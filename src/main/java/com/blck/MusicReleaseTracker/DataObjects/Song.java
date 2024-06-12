package com.blck.MusicReleaseTracker.DataObjects;

/*      MusicReleaseTracker
        Copyright (C) 2023 BLCK
        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.
        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.
        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>.*/

import java.util.SortedSet;
import java.util.TreeSet;

/** an object representing a song */
public class Song {
    private String songName;
    private final SortedSet<String> songArtists;
    private final String songDate;
    private String songType;

    public Song(String songName, String songArtists, String songDate, String songType) {
        this.songName = songName;
        this.songArtists = new TreeSet<>();
        this.songArtists.add(songArtists);
        this.songDate = songDate;
        this.songType = songType;
    }

    public Song(String songName, String songArtists, String songDate) {
        this.songName = songName;
        this.songArtists = new TreeSet<>();
        this.songArtists.add(songArtists);
        this.songDate = songDate;
    }

    public String getName() {
        return songName;
    }
    public String getArtists() {
        return String.join(", ", songArtists);
    }
    public String getDate() {
        return songDate;
    }
    public String getType() {
        return songType;
    }

    public void setName(String songName) {
        this.songName = songName;
    }

    public void appendArtist(String artist) {
        this.songArtists.add(artist);
    }

    @Override
    public String toString() {
        if (this.songType != null)
            return songName + " " + getArtists() + " " + songDate + " " + songType;
        if (this.songType == null)
            return songName + " " + getArtists() + " " + songDate;

        return null;
    }

}
