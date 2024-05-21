package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Core.SourcesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DB.ManageMigrateDB;
import org.sqlite.core.DB;

import java.io.File;
import java.sql.*;

public class HelperDB {

    public static String DBpath;

    public static void redoTestDB() {
        File testDB = new File(DBpath);
        if (testDB.exists())
            testDB.delete();

        ManageMigrateDB manageDB = new ManageMigrateDB(null, null);
        manageDB.createDBandSourceTables(DBpath);
    }

    public static void redoTestData() {
        clearTables();
        insertIntoArtists("artist1", "artist2", "artist3");
        insertIntoSource(SourcesEnum.beatport, "song1", "artist1", "2022-01-01", "type");
        insertIntoSource(SourcesEnum.musicbrainz, "song1", "artist1", "2022-01-01", "type");
    }

    private static void clearTables() {
        try (Connection conn = DriverManager.getConnection(DBpath)) {
            for (SourcesEnum sourceTable : SourcesEnum.values()) {
                String sql = "DELETE FROM " + sourceTable;
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(sql);
            }
            String sql = "DELETE FROM combview";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void insertIntoArtists(String... artists) {
        try (Connection conn = DriverManager.getConnection(DBpath)) {
            // artists table
            for (String artist : artists) {
                String sql = "INSERT INTO artists (artist, urlmusicbrainz, urlbeatport) values(?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, artist);
                pstmt.setString(2, "IDMB");
                pstmt.setString(3, "IDBP");
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void insertIntoSource(SourcesEnum source, String song, String artist, String date, String type) {
        try (Connection conn = DriverManager.getConnection(DBpath)) {
            String sql;
            if (type != null)
                sql = "INSERT INTO " + source + "(song, artist, date, type) values(?, ?, ?, ?)";
            else
                sql = "INSERT INTO " + source + "(song, artist, date) values(?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            if (type != null) {
                pstmt.setString(1, song);
                pstmt.setString(2, artist);
                pstmt.setString(3, date);
                pstmt.setString(4, type);
            }
            else {
                pstmt.setString(1, song);
                pstmt.setString(2, artist);
                pstmt.setString(3, date);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
