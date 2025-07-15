/*
 *         MusicReleaseTracker
 *         Copyright (C) 2023 - 2025 BLCK
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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SongTest {

    @Test
    void getSingleArtist() {
        Song song = new Song("", "artist","", null, null);
        assertEquals("artist", song.getArtists());
    }

    @Test
    void getOrderedArtistsByAlphabet() {
        Song song = new Song("", "zilch","", null, null);
        song.appendArtist("bob");
        song.appendArtist("joe");
        song.appendArtist("joe");
        assertEquals("bob, joe, zilch", song.getArtists());
    }

    @Test
    void toStringWithType() {
        Song song = new Song("song", "artist","date", "type", null);

        assertEquals("song artist date type", song.toString());
    }

    @Test
    void toStringWithNoType() {
        Song song = new Song("song", "artist","date", null, null);

        assertEquals("song artist date", song.toString());
    }

    @Test
    void argumentNullTypeIsEmpty() {
        Song song = new Song("song", "artist","date", null, null);

        assertThat(song.getType()).isEmpty();
    }

    @Test
    void argumentEmptyStringTypeIsEmpty() {
        Song song = new Song("song", "artist", "date", "", null);

        assertThat(song.getType()).isEmpty();
    }

}
