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

package com.blck.MusicReleaseTracker.Scraping.Scrapers;

import com.blck.MusicReleaseTracker.DataObjects.Song;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;

public class ScraperTest {

    @Test
    void emptySongListFromScraper() {
        ScraperBeatport scraperBP = new ScraperBeatport(null, null, null, null, null);

        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> scraperBP.processInfo(new ArrayList<>()));
    }

    @Test
    void unifyApostrophesBackticksAndAccents() {
        ScraperBeatport scraperBP = new ScraperBeatport(null, null, null, null, null);

        assertEquals("S'o'n'g", scraperBP.unifyAphostrophes("S’o´n'g"));
    }

    @Test
    void checkDateValidity() {
        ScraperBeatport scraperBP = new ScraperBeatport(null, null, null, null, null);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        assertFalse(scraperBP.isValidDate("2023", formatter));
        assertFalse(scraperBP.isValidDate("-", formatter));
        assertFalse(scraperBP.isValidDate("08-05-2023", formatter));
        assertTrue(scraperBP.isValidDate("2023-10-05", formatter));
    }

    @Test
    void leaveOldestDuplicatesOnlyAndSortByNewest() {
        ScraperBeatport scraperBP = new ScraperBeatport(null, null, null, null, null);
        List<Song> inputList = List.of(
                new Song("Song1", "", "2023-01-01", null, null),
                new Song("SONG1", "", "2005-05-05", null, null),
                new Song("song1", "", "2005-05-06", null, null),
                new Song("Song3", "", "2021-01-01", null, null),
                new Song("Song3", "", "2019-01-01", null, null),
                new Song("song2", "", "2017-01-01", null, null));

        List<Song> expectedList = List.of(
                new Song("Song3", "", "2019-01-01", null, null),
                new Song("song2", "", "2017-01-01", null, null),
                new Song("SONG1", "", "2005-05-05", null, null));

        List<Song> resultList = scraperBP.processInfo(inputList);

        assertArrayEquals(expectedList.toArray(), resultList.toArray());
    }

    @Test
    void reduceToIDBeatport() {
        ScraperBeatport scraperBP = new ScraperBeatport(null, null, null, null, "https://www.beatport.com/artist/artistname/1234/tracks");
        assertEquals("artistname/1234", scraperBP.getID());

        scraperBP = new ScraperBeatport(null, null, null, null, "https://www.beatport.com/artist/artistname/1234");
        assertEquals("artistname/1234", scraperBP.getID());

        scraperBP = new ScraperBeatport(null, null, null, null, "artistname/1234");
        assertEquals("artistname/1234", scraperBP.getID());
    }

    @Test
    void reduceToIDMusicbrainz() {
        ScraperMusicbrainz scraperMB = new ScraperMusicbrainz(null, null, null, null, "https://musicbrainz.org/artist/123-id-123/releases");
        assertEquals("123-id-123", scraperMB.getID());

        scraperMB = new ScraperMusicbrainz(null, null, null, null, "https://musicbrainz.org/artist/123-id-123");
        assertEquals("123-id-123", scraperMB.getID());

        scraperMB = new ScraperMusicbrainz(null, null, null, null, "123-id-123");
        assertEquals("123-id-123", scraperMB.getID());
    }

    @Test
    void reduceToIDYoutube() {
        ScraperYoutube scraperYT = new ScraperYoutube(null, null, null, null, "https://www.youtube.com/channel/123-id-123");
        assertEquals("123-id-123", scraperYT.getID());

        scraperYT = new ScraperYoutube(null, null, null, null, "123-id-123");
        assertEquals("123-id-123", scraperYT.getID());
    }

}
