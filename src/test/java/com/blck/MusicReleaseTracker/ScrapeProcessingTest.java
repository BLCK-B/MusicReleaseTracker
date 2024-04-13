package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Core.SourcesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.Scrapers.*;
import com.blck.MusicReleaseTracker.Simple.Song;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

@SpringBootTest
public class ScrapeProcessingTest {

    private final ValueStore store = new ValueStore();
    private final DBtools DB = new DBtools(store, null);
    private final ScrapeProcess testedSP;

    public ScrapeProcessingTest() {
        // data setup
        String DBpath = "jdbc:sqlite:" + Paths.get("src", "test", "testresources", "testdb.db");
        store.setDBpath(DBpath);

        ArrayList<String> filterWords = new ArrayList<>();
        filterWords.add("XXXXX");
        store.setFilterWords(filterWords);

        testedSP = new ScrapeProcess(store, null, null, DB, null);
    }

    @Test
    void processInfo() {
        ScraperParent scraperInstace = new ScraperParent(null, null);
        ArrayList<Song> songList = new ArrayList<>();
        songList.add(new Song("Song1", "artistName", "2023-01-01"));
        songList.add(new Song("Son’g3", "artistName", "2023-03-01"));
        songList.add(new Song("Song2", "artistName", "2023-02-01"));
        scraperInstace.setTestData(songList, SourcesEnum.beatport);
        scraperInstace.processInfo();
        // expected values: sort by date
        ArrayList<Song> expectedSongList = new ArrayList<>();
        expectedSongList.add(new Song("Son'g3", "artistName", "2023-03-01"));
        expectedSongList.add(new Song("Song2", "artistName", "2023-02-01"));
        expectedSongList.add(new Song("Song1", "artistName", "2023-01-01"));

        for (int i = 0; i < songList.size(); i++)
            assertEquals(songList.get(i).toString(), expectedSongList.get(i).toString());

        // incorrect dates: discard
        songList.add(new Song("Song4", "artistName", "2023"));
        songList.add(new Song("Song5", "artistName", "-"));
        songList.add(new Song("Song6", "artistName", "08-05-2023"));
        scraperInstace.setTestData(songList, SourcesEnum.beatport);
        scraperInstace.processInfo();

        for (int i = 0; i < songList.size(); i++)
            assertEquals(songList.get(i).toString(), expectedSongList.get(i).toString());

        expectedSongList.remove(expectedSongList.size() - 1);
        expectedSongList.add(new Song("Song1", "artistName", "2005-05-05"));

        // duplicates: prefer older
        songList.add(new Song("Song1", "artistName", "2023-01-01"));
        songList.add(new Song("Song1", "artistName", "2019-19-19"));
        songList.add(new Song("Song1", "artistName", "2005-05-05"));
        scraperInstace.setTestData(songList, SourcesEnum.beatport);
        scraperInstace.processInfo();

        for (int i = 0; i < songList.size(); i++)
            assertEquals(songList.get(i).toString(), expectedSongList.get(i).toString());
    }

    @Test
    void fillCombviewTable() {
        testedSP.fillCombviewTable();

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

    @Test
    void reduceToID() {
        ArrayList<String> input = new ArrayList<>();
        // beatport
        input.add("https://www.beatport.com/artist/artistname/1234/tracks");
        input.add("https://www.beatport.com/artist/artistname/1234");
        input.add("artistname/1234");
        // musicbrainz
        input.add("https://musicbrainz.org/artist/123-id-123/releases");
        input.add("https://musicbrainz.org/artist/123-id-123");
        input.add("123-id-123");
        // junodownload
        input.add("https://www.junodownload.com/artists/artistname/releases/");
        input.add("https://www.junodownload.com/artists/artistname");
        input.add("artistname");
        // youtube
        input.add("https://www.youtube.com/channel/123-id-123");
        input.add("123-id-123");

        for (int i = 0; i < input.size(); i++) {
            // beatport
            if (i < 3) {
                BeatportScraper scraper = new BeatportScraper(null, null, null, input.get(i));
                String output = scraper.getID();
                assertEquals("artistname/1234", output);
            }
            // musicbrainz
            else if (i < 6) {
                MusicbrainzScraper scraper = new MusicbrainzScraper(null, null, null, input.get(i));
                String output = scraper.getID();
                assertEquals("123-id-123", output);
            }
            // junodownload
            else if (i < 9) {
                JunodownloadScraper scraper = new JunodownloadScraper(null, null, null, input.get(i));
                String output = scraper.getID();
                assertEquals("artistname", output);
            }
            // youtube
            else {
                YoutubeScraper scraper = new YoutubeScraper(null, null, null, input.get(i));
                String output = scraper.getID();
                assertEquals("123-id-123", output);
            }
        }

    }


}
