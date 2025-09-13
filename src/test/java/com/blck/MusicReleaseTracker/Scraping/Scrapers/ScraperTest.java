
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
