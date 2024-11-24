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
import java.nio.file.Files;
import java.nio.file.Path;
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

    public void createDBandTables(Path path) {
        // note: generate by string templates after preview
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + path)) {
            Statement stmt = conn.createStatement();

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

            conn.setAutoCommit(false);
            stmt.executeBatch();
            conn.setAutoCommit(true);
            stmt.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error creating DB file");
        }
    }

    public void migrateDB(Path DB, Path DBtemplate) {
        try {
            Files.deleteIfExists(DBtemplate);
            createDBandTables(DB);
            createDBandTables(DBtemplate);

            var DBstructure = getDBStructure(DB);
            if (!DBstructure.equals(getDBStructure(DBtemplate))) {
                copyArtistsData(DB, DBtemplate);
                Files.delete(DB);
                new File(String.valueOf(DBtemplate))
                        .renameTo(DB.toFile());
            }
        } catch (Exception e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "failed to migrate DB");
        }
    }

    public void copyArtistsData(Path sourceDB, Path targetDB) {
        var DBcolumns = getDBStructure(sourceDB).get("artists");
        var templateColumns = getDBStructure(targetDB).get("artists");
        List<String> sharedColumns = new ArrayList<>(DBcolumns);
        sharedColumns.retainAll(templateColumns);
        String shared = String.join(", ", sharedColumns);

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + sourceDB)) {
            Statement stmt = conn.createStatement();
            stmt.execute("ATTACH DATABASE '" + targetDB + "' AS target_db");
            stmt.executeUpdate("INSERT INTO target_db.artists (" + shared + ") SELECT " + shared + " FROM artists");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, ArrayList<String>> getDBStructure(Path path) {
        if (Files.notExists(path))
            throw new RuntimeException("file " + path +  " does not exist");

        HashMap<String, ArrayList<String>> tableMap = new HashMap<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + path)) {
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
        createDBandTables(store.getDBpath());
    }

}
