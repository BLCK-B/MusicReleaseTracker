package com.blck.MusicReleaseTracker.Scraping;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.DataObjects.Song;
import com.blck.MusicReleaseTracker.FrontendAPI.SSEController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.blck.MusicReleaseTracker.Scraping.SongAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
@ExtendWith(MockitoExtension.class)
public class ScrapeProcessTest {

    int scrapers;

    @Mock
    DBqueries DB;
    @Mock
    ScraperManager scraperManager;
    @Mock
    SSEController sseController;
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
        assertEquals(1, values.get(3));
    }

    @Test
    void takeOlderSongArtistDuplicate() {
        ArrayList<Song> songList = new ArrayList<>();
        songList.add(new Song("song", "artist", "2022-02-02"));
        songList.add(new Song("song", "artist", "2023-01-01"));
        songList.add(new Song("song", "artist", "2022-01-01"));

        List<Song> output = scrapeProcess.mergeNameArtistDuplicates(songList);

        Song expected = new Song("song", "artist", "2022-01-01");
        assertThat(expected).dataMatches(output.getFirst());
    }

    @Test
    void wrongDateFormatException() {
        ArrayList<Song> songList = new ArrayList<>();
        songList.add(new Song("song", "artist", "2022"));
        songList.add(new Song("song", "artist", "2022-01-01"));

        Collection<Song> output = scrapeProcess.mergeNameArtistDuplicates(songList);

        verify(log).error(any(), eq(ErrorLogging.Severity.SEVERE), contains("incorrect date format"));
    }

    @Test
    void appendArtistsSortedAlphabeticallyWhenSameSongAndDate() {
        ArrayList<Song> songList = new ArrayList<>();
        songList.add(new Song("song", "zilch", "2022-01-01"));
        songList.add(new Song("song", "bob", "2022-01-01"));
        songList.add(new Song("song", "joe", "2022-01-01"));
        songList.add(new Song("song", "joe", "2022-01-01"));

        List<Song> output = scrapeProcess.mergeNameDateDuplicates(songList);

        Song expected = new Song("song", "bob, joe, zilch", "2022-01-01");
        assertThat(expected).dataMatches(output.getFirst());
    }

    @Test
    void sortByNewest() {
        ArrayList<Song> songList = new ArrayList<>();
        songList.add(new Song("song3", "artist", "2020-01-01"));
        songList.add(new Song("song1", "artist", "2023-01-05"));
        songList.add(new Song("song2", "artist", "2023-01-01"));
        ArrayList<Song> expected = new ArrayList<>();
        expected.add(new Song("song1", "artist", "2023-01-05"));
        expected.add(new Song("song2", "artist", "2023-01-01"));
        expected.add(new Song("song3", "artist", "2020-01-01"));

        List<Song> output = scrapeProcess.sortByNewestDate(songList);

        for (int i = 0; i < expected.size(); ++i)
            assertThat(expected.get(i)).dataMatches(output.get(i));
    }

    @Test
    void datesApartByDays() {
        Song s1 = new Song("song", "artist", "2020-01-01");
        Song s2 = new Song("song", "artist", "2020-01-05");

        int dayDiff = scrapeProcess.getDayDifference(s1, s2);

        assertEquals(4, dayDiff);
    }

    @Test
    void sameDateApart() {
        Song s1 = new Song("song", "artist", "2020-01-01");

        int dayDiff = scrapeProcess.getDayDifference(s1, s1);

        assertEquals(0, dayDiff);
    }

    @Test
    void positiveTimeApart() {
        Song s1 = new Song("song", "artist", "2020-01-01");
        Song s2 = new Song("song", "artist", "2020-01-05");

        int dayDiff = scrapeProcess.getDayDifference(s2, s1);

        assertTrue(dayDiff > 0);
    }

    @Test
    void mergeSongsWithinDaysApart() {
        ArrayList<Song> songList = new ArrayList<>();
        songList.add(new Song("song", "artist", "2020-01-05"));
        songList.add(new Song("song", "artist", "2020-01-04"));
        songList.add(new Song("song", "artist", "2022-10-01"));
        ArrayList<Song> expected = new ArrayList<>();
        expected.add(new Song("song", "artist", "2020-01-04"));
        expected.add(new Song("song", "artist", "2022-10-01"));

        List<Song> output = scrapeProcess.mergeSongsWithinDaysApart(songList, 2);

        for (int i = 0; i < expected.size(); ++i)
            assertThat(expected.get(i)).dataMatches(output.get(i));
    }

    @Test
    void mergeSongsWithinDaysApartAppendArtists() {
        ArrayList<Song> songList = new ArrayList<>();
        songList.add(new Song("song", "joe", "2020-01-05"));
        songList.add(new Song("song", "bob", "2020-01-03"));
        songList.add(new Song("song", "zilch", "2020-01-04"));
        Song expected = new Song("song", "bob, joe, zilch", "2020-01-03");

        List<Song> output = scrapeProcess.mergeSongsWithinDaysApart(songList, 2);

        assertThat(expected).dataMatches(output.getFirst());
    }

    @Test
    void noSongsToMergeByDaysApart() {
        ArrayList<Song> songList = new ArrayList<>();
        songList.add(new Song("song", "artist", "2020-01-05"));
        songList.add(new Song("song", "artist", "2021-01-05"));
        songList.add(new Song("song", "artist", "2022-01-05"));

        List<Song> output = scrapeProcess.mergeSongsWithinDaysApart(songList, 1);

        for (int i = 0; i < songList.size(); ++i)
            assertThat(songList.get(i)).dataMatches(output.get(i));
    }

    @Test
    void identicalSongsSameDate() {
        ArrayList<Song> songList = new ArrayList<>();
        songList.add(new Song("song", "artist", "2020-01-05"));
        songList.add(new Song("song", "artist", "2020-01-05"));
        ArrayList<Song> expected = new ArrayList<>();
        expected.add(new Song("song", "artist", "2020-01-05"));

        List<Song> output = scrapeProcess.mergeSongsWithinDaysApart(songList, 1);

        assertEquals(1, output.size());
    }

    @Test
    void justOutsideMaxDaysApart() {
        ArrayList<Song> songList = new ArrayList<>();
        songList.add(new Song("song", "artist", "2020-01-04"));
        songList.add(new Song("song", "artist", "2020-01-01"));
        songList.add(new Song("song", "artist", "2020-01-02"));
        songList.add(new Song("song", "artist", "2020-01-03"));
        songList.add(new Song("song", "artist", "2020-01-05"));
        ArrayList<Song> expected = new ArrayList<>();
        expected.add(new Song("song", "artist", "2020-01-01"));
        expected.add(new Song("song", "artist", "2020-01-04"));

        List<Song> output = scrapeProcess.mergeSongsWithinDaysApart(songList, 2);

        for (int i = 0; i < expected.size(); ++i)
            assertThat(expected.get(i)).dataMatches(output.get(i));
    }


}
