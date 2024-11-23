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
    public final static String testDBpath = "jdbc:sqlite:" + Paths.get("src", "test", "testresources", "testdb.db");
    public final static Path DBfilePath = Paths.get("src", "test", "testresources", "testdb.db");

    public final static String legacyTestDBpath = "jdbc:sqlite:" + Paths.get("src", "test", "testresources", "musicdata.db");
    public final static String testTemplateDBpath = "jdbc:sqlite:" + Paths.get("src", "test", "testresources", "DBTemplate.db");

    public static void deleteDB() {
        try {
            if (Files.exists(DBfilePath))
                Files.delete(DBfilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void redoTestDB() {
        try {
            if (!Files.exists(testResources))
                Files.createDirectory(testResources);
            if (Files.exists(DBfilePath))
                Files.delete(DBfilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MigrateDB manageDB = new MigrateDB(null, null);
        manageDB.createDBandSourceTables(testDBpath);
    }

    public static void redoTestData() {
        clearTables();
        insertIntoArtists("artist1", "artist2", "artist3");
    }

    public static int getCountOf(String table, String col, String name) {
        int count = 0;
        try (Connection conn = DriverManager.getConnection(testDBpath)) {
            String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + col + " = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            count = pstmt.executeQuery().getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return count;
    }

    public static int getNumEntries(String... tables) {
        int count = 0;
        try (Connection conn = DriverManager.getConnection(testDBpath)) {
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

    private static void clearTables() {
        try (Connection conn = DriverManager.getConnection(testDBpath)) {
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

    private static void insertIntoArtists(String... artists) {
        try (Connection conn = DriverManager.getConnection(testDBpath)) {
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

    public static void createDBv1(String path) {
        try (Connection conn = DriverManager.getConnection(path)) {
            Statement stmt = conn.createStatement();
            stmt.addBatch("""
                CREATE TABLE IF NOT EXISTS artists (
                artist text PRIMARY KEY,
                urlbeatport text
                );
                """);
            stmt.addBatch("""
                CREATE TABLE IF NOT EXISTS combview (
                song text NOT NULL,
                artist text NOT NULL,
                date text NOT NULL
                );
                """);
            conn.setAutoCommit(false);
            stmt.executeBatch();
            conn.setAutoCommit(true);
            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createDBv2(String path) {
        try (Connection conn = DriverManager.getConnection(path)) {
            Statement stmt = conn.createStatement();
            stmt.addBatch("""
                CREATE TABLE IF NOT EXISTS artists (
                artist text PRIMARY KEY,
                urlbeatport text
                );
                """);
            stmt.addBatch("""
                CREATE TABLE IF NOT EXISTS combview (
                song text NOT NULL,
                artist text NOT NULL,
                date text NOT NULL,
                album text
                );
                """);
            conn.setAutoCommit(false);
            stmt.executeBatch();
            conn.setAutoCommit(true);
            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createDBv3(String path) {
        try (Connection conn = DriverManager.getConnection(path)) {
            Statement stmt = conn.createStatement();
            stmt.addBatch("""
                CREATE TABLE IF NOT EXISTS artists (
                artist text PRIMARY KEY,
                urlbeatport text
                );
                """);
            conn.setAutoCommit(false);
            stmt.executeBatch();
            conn.setAutoCommit(true);
            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteLegacyFiles(){
        Path s1 = Paths.get("src", "test", "testresources", "musicdata.db");
        Path s2 = Paths.get("src", "test", "testresources", "DBTemplate.db");
			try {
                if (Files.exists(s1))
				    Files.delete(s1);
                if (Files.exists(s2))
                    Files.delete(s2);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

}
