package com.blck.MusicReleaseTracker.DataObjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

public class SongTest {

    @Test
    void getSingleArtist() {
        Song song = new Song("", "artist","");
        assertEquals("artist", song.getArtists());
    }

    @Test
    void getOrderedArtistsByAlphabet() {
        Song song = new Song("", "zilch","");
        song.appendArtist("bob");
        song.appendArtist("joe");
        song.appendArtist("joe");
        assertEquals("bob, joe, zilch", song.getArtists());
    }

    @Test
    void correctStringWithType() {
        Song song = new Song("song", "artist","date", "type");

        assertEquals("song artist date type", song.toString());
    }

    @Test
    void correctStringWithNoType() {
        Song song = new Song("song", "artist","date");

        assertEquals("song artist date", song.toString());
    }

    @Test
    void argumentTypeNullDoesNotThrowException() {
        Song song = new Song("song", "artist","date", null);
    }

}
