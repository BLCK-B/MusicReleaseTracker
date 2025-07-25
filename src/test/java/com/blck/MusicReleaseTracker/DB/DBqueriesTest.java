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

package com.blck.MusicReleaseTracker.DB;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.TablesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DataObjects.Album;
import com.blck.MusicReleaseTracker.DataObjects.MediaItem;
import com.blck.MusicReleaseTracker.DataObjects.Song;
import com.blck.MusicReleaseTracker.JsonSettings.SettingsIO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DBqueriesTest {

    @Mock
    ValueStore store;
    @Mock
    MigrateDB migrateDB;
    @Mock
    ErrorLogging log;
    @Mock
    SettingsIO settingsIO;
    @InjectMocks
    DBqueries dBqueriesClass;

    List<Song> songList;

    @BeforeAll
    static void setUpDB() {
        HelperDB.redoTestDB();
    }

    @AfterAll
    static void cleanUp() {
        HelperDB.redoTestData(HelperDB.testDBpath);
    }

    @BeforeEach
    void setUp() {
        HelperDB.redoTestData(HelperDB.testDBpath);
        lenient().when(store.getDBpath()).thenReturn(HelperDB.testDBpath);
        dBqueriesClass = new DBqueries(store, log, settingsIO, migrateDB);
        songList = List.of(
                new Song("song1", "artist1", "2022-01-01", "remix", null),
                new Song("song2", "artist1", "2022-01-01", "type", null),
                new Song("song3", "artist2", "2022-01-01", "Remixed", null));
    }

    @Test
    void batchInsertIntoSource() {
        dBqueriesClass.batchInsertSongs(songList, TablesEnum.beatport, 10);

        assertEquals(3, HelperDB.getNumEntries(HelperDB.testDBpath, "beatport"));
    }

    @Test
    void batchInsertIntoCombview() {
        dBqueriesClass.batchInsertCombview(songList);

        assertEquals(3, HelperDB.getNumEntries(HelperDB.testDBpath, "combview"));
    }

    @Test
    void batchInsertOverLimit() {
        dBqueriesClass.batchInsertSongs(songList, TablesEnum.beatport, 1);

        assertEquals(1, HelperDB.getNumEntries(HelperDB.testDBpath, "beatport"));
    }

    @Test
    void getArtist1EntriesInSourceTable() {
        dBqueriesClass.batchInsertSongs(songList, TablesEnum.beatport, 10);

        assertEquals(2, dBqueriesClass.loadTable(TablesEnum.beatport, "artist1").size());
    }

    @Test
    void getSinglesOnlyFromCombview() {
        dBqueriesClass.batchInsertCombview(songList);

        assertEquals(3, dBqueriesClass.readCombviewSingles().size());
    }

    @Test
    void getAlbumsOnlyFromCombview() {
        songList.get(0).setAlbumID("albumOne");
        songList.get(1).setAlbumID("albumTwo");
        songList.get(2).setAlbumID("albumTwo");
        dBqueriesClass.batchInsertCombview(songList);

        List<Album> albums = dBqueriesClass.readCombviewAlbums();

        assertEquals(2, albums.size());
        assertEquals(2, albums.get(1).getAlbumSongs().size());
    }

    @Test
    void albumSongsSortedByName() {
        songList = List.of(
                new Song("C", "artist", "2022-01-01", null, null),
                new Song("A", "artist", "2022-01-01", null, null),
                new Song("B", "artist", "2022-01-01", null, null));
        songList.get(0).setAlbumID("album");
        songList.get(1).setAlbumID("album");
        songList.get(2).setAlbumID("album");
        dBqueriesClass.batchInsertCombview(songList);
        List<Song> expected = List.of(
                new Song("A", "artist", "2022-01-01", null, null),
                new Song("B", "artist", "2022-01-01", null, null),
                new Song("C", "artist", "2022-01-01", null, null));

        List<Song> output = dBqueriesClass.readCombviewAlbums().getFirst().getAlbumSongs();

        for (int i = 0; i < expected.size(); ++i)
            assertEquals(expected.get(i).getName(), output.get(i).getName());
    }

    @Test
    void mixedSinglesAndAlbumsReadSingles() {
        songList.getFirst().setAlbumID("album");
        dBqueriesClass.batchInsertCombview(songList);

        assertEquals(2, dBqueriesClass.readCombviewSingles().size());
    }

    @Test
    void mixedSinglesAndAlbumsReadAlbums() {
        songList.getFirst().setAlbumID("album");
        dBqueriesClass.batchInsertCombview(songList);

        assertEquals(1, dBqueriesClass.readCombviewAlbums().size());
    }

    @Test
    void getMediaItemsFromCombview() {
        songList.getFirst().setAlbumID("album");
        dBqueriesClass.batchInsertCombview(songList);

        List<MediaItem> mediaItems = dBqueriesClass.loadCombviewTable();

        assertEquals(3, mediaItems.size());
        assertEquals(1, mediaItems.stream()
                .filter(item -> item.getAlbum() != null)
                .count()
        );
    }

    @Test
    void dataFromCombviewIsSortedByNewestDate() {
        songList = List.of(
                new Song("song", "artist", "2022-01-03", null, null),
                new Song("song", "artist", "2022-01-02", null, null),
                new Song("albumSong", "artist", "2022-01-04", null, null));
        songList.get(2).setAlbumID("album");
        dBqueriesClass.batchInsertCombview(songList);
        List<Song> expected = List.of(
                new Song("albumSong", "artist", "2022-01-04", null, null),
                new Song("song", "artist", "2022-01-03", null, null),
                new Song("song", "artist", "2022-01-02", null, null));

        List<MediaItem> mediaItems = dBqueriesClass.loadCombviewTable();

        for (int i = 0; i < expected.size(); ++i)
            assertEquals(expected.get(i).getDate(), mediaItems.get(i).getDate());
    }

    @Test
    void dataFromCombviewIsSecondarilySortedByName() {
        songList = List.of(
                new Song("B", "artist", "2022-01-01", null, null),
                new Song("A", "artist", "2022-01-01", null, null),
                new Song("C", "artist", "2022-01-01", null, null));
        songList.get(2).setAlbumID("album");
        dBqueriesClass.batchInsertCombview(songList);
        List<Song> expected = List.of(
                new Song("A", "artist", "2022-01-01", null, null),
                new Song("B", "artist", "2022-01-01", null, null),
                new Song("C", "artist", "2022-01-01", null, null));

        List<MediaItem> mediaItems = dBqueriesClass.loadCombviewTable();

        for (int i = 0; i < expected.size(); ++i)
            assertEquals(expected.get(i).getDate(), mediaItems.get(i).getDate());
    }

    @Test
    void newArtistSourceID() {
        assertEquals("IDBP", dBqueriesClass.getArtistSourceID("artist1", TablesEnum.beatport).get());

        dBqueriesClass.updateArtistSourceID("artist1", TablesEnum.beatport, "newID");

        assertEquals("newID", dBqueriesClass.getArtistSourceID("artist1", TablesEnum.beatport).get());
    }

    @Test
    void nullArtistSourceID() {
        assertEquals("IDBP", dBqueriesClass.getArtistSourceID("artist1", TablesEnum.beatport).get());

        dBqueriesClass.updateArtistSourceID("artist1", TablesEnum.beatport, null);

        assertTrue(dBqueriesClass.getArtistSourceID("artist1", TablesEnum.beatport).isEmpty());
    }

    @Test
    void insertNewArtist() {
        assertFalse(dBqueriesClass.getArtistList().contains("joe"));

        dBqueriesClass.insertIntoArtistList("joe");

        assertTrue(dBqueriesClass.getArtistList().contains("joe"));
    }

    @Test
    void noFilterMatchSongPasses() {
        HashMap<String, String> filterWords = new HashMap<>();
        filterWords.put("remix", "true");

        assertTrue(dBqueriesClass.songPassesFilterCheck(new Song("song", "", "", "type", null), filterWords));
    }

    @Test
    void filtersSongDueToUnwantedName() {
        HashMap<String, String> filterWords = new HashMap<>();
        filterWords.put("remix", "true");

        assertFalse(dBqueriesClass.songPassesFilterCheck(new Song("REMIXsong", "", "", "type", null), filterWords));
    }

    @Test
    void filtersSongDueToUnwantedType() {
        HashMap<String, String> filterWords = new HashMap<>();
        filterWords.put("remix", "true");

        assertFalse(dBqueriesClass.songPassesFilterCheck(new Song("song", "", "", "typeRemix", null), filterWords));
    }

    @Test
    void noFilterMatchSongWithNullTypePasses() {
        HashMap<String, String> filterWords = new HashMap<>();
        filterWords.put("remix", "true");

        assertTrue(dBqueriesClass.songPassesFilterCheck(new Song("song", "", "", null, null), filterWords));
    }

    @Test
    void filtersSongWithNullTypeDueToUnwantedName() {
        HashMap<String, String> filterWords = new HashMap<>();
        filterWords.put("remix", "true");

        assertFalse(dBqueriesClass.songPassesFilterCheck(new Song("songRemix)", "", "", null, null), filterWords));
    }

    @Test
    void getDataFromSourceTablesForCombviewWithFiltering() {
        HashMap<String, String> filterWords = new HashMap<>();
        filterWords.put("remix", "true");
        when(settingsIO.getFilterValues()).thenReturn(filterWords);
        dBqueriesClass.batchInsertSongs(songList, TablesEnum.beatport, 10);
        songList = List.of(
                new Song("songRemixed", "artist", "2022-01-01", null, null));
        dBqueriesClass.batchInsertSongs(songList, TablesEnum.youtube, 10);

        assertEquals(1, dBqueriesClass.getSourceTablesDataForCombview().size());
    }

    @Test
    void truncateCombviewTable() {
        dBqueriesClass.batchInsertCombview(songList);
        int entries = HelperDB.getNumEntries(HelperDB.testDBpath, "combview", "beatport");
        assertEquals(3, entries);

        dBqueriesClass.truncateCombview();

        entries = HelperDB.getNumEntries(HelperDB.testDBpath, "combview", "beatport");
        assertEquals(0, entries);
    }

    @Test
    void truncateAllScrapeTables() {
        songList = List.of(
                new Song("song1", "artist1", "2022-01-01", null, null),
                new Song("song2", "artist1", "2022-01-01", null, null));
        dBqueriesClass.batchInsertSongs(songList, TablesEnum.musicbrainz, 10);
        dBqueriesClass.batchInsertSongs(songList, TablesEnum.beatport, 10);
        dBqueriesClass.batchInsertCombview(songList);
        int entries = HelperDB.getNumEntries(HelperDB.testDBpath, "combview", "musicbrainz", "beatport");
        assertEquals(6, entries);

        dBqueriesClass.truncateAllTables();

        entries = HelperDB.getNumEntries(HelperDB.testDBpath, "combview", "musicbrainz", "beatport");
        assertEquals(0, entries);
    }

    @Test
    void getScrapersFromIDs() {
        assertEquals(6, dBqueriesClass.getAllScrapers().size());
    }

    @Test
    void clearArtistDataFrom() {
        ;
        dBqueriesClass.batchInsertSongs(songList, TablesEnum.beatport, 10);
        assertEquals(2, HelperDB.getCountOf(HelperDB.testDBpath, "beatport", "artist", "artist1"));

        dBqueriesClass.clearArtistDataFrom("artist1", TablesEnum.beatport);

        assertEquals(0, HelperDB.getCountOf(HelperDB.testDBpath, "beatport", "artist", "artist1"));
        assertEquals(1, HelperDB.getCountOf(HelperDB.testDBpath, "beatport", "artist", "artist2"));
    }

    @Test
    void deleteArtistFromAllTables() {
        dBqueriesClass.batchInsertSongs(songList, TablesEnum.beatport, 10);
        dBqueriesClass.batchInsertCombview(songList);
        assertEquals(1, HelperDB.getCountOf(HelperDB.testDBpath, "artists", "artist", "artist1"));
        assertEquals(2, HelperDB.getCountOf(HelperDB.testDBpath, "beatport", "artist", "artist1"));
        assertEquals(2, HelperDB.getCountOf(HelperDB.testDBpath, "combview", "artist", "artist1"));

        dBqueriesClass.removeArtistFromAllTables("artist1");

        assertEquals(0, HelperDB.getCountOf(HelperDB.testDBpath, "artists", "artist", "artist1"));
        assertEquals(0, HelperDB.getCountOf(HelperDB.testDBpath, "beatport", "artist", "artist1"));
        assertEquals(0, HelperDB.getCountOf(HelperDB.testDBpath, "combview", "artist", "artist1"));
    }


}
