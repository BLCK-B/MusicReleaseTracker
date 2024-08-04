package com.blck.MusicReleaseTracker.DataObjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SongTest {

    @Test
    void getSingleArtist() {
        Song song = new Song("", "artist","");
        assertEquals("artist", song.getArtists());
    }

    @Test
    void getOrderedArtists() {
        Song song = new Song("", "zilch","");
        song.appendArtist("bob");
        song.appendArtist("joe");
        song.appendArtist("joe");
        assertEquals("bob, joe, zilch", song.getArtists());
    }

    @Test
    void correctStringNoType() {
        Song song = new Song("song", "artist","date");

        assertEquals("song artist date", song.toString());
    }

    @Test
    void correctStringWithType() {
        Song song = new Song("song", "artist","date", "type");

        assertEquals("song artist date type", song.toString());
    }

}
