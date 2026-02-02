
package com.blck.MusicReleaseTracker.Scraping;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.DataObjects.Song;
import com.blck.MusicReleaseTracker.Controllers.SseController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;

import static com.blck.MusicReleaseTracker.Scraping.Helpers.SongAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScrapeProcessTest {

    int scrapers;

    @Mock
    DBqueries DB;
    @Mock
    ScraperManager scraperManager;
    @Mock
    SseController sseController;
    @Mock
    ErrorLogging log;
    @InjectMocks
    ScrapeProcess scrapeProcess;
    @Captor
    private ArgumentCaptor<Double> progressCaptor;

    @BeforeEach
    void setUp() {
        scrapers = 4;
        lenient().when(scraperManager.loadWithScrapers()).thenReturn(4);
        lenient().when(scraperManager.scrapeNext()).thenAnswer(invocation -> {
            scrapers--;
            return scrapers;
        });
    }

    @Test
    void scrapeDataCallsAllScrapers() {
        scrapeProcess.scrapeData(scraperManager);

        verify(scraperManager, times(4)).scrapeNext();
    }

    @Test
    void scrapeCancelBreaksLoop() {
        when(scraperManager.scrapeNext()).thenAnswer(invocation -> {
            scrapeProcess.scrapeCancel = true;
            return scrapers;
        });

        scrapeProcess.scrapeData(scraperManager);

        verify(scraperManager, times(1)).scrapeNext();
    }

    @Test
    void calculatesCorrectProgress() {
        scrapeProcess.scrapeData(scraperManager);

        verify(sseController, atLeastOnce()).sendProgress(progressCaptor.capture());
        List<Double> values = progressCaptor.getAllValues();
        assertEquals(0.25, values.get(0));
        assertEquals(0.5, values.get(1));
        assertEquals(0.75, values.get(2));
    }

    @Test
    void takeOlderSongArtistDuplicate() {
        List<Song> songList = List.of(
                new Song("song", "artist", "2022-02-02", null, null),
                new Song("song", "artist", "2023-01-01", null, "example.com"),
                new Song("song", "artist", "2022-01-01", null, null));

        List<Song> output = scrapeProcess.mergeNameArtistDuplicates(songList);

        Song expected = new Song("song", "artist", "2022-01-01", null, "example.com");
        assertThat(expected).dataMatches(output.getFirst());
    }

    @Test
    void wrongDateFormatException() {
        List<Song> songList = List.of(
                new Song("song", "artist", "2022", null, null),
                new Song("song", "artist", "2022-01-01", null, null));

        Collection<Song> output = scrapeProcess.mergeNameArtistDuplicates(songList);

        verify(log).error(any(), eq(ErrorLogging.Severity.SEVERE), contains("incorrect date format"));
    }

    @Test
    void appendArtistsSortedAlphabeticallyWhenSameSongAndDate() {
        List<Song> songList = List.of(
                new Song("song", "zilch", "2022-01-01", null, null),
                new Song("song", "bob", "2022-01-01", null, null),
                new Song("song", "joe", "2022-01-01", null, "example.com"),
                new Song("song", "joe", "2022-01-01", null, null));

        List<Song> output = scrapeProcess.mergeNameDateDuplicates(songList);

        Song expected = new Song("song", "bob, joe, zilch", "2022-01-01", null, "example.com");
        assertThat(expected).dataMatches(output.getFirst());
    }

    @Test
    void sortByNewestDate() {
        List<Song> songList = List.of(
                new Song("song3", "artist", "2020-01-01", null, null),
                new Song("song1", "artist", "2023-01-05", null, null),
                new Song("song2", "artist", "2023-01-01", null, null));
        List<Song> expected = List.of(
                new Song("song1", "artist", "2023-01-05", null, null),
                new Song("song2", "artist", "2023-01-01", null, null),
                new Song("song3", "artist", "2020-01-01", null, null));

        List<Song> output = scrapeProcess.sortByNewestAndByName(songList);

        for (int i = 0; i < expected.size(); ++i)
            assertThat(expected.get(i)).dataMatches(output.get(i));
    }

    @Test
    void sameDateThenSortedAlphabeticallyByName() {
        List<Song> songList = List.of(
                new Song("C", "artist", "2020-01-01", null, null),
                new Song("A", "artist", "2020-01-01", null, null),
                new Song("B", "artist", "2020-01-01", null, null));
        List<Song> expected = List.of(
                new Song("A", "artist", "2020-01-01", null, null),
                new Song("B", "artist", "2020-01-01", null, null),
                new Song("C", "artist", "2020-01-01", null, null));

        List<Song> output = scrapeProcess.sortByNewestAndByName(songList);

        for (int i = 0; i < expected.size(); ++i)
            assertThat(expected.get(i)).dataMatches(output.get(i));
    }

    @Test
    void datesApartByDays() {
        Song s1 = new Song("song", "artist", "2020-01-01", null, null);
        Song s2 = new Song("song", "artist", "2020-01-05", null, null);

        int dayDiff = scrapeProcess.getDayDifference(s1, s2);

        assertEquals(4, dayDiff);
    }

    @Test
    void sameDateApart() {
        Song s1 = new Song("song", "artist", "2020-01-01", null, null);

        int dayDiff = scrapeProcess.getDayDifference(s1, s1);

        assertEquals(0, dayDiff);
    }

    @Test
    void positiveTimeApart() {
        Song s1 = new Song("song", "artist", "2020-01-01", null, null);
        Song s2 = new Song("song", "artist", "2020-01-05", null, null);

        int dayDiff = scrapeProcess.getDayDifference(s2, s1);

        assertTrue(dayDiff > 0);
    }

    @Test
    void mergeSongsWithinDaysApart() {
        List<Song> songList = List.of(
                new Song("song", "artist", "2020-01-05", null, "example.com"),
                new Song("song", "artist", "2020-01-04", null, null),
                new Song("song", "artist", "2022-10-01", null, null));
        List<Song> expected = List.of(
                new Song("song", "artist", "2020-01-04", null, "example.com"),
                new Song("song", "artist", "2022-10-01", null, null));

        List<Song> output = scrapeProcess.mergeSongsWithinDaysApart(songList, 2);

        for (int i = 0; i < expected.size(); ++i)
            assertThat(expected.get(i)).dataMatches(output.get(i));
    }

    @Test
    void mergeSongsWithinDaysApartAppendArtists() {
        List<Song> songList = List.of(
                new Song("song", "joe", "2020-01-05", null, null),
                new Song("song", "bob", "2020-01-03", null, null),
                new Song("song", "zilch", "2020-01-04", null, "example.com"));
        Song expected = new Song("song", "bob, joe, zilch", "2020-01-03", null, "example.com");

        List<Song> output = scrapeProcess.mergeSongsWithinDaysApart(songList, 2);

        assertThat(expected).dataMatches(output.getFirst());
    }

    @Test
    void noSongsToMergeByDaysApart() {
        List<Song> songList = List.of(
                new Song("song", "artist", "2020-01-05", null, null),
                new Song("song", "artist", "2021-01-05", null, null),
                new Song("song", "artist", "2022-01-05", null, null));

        List<Song> output = scrapeProcess.mergeSongsWithinDaysApart(songList, 1);

        for (int i = 0; i < songList.size(); ++i)
            assertThat(songList.get(i)).dataMatches(output.get(i));
    }

    @Test
    void identicalSongsSameDate() {
        List<Song> songList = List.of(
                new Song("song", "artist", "2020-01-05", null, null),
                new Song("song", "artist", "2020-01-05", null, "example.com"));
        List<Song> expected = List.of(
                new Song("song", "artist", "2020-01-05", null, "example.com"));

        List<Song> output = scrapeProcess.mergeSongsWithinDaysApart(songList, 1);

        assertEquals(1, output.size());
    }

    @Test
    void justOutsideMaxDaysApart() {
        List<Song> songList = List.of(
                new Song("song", "artist", "2020-01-04", null, null),
                new Song("song", "artist", "2020-01-01", null, null),
                new Song("song", "artist", "2020-01-02", null, null),
                new Song("song", "artist", "2020-01-03", null, null),
                new Song("song", "artist", "2020-01-05", null, null));
        List<Song> expected = List.of(
                new Song("song", "artist", "2020-01-01", null, null),
                new Song("song", "artist", "2020-01-04", null, null));

        List<Song> output = scrapeProcess.mergeSongsWithinDaysApart(songList, 2);

        for (int i = 0; i < expected.size(); ++i)
            assertThat(expected.get(i)).dataMatches(output.get(i));
    }

    @Test
    void assignAlbumForAtLeastXSameDayReleasesByAnArtist() {
        List<Song> songList = List.of(
                new Song("song1", "artist", "2020-01-04", null, null),
                new Song("song2", "artist", "2020-01-04", null, null),
                new Song("song3", "artist", "2020-01-04", null, null));

        List<Song> output = scrapeProcess.groupSameDateArtistSongs(songList, 2);

        for (Song song : output)
            assertNotNull(song.getAlbum());
    }

    @Test
    void lessThanAtLeastSameDayReleasesRemainSongs() {
        List<Song> songList = List.of(
                new Song("song1", "artist", "2020-01-04", null, null),
                new Song("song2", "artist", "2020-01-04", null, null));

        List<Song> output = scrapeProcess.groupSameDateArtistSongs(songList, 3);

        for (Song song : output)
            assertNull(song.getAlbum());
    }

    @Test
    void bothGroupAndNotGroupCases() {
        List<Song> songList = List.of(
                new Song("song1", "artist", "2020-01-04", null, null),
                new Song("song2", "notGroup", "2020-01-04", null, null),
                new Song("song3", "artist", "2020-01-04", null, null),
                new Song("song4", "neitherGroup", "2020-01-04", null, null),
                new Song("song4", "artist", "2020-01-04", null, null));

        List<Song> output = scrapeProcess.groupSameDateArtistSongs(songList, 2);

        assertEquals(2, output.stream()
                .filter(song -> song.getAlbum() == null)
                .count()
        );
    }
}
