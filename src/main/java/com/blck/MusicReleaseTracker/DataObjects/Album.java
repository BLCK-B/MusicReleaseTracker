
package com.blck.MusicReleaseTracker.DataObjects;

import java.util.List;
import java.util.Optional;

/**
 *
 * @param album album identifier, ideally unique
 * @param songs list of songs the album consists of
 */
public record Album(String album, List<Song> songs) implements MediaItem {

    @Override
    public List<Song> getSongs() {
        return songs;
    }

    @Override
    public String getAlbum() {
        return album;
    }

    @Override
    public String getName() {
        return album;
    }

    @Override
    public String getDate() {
        return songs.getFirst().getDate();
    }

    @Override
    public Optional<String> getThumbnailUrl() {
        return songs.getFirst().getThumbnailUrl();
    }
}
