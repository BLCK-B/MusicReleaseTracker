package com.blck.MusicReleaseTracker.DTO;

import com.blck.MusicReleaseTracker.DataObjects.Song;

import java.util.List;

/**
 *
 * @param songs either songs in album or a single song as single
 * @param album album ID
 */
public record MediaItemDTO(
        List<Song> songs,
        String album
) {
}
