package com.blck.MusicReleaseTracker;
import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.TablesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.DB.ManageMigrateDB;
import com.blck.MusicReleaseTracker.DataObjects.Song;
import com.blck.MusicReleaseTracker.JsonSettings.SettingsIO;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

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
public class DBqueriesTest {

    final static String testDBpath = "jdbc:sqlite:" + Paths.get("src", "test", "testresources", "testdb.db");

    @Mock
    ValueStore store;
    @Mock
    ManageMigrateDB manageMigrateDB;
    @Mock
    ErrorLogging log;
    @Mock
    SettingsIO settingsIO;
    @InjectMocks
    DBqueries dBqueriesClass;

    ArrayList<Song> songList;

    @BeforeAll
    static void setUpDB() {
        HelperDB.redoTestDB();
    }

    @BeforeEach
    void setUp() {
        HelperDB.redoTestData();
        lenient().when(store.getDBpathString()).thenReturn(testDBpath);
        dBqueriesClass = new DBqueries(store, log, settingsIO, manageMigrateDB);
        songList = new ArrayList<>();
        songList.add(new Song("song1", "artist1", "2022-01-01", "remix"));
        songList.add(new Song("song2", "artist1", "2022-01-01", "type"));
        songList.add(new Song("song3", "artist2", "2022-01-01", "Remixed"));
    }

    @Test
    void batchInsertIntoSource() {
        dBqueriesClass.batchInsertSongs(songList, TablesEnum.beatport, 10);

        assertEquals(3, HelperDB.getNumEntries("beatport"));
    }

    @Test
    void batchInsertIntoCombview() {
        dBqueriesClass.batchInsertSongs(songList, TablesEnum.combview, 10);

        assertEquals(3, HelperDB.getNumEntries("combview"));
    }

    @Test
    void batchInsertOverLimit() {
        dBqueriesClass.batchInsertSongs(songList, TablesEnum.combview, 1);

        assertEquals(1, HelperDB.getNumEntries("combview"));
    }

    @Test
    void getArtistEntriesInSourceTable() {
        dBqueriesClass.batchInsertSongs(songList, TablesEnum.beatport, 10);

        assertEquals(2, dBqueriesClass.loadTable(TablesEnum.beatport, "artist1").size());
    }

    @Test
    void getEntriesInCombviewTable() {
        dBqueriesClass.batchInsertSongs(songList, TablesEnum.combview, 10);

        assertEquals(3, dBqueriesClass.loadCombviewTable().size());
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

        assertTrue(dBqueriesClass.songPassesFilterCheck(new Song("song", "", "", "type"), filterWords));
    }

    @Test
    void filtersSongDueToUnwantedName() {
        HashMap<String, String> filterWords = new HashMap<>();
        filterWords.put("remix", "true");

        assertFalse(dBqueriesClass.songPassesFilterCheck(new Song("REMIXsong", "", "", "type"), filterWords));
    }

    @Test
    void filtersSongDueToUnwantedType() {
        HashMap<String, String> filterWords = new HashMap<>();
        filterWords.put("remix", "true");

        assertFalse(dBqueriesClass.songPassesFilterCheck(new Song("song", "", "", "typeRemix"), filterWords));
    }

    @Test
    void noFilterMatchSongWithNullTypePasses() {
        HashMap<String, String> filterWords = new HashMap<>();
        filterWords.put("remix", "true");

        assertTrue(dBqueriesClass.songPassesFilterCheck(new Song("song", "", "", null), filterWords));
    }

    @Test
    void filtersSongWithNullTypeDueToUnwantedName() {
        HashMap<String, String> filterWords = new HashMap<>();
        filterWords.put("remix", "true");

        assertFalse(dBqueriesClass.songPassesFilterCheck(new Song("songRemix)", "", "", null), filterWords));
    }

    @Test
    void getDataFromSourceTablesForCombviewWithFiltering() {
        HashMap<String, String> filterWords = new HashMap<>();
        filterWords.put("remix", "true");
        when(settingsIO.getFilterValues()).thenReturn(filterWords);
        dBqueriesClass.batchInsertSongs(songList, TablesEnum.beatport, 10);
        songList.clear();
        songList.add(new Song("songRemixed", "artist", "2022-01-01", null));
        dBqueriesClass.batchInsertSongs(songList, TablesEnum.youtube, 10);

        assertEquals(1, dBqueriesClass.getSourceTablesDataForCombview().size());
    }

    @Test
    void truncateCombviewTable() {
        dBqueriesClass.batchInsertSongs(songList, TablesEnum.combview, 10);
        int entries = HelperDB.getNumEntries("combview", "beatport");
        assertEquals(3, entries);

        dBqueriesClass.truncateCombview();

        entries = HelperDB.getNumEntries("combview", "beatport");
        assertEquals(0, entries);
    }

    @Test
    void truncateAllScrapeTables() {
        songList = new ArrayList<>();
        songList.add(new Song("song1", "artist1", "2022-01-01", null));
        songList.add(new Song("song2", "artist1", "2022-01-01", null));
        dBqueriesClass.batchInsertSongs(songList, TablesEnum.musicbrainz, 10);
        dBqueriesClass.batchInsertSongs(songList, TablesEnum.junodownload, 10);
        dBqueriesClass.batchInsertSongs(songList, TablesEnum.combview, 10);
        int entries = HelperDB.getNumEntries("combview", "musicbrainz", "junodownload");
        assertEquals(6, entries);

        dBqueriesClass.truncateAllTables();

        entries = HelperDB.getNumEntries("combview", "musicbrainz", "junodownload");
        assertEquals(0, entries);
    }

    @Test
    void getScrapersFromIDs() {
        assertEquals(6, dBqueriesClass.getAllScrapers().size());
    }

    @Test
    void clearArtistDataFrom() {;
        dBqueriesClass.batchInsertSongs(songList, TablesEnum.beatport, 10);
        assertEquals(2, HelperDB.getCountOf("beatport", "artist", "artist1"));

        dBqueriesClass.clearArtistDataFrom("artist1", TablesEnum.beatport);

        assertEquals(0, HelperDB.getCountOf("beatport", "artist", "artist1"));
        assertEquals(1, HelperDB.getCountOf("beatport", "artist", "artist2"));
    }

    @Test
    void deleteArtistFromAllTables() {
        dBqueriesClass.batchInsertSongs(songList, TablesEnum.beatport, 10);
        dBqueriesClass.batchInsertSongs(songList, TablesEnum.combview, 10);
        assertEquals(1, HelperDB.getCountOf("artists", "artist", "artist1"));
        assertEquals(2, HelperDB.getCountOf("beatport", "artist", "artist1"));
        assertEquals(2, HelperDB.getCountOf("combview", "artist", "artist1"));

        dBqueriesClass.removeArtistFromAllTables("artist1");

        assertEquals(0, HelperDB.getCountOf("artists", "artist", "artist1"));
        assertEquals(0, HelperDB.getCountOf("beatport", "artist", "artist1"));
        assertEquals(0, HelperDB.getCountOf("combview", "artist", "artist1"));
    }

    @AfterAll
    static void cleanUp() {
        HelperDB.redoTestData();
    }



}
