
package com.blck.MusicReleaseTracker.DataObjects;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SongTest {

    @Test
    void getSingleArtist() {
        Song song = new Song("", "artist", "", null, null);
        assertEquals("artist", song.getArtists());
    }

    @Test
    void getOrderedArtistsByAlphabet() {
        Song song = new Song("", "zilch", "", null, null);
        song.appendArtist("bob");
        song.appendArtist("joe");
        song.appendArtist("joe");
        assertEquals("bob, joe, zilch", song.getArtists());
    }

    @Test
    void toStringWithType() {
        Song song = new Song("song", "artist", "date", "type", null);

        assertEquals("song artist date type", song.toString());
    }

    @Test
    void toStringWithNoType() {
        Song song = new Song("song", "artist", "date", null, null);

        assertEquals("song artist date", song.toString());
    }

    @Test
    void argumentNullTypeIsEmpty() {
        Song song = new Song("song", "artist", "date", null, null);

        assertThat(song.getType()).isEmpty();
    }

    @Test
    void argumentEmptyStringTypeIsEmpty() {
        Song song = new Song("song", "artist", "date", "", null);

        assertThat(song.getType()).isEmpty();
    }

}
