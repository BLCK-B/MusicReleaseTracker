/*
 *         MusicReleaseTracker
 *         Copyright (C) 2023 - 2024 BLCK
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

import com.blck.MusicReleaseTracker.Core.TablesEnum;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

public class HelperDB {

    public final static Path testResources = Paths.get("src", "test", "testresources");
    public final static Path testDBpath = Paths.get("src", "test", "testresources", "testdb.db");
    public final static Path testTemplateDBpath = Paths.get("src", "test", "testresources", "DBTemplate.db");

    public static void deleteDB() {
        try {
            if (Files.exists(testDBpath))
                Files.delete(testDBpath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void redoTestDB() {
        try {
            if (!Files.exists(testResources))
                Files.createDirectory(testResources);
            if (Files.exists(testDBpath))
                Files.delete(testDBpath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MigrateDB manageDB = new MigrateDB(null, null);
        manageDB.createDBandTables(testDBpath);
    }

    public static void redoTestData(Path path) {
        clearTables(path);
        insertIntoArtists(path, "artist1", "artist2", "artist3");
    }

    public static int getCountOf(Path path, String table, String col, String name) {
        int count = 0;
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + path)) {
            String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + col + " = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            count = pstmt.executeQuery().getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return count;
    }

    public static int getNumEntries(Path path, String... tables) {
        if (tables.length == 0)
            throw new RuntimeException("no tables specified");
        int count = 0;
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + path)) {
            for (String table : tables) {
                String sql = "SELECT COUNT(*) FROM " + table;
                Statement statement = conn.createStatement();
                count += statement.executeQuery(sql).getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return count;
    }

    public static boolean isArtistsColumnNotEmpty(Path path, String column) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + path)) {
             Statement stmt = conn.createStatement();
             ResultSet resultSet = stmt.executeQuery(
                     "SELECT 1 FROM artists WHERE " + column + " IS NOT NULL LIMIT 1");
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException("Error checking if column contains data", e);
        }
    }

    private static void clearTables(Path path) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + path)) {
            Statement stmt = conn.createStatement();
            for (TablesEnum sourceTable : TablesEnum.values())
                stmt.addBatch("DELETE FROM " + sourceTable);
            stmt.addBatch("DELETE FROM artists");
            conn.setAutoCommit(false);
            stmt.executeBatch();
            conn.setAutoCommit(true);
            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void insertIntoArtists(Path path, String... artists) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + path)) {
            String sql = "INSERT INTO artists (artist, urlmusicbrainz, urlbeatport) values(?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            for (String artist : artists) {
                pstmt.setString(1, artist);
                pstmt.setString(2, "IDMB");
                pstmt.setString(3, "IDBP");
                pstmt.addBatch();
            }
            conn.setAutoCommit(false);
            pstmt.executeBatch();
            conn.setAutoCommit(true);
            pstmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteTestDBs() {
		try {
			Files.deleteIfExists(testDBpath);
            Files.deleteIfExists(testTemplateDBpath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }

    public static void createArtistsTestDB(String path, int columns) {
        try (Connection conn = DriverManager.getConnection(path)) {
            Statement stmt = conn.createStatement();
            switch (columns) {
                case 1 -> stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS artists (
                artist text PRIMARY KEY
                );
                """);
                case 2 -> stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS artists (
                artist text PRIMARY KEY,
                urlbeatport text
                );
                """);
                case 3 -> stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS artists (
                artist text PRIMARY KEY,
                urlbeatport text,
                urlmusicbrainz text
                );
                """);
                default -> throw new RuntimeException("invalid number");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void fillArtistsTable(Path path, int columns) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + path)) {
            PreparedStatement pstmt = conn.prepareStatement(
                switch (columns) {
                    case 1 -> "INSERT INTO artists (artist) values(?)";
                    case 2 -> "INSERT INTO artists (artist, urlbeatport) values(?, ?)";
                    case 3 -> "INSERT INTO artists (artist, urlbeatport, urlmusicbrainz) values(?, ?, ?)";
                    default -> throw new RuntimeException("invalid number");
                }
            );
            for (int i = 0; i < 3; ++i) {
                switch (columns) {
                    case 1 -> pstmt.setString(1, Integer.toString(i));
                    case 2 -> {
                        pstmt.setString(1, Integer.toString(i));
                        pstmt.setString(2, Integer.toString(i));
                    }
                    case 3 -> {
                        pstmt.setString(1, Integer.toString(i));
                        pstmt.setString(2, Integer.toString(i));
                        pstmt.setString(3, Integer.toString(i));
                    }
                }
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
