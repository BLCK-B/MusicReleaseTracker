package com.blck.MusicReleaseTracker.DataObjects;

import java.util.List;
import java.util.Optional;

/**
 * MediaItem may represent a {@code Song} or an {@code Album}. </br>
 * The shared parameters of a song and album are are date and name.
 */
public interface MediaItem {

    List<Song> getSongs();

    String getAlbum();

    String getName();

    String getDate();

    Optional<String> getThumbnailUrl();
}