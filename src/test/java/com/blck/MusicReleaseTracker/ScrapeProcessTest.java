package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Simple.SongClass;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ScrapeProcessTest {

    private final ScrapeProcess scrapeProcess;

    @Autowired
    public ScrapeProcessTest(ScrapeProcess scrapeProcess) {
        this.scrapeProcess = scrapeProcess;
    }

    @Test
    void processInfo() {
        ArrayList<SongClass> songList = new ArrayList<>();
        songList.add(new SongClass("Song1", "artistName", "2023-01-01"));
        songList.add(new SongClass("Son’g3", "artistName", "2023-03-01"));
        songList.add(new SongClass("Song2", "artistName", "2023-02-01"));
        scrapeProcess.processInfo(songList, "test");
        // expected values: sort by date
        ArrayList<SongClass> expectedSongList = new ArrayList<>();
        expectedSongList.add(new SongClass("Son'g3", "artistName", "2023-03-01"));
        expectedSongList.add(new SongClass("Song2","artistName", "2023-02-01"));
        expectedSongList.add(new SongClass("Song1","artistName", "2023-01-01"));

        for (int i = 0; i < songList.size(); i++)
            assertEquals(songList.get(i).toString(), expectedSongList.get(i).toString());

        // incorrect dates: discard
        songList.add(new SongClass("Song4", "artistName", "2023"));
        songList.add(new SongClass("Song5", "artistName", "-"));
        songList.add(new SongClass("Song6", "artistName", "08-05-2023"));
        scrapeProcess.processInfo(songList, "test");

        for (int i = 0; i < songList.size(); i++)
            assertEquals(songList.get(i).toString(), expectedSongList.get(i).toString());

        expectedSongList.remove(expectedSongList.size() - 1);
        expectedSongList.add(new SongClass("Song1","artistName", "2005-05-05"));

        // duplicates: prefer older
        songList.add(new SongClass("Song1", "artistName", "2023-01-01"));
        songList.add(new SongClass("Song1", "artistName", "2019-19-19"));
        songList.add(new SongClass("Song1", "artistName", "2005-05-05"));
        scrapeProcess.processInfo(songList, "test");

        for (int i = 0; i < songList.size(); i++)
            assertEquals(songList.get(i).toString(), expectedSongList.get(i).toString());
    }

    @Test
    void fillCombviewTable() {
        String DBpath = null;
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) { // Windows
            String appDataPath = System.getenv("APPDATA");
            DBpath = "jdbc:sqlite:" + appDataPath + File.separator + "MusicReleaseTracker" + File.separator + "testingdata.db";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {  // Linux
            String userHome = System.getProperty("user.home");
            DBpath = "jdbc:sqlite:" + userHome + File.separator + ".MusicReleaseTracker" + File.separator + "testingdata.db";
        }
        else
            throw new UnsupportedOperationException("unsupported OS");

        scrapeProcess.fillCombviewTable(DBpath);

        ArrayList<SongClass> songList = new ArrayList<>();
        ArrayList<SongClass> expectedSongList = new ArrayList<>();
        expectedSongList.add(new SongClass("Collab", "B, L, K", "2023-11-10"));
        expectedSongList.add(new SongClass("duplicates", "K", "2023-07-27"));
        expectedSongList.add(new SongClass("DiffDates", "B", "2000-30-30"));

        try {
            Connection conn = DriverManager.getConnection(DBpath);
            String sql = "SELECT * FROM combview ORDER BY date DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String song = rs.getString("song");
                String artist = rs.getString("artist");
                String date = rs.getString("date");
                songList.add(new SongClass(song, artist, date));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < expectedSongList.size(); i++) {
            SongClass obj1 = expectedSongList.get(i);
            SongClass obj2 = songList.get(i);
            assertAll("Combview table rows",
                () -> assertEquals(obj1.getName(), obj2.getName()),
                () -> assertEquals(obj1.getArtist(), obj2.getArtist()),
                () -> assertEquals(obj1.getDate(), obj2.getDate())
            );
        }

    }

    @Test
    void reduceToID() {
        ArrayList<String> input = new ArrayList<String>();
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
                String output = scrapeProcess.reduceToID(input.get(i), "beatport");
                assertEquals("artistname/1234", output);
            }
            // musicbrainz
            else if (i < 6) {
                String output = scrapeProcess.reduceToID(input.get(i), "musicbrainz");
                assertEquals("123-id-123", output);
            }
            // junodownload
            else if (i < 9) {
                String output = scrapeProcess.reduceToID(input.get(i), "junodownload");
                assertEquals("artistname", output);
            }
            // youtube
            else {
                String output = scrapeProcess.reduceToID(input.get(i), "youtube");
                assertEquals("123-id-123", output);
            }
        }

    }


}
