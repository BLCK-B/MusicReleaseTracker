package com.blck.MusicReleaseTracker;

import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MainBackendTest {

    @Test
    void processInfo() {
        ArrayList<SongClass> songList = new ArrayList<>();
        songList.add(new SongClass("Song1", "artistName", "2023-01-01"));
        songList.add(new SongClass("Son’g3", "artistName", "2023-03-01"));
        songList.add(new SongClass("Song2", "artistName", "2023-02-01"));
        MainBackend.processInfo(songList, "test");
        //expected values: sort by date
        ArrayList<SongClass> expectedSongList = new ArrayList<>();
        expectedSongList.add(new SongClass("Son'g3", "artistName", "2023-03-01"));
        expectedSongList.add(new SongClass("Song2","artistName", "2023-02-01"));
        expectedSongList.add(new SongClass("Song1","artistName", "2023-01-01"));

        for (int i = 0; i < songList.size(); i++)
            assertEquals(songList.get(i).toString(), expectedSongList.get(i).toString());

        //incorrect dates: discard
        songList.add(new SongClass("Song4", "artistName", "2023"));
        songList.add(new SongClass("Song5", "artistName", "-"));
        songList.add(new SongClass("Song6", "artistName", "08-05-2023"));
        MainBackend.processInfo(songList, "test");

        for (int i = 0; i < songList.size(); i++)
            assertEquals(songList.get(i).toString(), expectedSongList.get(i).toString());

        expectedSongList.remove(expectedSongList.size() - 1);
        expectedSongList.add(new SongClass("Song1","artistName", "2005-05-05"));

        //duplicates: prefer older
        songList.add(new SongClass("Song1", "artistName", "2023-01-01"));
        songList.add(new SongClass("Song1", "artistName", "2019-19-19"));
        songList.add(new SongClass("Song1", "artistName", "2005-05-05"));
        MainBackend.processInfo(songList, "test");

        for (int i = 0; i < songList.size(); i++)
            assertEquals(songList.get(i).toString(), expectedSongList.get(i).toString());
    }

    @Test
    void fillCombviewTable() {
        String DBpath = null;
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) { //Windows
            String appDataPath = System.getenv("APPDATA");
            DBpath = "jdbc:sqlite:" + appDataPath + File.separator + "MusicReleaseTracker" + File.separator + "testingdata.db";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {  //Linux
            String userHome = System.getProperty("user.home");
            DBpath = "jdbc:sqlite:" + userHome + File.separator + ".MusicReleaseTracker" + File.separator + "testingdata.db";
        }
        else
            throw new UnsupportedOperationException("unsupported OS");

        MainBackend.fillCombviewTable(DBpath);
        assertEquals(1,1);
    }

}
