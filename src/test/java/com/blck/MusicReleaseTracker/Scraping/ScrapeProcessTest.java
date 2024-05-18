package com.blck.MusicReleaseTracker.Scraping;

import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.DB.DBqueriesClass;
import com.blck.MusicReleaseTracker.DataObjects.Song;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.event.annotation.BeforeTestClass;

import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;

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

public class ScrapeProcessTest {

    private ValueStore store = new ValueStore();
    private DBqueries DB = new DBqueriesClass(store, null, null);
    private ScrapeProcess scrapeProcess;
    private ArrayList<Song> expectedList;

    @BeforeTestClass
    void beforeClass() {
        // data setup
        String DBpath = "jdbc:sqlite:" + Paths.get("src", "test", "testresources", "testdb.db");
        store.setDBpath(DBpath);

        ArrayList<String> filterWords = new ArrayList<>();
        filterWords.add("XXXXX");
        store.setFilterWords(filterWords);
        scrapeProcess = new ScrapeProcess(store, null, null, DB, null);
    }

    @BeforeEach
    void setUp() {
        // TODO: granular setup of instance store
//        scrapeProcess = new ScrapeProcess(store, null, null, DB, null);
    }

    @Disabled("do not forget")
    @Test
    void fillCombviewTable() {
        scrapeProcess.fillCombviewTable();

        ArrayList<Song> songList = new ArrayList<>();
        ArrayList<Song> expectedSongList = new ArrayList<>();
        expectedSongList.add(new Song("Collab", "B, L, K", "2023-11-10"));
        expectedSongList.add(new Song("duplicates", "K", "2023-07-27"));
        expectedSongList.add(new Song("DiffDates", "B", "2000-30-30"));

        try (Connection conn = DriverManager.getConnection(store.getDBpath())) {
            String sql = "SELECT * FROM combview ORDER BY date DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String song = rs.getString("song");
                String artist = rs.getString("artist");
                String date = rs.getString("date");
                songList.add(new Song(song, artist, date));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < expectedSongList.size(); i++) {
            Song obj1 = expectedSongList.get(i);
            Song obj2 = songList.get(i);
            assertAll("Combview table rows",
                    () -> assertEquals(obj1.getName(), obj2.getName()),
                    () -> assertEquals(obj1.getArtist(), obj2.getArtist()),
                    () -> assertEquals(obj1.getDate(), obj2.getDate())
            );
        }
    }

}
