/*
 *         MusicReleaseTracker
 *         Copyright (C) 2023 - 2024 BLCK
 *         This program is free software: you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation, either version 3 of the License, or
 *         (at your option) any later version.
 *         This program is distributed in the hope that it will be useful,
 *         but WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *         GNU General Public License for more details.
 *         You should have received a copy of the GNU General Public License
 *         along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.blck.MusicReleaseTracker.DataObjects;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

public class Song implements Comparable<Song>, MediaItem {
    private final String songName;
    private final SortedSet<String> songArtists;
    private final String songDate;
    private final Optional<String> songType;
    private String album;

    public Song(String songName, String songArtists, String songDate, String songType) {
        this.songName = songName;
        this.songArtists = new TreeSet<>();
        this.songArtists.add(songArtists);
        this.songDate = songDate;
        if (songType == null)
            this.songType = Optional.empty();
        else if (songType.isBlank())
            this.songType = Optional.empty();
        else
            this.songType = Optional.of(songType);
    }

    public Song(String songName, String songArtists, String songDate) {
        this.songName = songName;
        this.songArtists = new TreeSet<>();
        this.songArtists.add(songArtists);
        this.songDate = songDate;
        this.songType = Optional.empty();
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbumID(String album) {
        this.album = album;
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

    public Optional<String> getType() {
        return songType;
    }

    public void appendArtist(String artist) {
        this.songArtists.add(artist);
    }

    @Override
    public String toString() {
        return songName + " " + getArtists() + " " + songDate +
                (songType.map(type -> " " + type).orElse(""));
    }

    public int compareDates(Song s, DateTimeFormatter formatter) {
        return LocalDate.parse(getDate(), formatter)
                .compareTo(LocalDate.parse(s.getDate(), formatter));
    }

    @Override
    public int compareTo(Song s) {
        return getName().toLowerCase().compareTo(s.getName().toLowerCase());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (this == o)
            return true;
        if (!(o instanceof Song s))
            return false;
        return getName().equalsIgnoreCase(s.getName());
    }

    @Override
    public int hashCode() {
        return getName().toLowerCase().hashCode();
    }
}
