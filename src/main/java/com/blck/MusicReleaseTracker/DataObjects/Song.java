package com.blck.MusicReleaseTracker.DataObjects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

public class Song implements Comparable<Song>, MediaItem {

    private final String name;

    private final SortedSet<String> artists;

    private final String date;

    private final Optional<String> type;

    private final Optional<String> thumbnailUrl;

    private String album;

    /**
     *
     * @param name        name of the song
     * @param songArtists best created with just one artist
     * @param date        yyyy-MM-dd
     * @param type        type of song for filtering purposes like remix, instrumental
     */
    public Song(String name, String songArtists, String date, String type, String thumbnailUrl) {
        this.name = name;
        this.artists = new TreeSet<>();
        this.artists.add(songArtists);
        this.date = date;
        if (type == null || type.isBlank())
            this.type = Optional.empty();
        else
            this.type = Optional.of(type);
        if (thumbnailUrl == null)
            this.thumbnailUrl = Optional.empty();
        else
            this.thumbnailUrl = Optional.of(thumbnailUrl);
    }

    @JsonIgnore
    @Override
    public List<Song> getSongs() {
        return List.of(this);
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbumID(String album) {
        this.album = album;
    }

    public String getName() {
        return name;
    }

    public String getArtists() {
        return String.join(", ", artists);
    }

    public String getDate() {
        return date;
    }

    public Optional<String> getType() {
        return type;
    }

    public Optional<String> getThumbnailUrl() {
        return thumbnailUrl;
    }

    /**
     * Appends new artist to the song's artist list and sorts alphabetically.
     *
     * @param artist artist to add
     */
    public void appendArtist(String artist) {
        this.artists.add(artist);
    }

    /**
     *
     * @param formatter date formatter (yyyy-MM-dd)
     * @return compare release dates
     */
    public int compareDates(Song s, DateTimeFormatter formatter) {
        return LocalDate.parse(getDate(), formatter)
                .compareTo(LocalDate.parse(s.getDate(), formatter));
    }

    /**
     *
     * @return compare song names
     */
    @Override
    public int compareTo(Song s) {
        return getName().toLowerCase().compareTo(s.getName().toLowerCase());
    }

    /**
     *
     * @return true if the song names match
     */
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

    @Override
    public String toString() {
        return name + " " + getArtists() + " " + date +
                (type.map(type -> " " + type).orElse(""));
    }
}
