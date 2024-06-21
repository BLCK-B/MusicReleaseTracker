package com.blck.MusicReleaseTracker.Scraping.Scrapers;

import com.blck.MusicReleaseTracker.DataObjects.Song;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

public class ScraperTest {

    private Scraper scraper;

    @BeforeEach
    void setUp() {
        scraper = new Scraper(null, null);
    }

    @Test
    void emptySongListFromScraper() {
        assertThrows(Exception.class, () -> scraper.processInfo(new ArrayList<>()));
    }

    @Test
    void unifyApostrophesBackticksAndAccents() {
        assertEquals("S'o'n'g", scraper.unifyAphostrophes("S’o´n'g"));
    }

    @Test
    void checkDateValidity() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        assertFalse(scraper.isValidDate("2023", formatter));
        assertFalse(scraper.isValidDate("-", formatter));
        assertFalse(scraper.isValidDate("08-05-2023", formatter));
        assertTrue(scraper.isValidDate("2023-10-05", formatter));
    }

    @Test
    void leaveOldestDuplicatesOnlyAndSortByNewest() {
        ArrayList<Song> inputList = new ArrayList<>();
        inputList.add(new Song("Song1", "", "2023-01-01", null));
        inputList.add(new Song("SONG1", "", "2005-05-05", null));
        inputList.add(new Song("song1", "", "2005-05-06", null));
        inputList.add(new Song("Song3", "", "2021-01-01", null));
        inputList.add(new Song("Song3", "", "2019-01-01", null));
        inputList.add(new Song("song2", "", "2017-01-01", null));
        ArrayList<Song> expectedList = new ArrayList<>();
        expectedList.add(new Song("Song3", "", "2019-01-01", null));
        expectedList.add(new Song("song2", "", "2017-01-01", null));
        expectedList.add(new Song("SONG1", "", "2005-05-05", null));

        List<Song> resultList = scraper.processInfo(inputList);

        assertArrayEquals(expectedList.toArray(), resultList.toArray());
    }

    @Test
    void reduceToIDBeatport() {
        scraper = new ScraperBeatport(null, null, null, "https://www.beatport.com/artist/artistname/1234/tracks");
        assertEquals("artistname/1234", scraper.getID());

        scraper = new ScraperBeatport(null, null, null, "https://www.beatport.com/artist/artistname/1234");
        assertEquals("artistname/1234", scraper.getID());

        scraper = new ScraperBeatport(null, null, null, "artistname/1234");
        assertEquals("artistname/1234", scraper.getID());
    }

    @Test
    void reduceToIDMusicbrainz() {
        scraper = new ScraperMusicbrainz(null, null, null, "https://musicbrainz.org/artist/123-id-123/releases");
        assertEquals("123-id-123", scraper.getID());

        scraper = new ScraperMusicbrainz(null, null, null, "https://musicbrainz.org/artist/123-id-123");
        assertEquals("123-id-123", scraper.getID());

        scraper = new ScraperMusicbrainz(null, null, null, "123-id-123");
        assertEquals("123-id-123", scraper.getID());
    }

    @Test
    void reduceToIDJunodownload() {
        scraper = new ScraperJunodownload(null, null, null, "https://www.junodownload.com/artists/artistname/releases/");
        assertEquals("artistname", scraper.getID());

        scraper = new ScraperJunodownload(null, null, null, "https://www.junodownload.com/artists/artistname");
        assertEquals("artistname", scraper.getID());

        scraper = new ScraperJunodownload(null, null, null, "artistname");
        assertEquals("artistname", scraper.getID());
    }

    @Test
    void reduceToIDYoutube() {
        scraper = new ScraperYoutube(null, null, null, "https://www.youtube.com/channel/123-id-123");
        assertEquals("123-id-123", scraper.getID());

        scraper = new ScraperYoutube(null, null, null, "123-id-123");
        assertEquals("123-id-123", scraper.getID());
    }

}
