package com.blck.MusicReleaseTracker.Scrapers;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.SourcesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.Simple.Song;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class ScraperParent {

    protected final ValueStore store;
    protected final ErrorLogging log;
    protected ArrayList<Song> songList;
    SourcesEnum source;

    @Autowired
    public ScraperParent(ValueStore valueStore, ErrorLogging errorLogging) {
        this.store = valueStore;
        this.log = errorLogging;
    }

    public void setTestData(ArrayList<Song> songList, SourcesEnum source) {
        this.songList = songList;
        this.source = source;
    }

    public void scrape() throws ScraperTimeoutException {
        System.out.println("The method scrape() is to be overriden.");
    }

    public String getID() {
        return "The method getID() is to be overriden.";
    }

    public void processInfo() {
        // unify apostrophes/grave accents/backticks/quotes...
        for (Song object : songList) {
            String songName = object.getName().replace("’", "'").replace("`", "'").replace("´", "'");
            object.setName(songName);
        }
        // discard objects with an incorrect date format
        songList.removeIf(obj -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            try {
                LocalDate.parse(obj.getDate(), formatter);
                return false;
            } catch (DateTimeParseException e) {
                return true;
            }
        });
        // sort by date from oldest
        songList.sort((obj1, obj2) -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date1 = LocalDate.parse(obj1.getDate(), formatter);
            LocalDate date2 = LocalDate.parse(obj2.getDate(), formatter);
            return date1.compareTo(date2);
        });
        // remove name duplicates
        Set<String> recordedNames = new HashSet<>();
        songList.removeIf(obj -> {
            String name = obj.getName().toLowerCase();
            if (recordedNames.contains(name))
                return true;
            else {
                recordedNames.add(name);
                return false;
            }
        });
        // reverse to newest-oldest
        Collections.reverse(songList);
    }

    public void insertSet() {
        PreparedStatement pstmt = null;
        // insert a set of songs to a source table
        try (Connection conn = DriverManager.getConnection(store.getDBpath())) {
            int i = 0;
            for (Song songObject : songList) {
                if (i == 15)
                    break;
                if (songObject.getType() != null) {
                    String sql = "insert into " + source + "(song, artist, date, type) values(?, ?, ?, ?)";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, songObject.getName());
                    pstmt.setString(2, songObject.getArtist());
                    pstmt.setString(3, songObject.getDate());
                    pstmt.setString(4, songObject.getType());
                } else {
                    String sql = "insert into " + source + "(song, artist, date) values(?, ?, ?)";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, songObject.getName());
                    pstmt.setString(2, songObject.getArtist());
                    pstmt.setString(3, songObject.getDate());
                }
                pstmt.executeUpdate();
                i++;
            }
            conn.setAutoCommit(false);
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error inserting a set of songs");
        }
    }

}
