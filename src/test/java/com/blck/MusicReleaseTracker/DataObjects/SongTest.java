package com.blck.MusicReleaseTracker.DataObjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SongTest {

    @Test
    void getSingleArtist() {
        Song song = new Song("", "artist","", "");
        assertEquals("artist", song.getArtists());
    }

    @Test
    void getOrderedArtists() {
        Song song = new Song("", "zilch","", "");
        song.appendArtist("bob");
        song.appendArtist("joe");
        song.appendArtist("joe");
        assertEquals("bob, joe, zilch", song.getArtists());
    }

}
