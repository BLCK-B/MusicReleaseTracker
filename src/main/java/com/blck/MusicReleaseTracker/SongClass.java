package com.blck.MusicReleaseTracker;

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

import java.util.Objects;

public class SongClass {
    //an object representing a song with following parameters:
    private final String songName;
    private String songArtist;
    private final String songDate;
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

    public void appendArtist(String artist) {
        if (!this.songArtist.contains(artist))
            this.songArtist = this.songArtist + ", " + artist;
    }

    @Override
    public String toString() {
        if (this.songType != null)
            return songName +" "+ songArtist +" "+ songDate +" "+ songType;
        if (this.songType == null)
            return songName +" "+ songArtist +" "+ songDate;

        return null;
    }

}
