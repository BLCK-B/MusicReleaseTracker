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

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MigrateDB {

    private final ValueStore store;
    private final ErrorLogging log;

    @Autowired
    public MigrateDB(ValueStore valueStore, ErrorLogging errorLogging) {
        this.store = valueStore;
        this.log = errorLogging;
    }

    public void createDBandSourceTables(String path) {
        // note: generate by string templates after preview
        try (Connection conn = DriverManager.getConnection(path)) {
            Statement stmt = conn.createStatement();

            stmt.addBatch("""
                    CREATE TABLE IF NOT EXISTS musicbrainz (
                    song text NOT NULL,
                    artist text NOT NULL,
                    date text NOT NULL
                    );
                    """);

            stmt.addBatch("""
                    CREATE TABLE IF NOT EXISTS beatport (
                    song text NOT NULL,
                    artist text NOT NULL,
                    date text NOT NULL,
                    type text NOT NULL
                    );
                    """);

            stmt.addBatch("""
                    CREATE TABLE IF NOT EXISTS junodownload (
                    song text NOT NULL,
                    artist text NOT NULL,
                    date text NOT NULL
                    );
                    """);

            stmt.addBatch("""
                    CREATE TABLE IF NOT EXISTS youtube (
                    song text NOT NULL,
                    artist text NOT NULL,
                    date text NOT NULL
                    );
                    """);

            stmt.addBatch("""
                    CREATE TABLE IF NOT EXISTS artists (
                    artist text PRIMARY KEY,
                    urlmusicbrainz text,
                    urlbeatport text,
                    urljunodownload text,
                    urlyoutube text
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
            log.error(e, ErrorLogging.Severity.SEVERE, "error creating DB file");
        }
    }

    public void migrateDB(Path DBfilePath, Path templateDBfilePath) {
        if (Files.exists(DBfilePath)) {
			try {
				Files.delete(templateDBfilePath);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
        final String DBpath = "jdbc:sqlite:" + DBfilePath;
        final String DBtemplatePath = "jdbc:sqlite:" + templateDBfilePath;
        createDBandSourceTables(DBpath);
        createDBandSourceTables(DBtemplatePath);

        var DBstructure = getDBStructure(DBpath);
        if (!DBstructure.equals(getDBStructure(DBtemplatePath))) {
            copyArtistsData(DBpath, DBtemplatePath);
            try {
                Files.delete(DBfilePath);
                File newFile = new File(String.valueOf(templateDBfilePath));
                newFile.renameTo(DBfilePath.toFile());
            } catch (Exception e) {
                log.error(e, ErrorLogging.Severity.SEVERE, "error renaming/deleting DB files");
            }
        }
    }

    public void copyArtistsData(String sourceDBPath, String targetDBpath) {
        if (targetDBpath.contains("jdbc:sqlite:"))
            throw new RuntimeException("targetDBpath cannot have sqlite prefix");

        String testTemplateDBpath = String.valueOf(Paths.get("src", "test", "testresources", "DBTemplate.db"));

        var DBcolumns = getDBStructure(sourceDBPath).get("artists");
        var templateColumns = getDBStructure(targetDBpath).get("artists");

//        if (!DBcolumns.equals(templateColumns))

        List<String> sharedColumns = new ArrayList<>(DBcolumns);
        sharedColumns.retainAll(templateColumns);
        String shared = String.join(", ", sharedColumns);

        try (Connection conn = DriverManager.getConnection(sourceDBPath)) {
            Statement stmt = conn.createStatement();
            stmt.execute("ATTACH DATABASE '" + testTemplateDBpath + "' AS target_db");
            stmt.executeUpdate("INSERT INTO target_db.artists (" + shared + ") SELECT " + shared + " FROM artists");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, ArrayList<String>> getDBStructure(String path) {
        // returns a structure of tables and their columns
        HashMap<String, ArrayList<String>> tableMap = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(path)) {
            String sql = "SELECT name FROM sqlite_master WHERE type='table'";
            Statement stmt = conn.createStatement();
            ResultSet rsTables = stmt.executeQuery(sql);
            ArrayList<String> tablesList = new ArrayList<>();

            while (rsTables.next())
                tablesList.add(rsTables.getString(1));

            for (String tableName : tablesList) {
                ArrayList<String> tableColumnsList = new ArrayList<>();
                ResultSet rsColumns = stmt.executeQuery("PRAGMA table_info(" + tableName + ")");

                while (rsColumns.next())
                    tableColumnsList.add(rsColumns.getString("name"));

                tableMap.put(tableName, tableColumnsList);
            }
            stmt.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error parsing DB structure");
        }
        return tableMap;
    }

    public void resetDB() {
        File musicdata = new File(store.getAppDataPath() + "musicdata.db");
        musicdata.delete();
        createDBandSourceTables(store.getDBpathString());
    }

}
