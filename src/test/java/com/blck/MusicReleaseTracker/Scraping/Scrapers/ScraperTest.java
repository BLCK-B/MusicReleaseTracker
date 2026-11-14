
package com.blck.MusicReleaseTracker.Scraping.Scrapers;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.DataObjects.Song;
import com.blck.MusicReleaseTracker.Scraping.ScraperGenericException;
import com.blck.MusicReleaseTracker.Scraping.ScraperTimeoutException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
    void unicodeNormalisation() {
        ScraperBeatport scraperBP = new ScraperBeatport(null, null, null, null, null);

        assertEquals("<3", scraperBP.decodeUnicode("\\u003c3"));
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

    @Test
    void reduceToIDBandcamp() {
        ScraperBandcamp scraperBC = new ScraperBandcamp(null, null, null, null, "https://bandcamp.k47.cz/?art=123-id-123");
        assertEquals("123-id-123", scraperBC.getID());

        scraperBC = new ScraperBandcamp(null, null, null, null, "123-id-123");
        assertEquals("123-id-123", scraperBC.getID());
    }

    @Test
    void youtubeExtractDateFromDescription() {
        ScraperYoutube scraperYT = new ScraperYoutube(null, null, null, null, "https://www.youtube.com/channel/123-id-123");
        String description1 = """
                Provided to YouTube by Sound Recordings · Dimension · Karen ℗ 2025
                Under exclusive licence Released on: 2025-12-05 Sound Recordings
                Associated Performer: Dimension x Karen
                """;
        String description2 = """
                Live from AIR Studios by Dimension & Karen is available to download now. Follow Dimension: https://www.example.com
                Follow Karen: https://www.example.com/
                """;

        var result = scraperYT.extractDateFromDescription(new String[]{description1, description2});

        assertEquals(2, result.length);
        assertEquals("2025-12-05", result[0]);
        assertEquals("", result[1]);
    }

    @Test
    void youtubeMergeDescriptionAndPublishedDates() {
        ScraperYoutube scraperYT = new ScraperYoutube(null, null, null, null, "https://www.youtube.com/channel/123-id-123");
        var preferredDates = new String[]{"a", "", "b", "c"};
        var preferredDatesMissing = new String[]{"a", "b"};
        var defaultDates = new String[]{"x", "x", "x", "x"};

        assertArrayEquals(
                new String[]{"a", "x", "b", "c"},
                scraperYT.mergeDescriptionAndPublishedDates(preferredDates, defaultDates)
        );
        assertArrayEquals(
                defaultDates,
                scraperYT.mergeDescriptionAndPublishedDates(preferredDatesMissing, defaultDates)
        );
    }

    @Test
    void emptyFeedDetectionYoutube() {
        String emptyFeed = """
                        <feed>
                        <link rel="self" href="http://www.youtube.com/feeds/videos.xml?channel_id=UCcc1G3A0ZDQ94V1iRRjNjGw"/>
                        <id>yt:channel:cc1G3A0ZDQ94V1iRRjNjGw</id>
                        <yt:channelId>cc1G3A0ZDQ94V1iRRjNjGw</yt:channelId>
                        <title> - Topic</title>
                        <link rel="alternate" href="https://www.youtube.com/channel/UCcc1G3A0ZDQ94V1iRRjNjGw"/>
                        <author>
                        <name> - Topic</name>
                        <uri>
                        https://www.youtube.com/channel/UCcc1G3A0ZDQ94V1iRRjNjGw
                        </uri>
                        </author>
                        <published>2023-07-20T11:46:00+00:00</published>
                        </feed>
                """;
        String nullFeed = """
                        <feed>
                        </feed>
                """;
        Document doc1 = Jsoup.parse(emptyFeed, "", org.jsoup.parser.Parser.xmlParser());
        Document doc2 = Jsoup.parse(nullFeed, "", org.jsoup.parser.Parser.xmlParser());

        ScraperYoutube scraper = new ScraperYoutube(null, null, null, "", "");

        assertTrue(scraper.isFeedEmpty(doc1));
        assertFalse(scraper.isFeedEmpty(doc2));
    }

    @Test
    void youtubeScrapeFailsOnResultEmpty() {
        ScraperYoutube scraper = spy(new ScraperYoutube(null, null, null, "", ""));

        assertThatExceptionOfType(ScraperGenericException.class).isThrownBy(() ->
                scraper.scrape("src/test/java/com/blck/MusicReleaseTracker/Scraping/Scrapers/youtubeEmptyResult.xml", 10)
        );
    }

    @Test
    void beatportContentParsing() throws ScraperTimeoutException, ScraperGenericException {
        ScraperBeatport scraper = spy(new ScraperBeatport(null, null, null, "", ""));
        ArgumentCaptor<List<Song>> songListCaptor = ArgumentCaptor.forClass(List.class);
        doNothing().when(scraper).insertSet(songListCaptor.capture());

        scraper.scrape("src/test/java/com/blck/MusicReleaseTracker/Scraping/Scrapers/beatportResults.html", 10);

        List<Song> parsedSongs = songListCaptor.getValue();
        assertEquals(2, parsedSongs.size());
        assertEquals("Low Down", parsedSongs.get(0).getName());
        assertEquals("2026-04-24", parsedSongs.get(0).getDate());
        assertEquals("Original Mix", parsedSongs.get(0).getType());
        assertEquals("https://geo-media.beatport.com/image_size/300x300/c4002400-d9ec-4a04-b74e-88539f911b44.jpg", parsedSongs.get(0).getThumbnailUrl());
        assertEquals("Reload", parsedSongs.get(1).getName());
        assertEquals("2025-11-06", parsedSongs.get(1).getDate());
        assertEquals("Original Mix", parsedSongs.get(1).getType());
        assertEquals("https://geo-media.beatport.com/image_size/300x300/9b1c1bc4-4bf2-4973-8953-2fc143f3ccd1.jpg", parsedSongs.get(1).getThumbnailUrl());
    }
}
