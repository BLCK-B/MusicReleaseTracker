package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

public class ManageMigrateDB {

    private final ValueStore store;
    private final ErrorLogging log;

    @Autowired
    public ManageMigrateDB(ValueStore valueStore, ErrorLogging errorLogging) {
        this.store = valueStore;
        this.log = errorLogging;
    }

    public void createDBandSourceTables(String path) {
        // note: generate by string templates after preview
        try (Connection conn = DriverManager.getConnection(path)) {
            String sql = """
                    CREATE TABLE IF NOT EXISTS musicbrainz (
                    song text NOT NULL,
                    artist text NOT NULL,
                    date text NOT NULL
                    );
                    """;
            Statement stmt = conn.createStatement();
            stmt.execute(sql);

            sql = """
                    CREATE TABLE IF NOT EXISTS beatport (
                    song text NOT NULL,
                    artist text NOT NULL,
                    date text NOT NULL,
                    type text NOT NULL
                    );
                    """;
            stmt = conn.createStatement();
            stmt.execute(sql);

            sql = """
                    CREATE TABLE IF NOT EXISTS junodownload (
                    song text NOT NULL,
                    artist text NOT NULL,
                    date text NOT NULL
                    );
                    """;
            stmt = conn.createStatement();
            stmt.execute(sql);

            sql = """
                    CREATE TABLE IF NOT EXISTS youtube (
                    song text NOT NULL,
                    artist text NOT NULL,
                    date text NOT NULL
                    );
                    """;
            stmt = conn.createStatement();
            stmt.execute(sql);

            sql = """
                    CREATE TABLE IF NOT EXISTS artists (
                    artist text PRIMARY KEY,
                    urlmusicbrainz text,
                    urlbeatport text,
                    urljunodownload text,
                    urlyoutube text
                    );
                    """;
            stmt = conn.createStatement();
            stmt.execute(sql);

            sql = """
                    CREATE TABLE IF NOT EXISTS combview (
                    song text NOT NULL,
                    artist text NOT NULL,
                    date text NOT NULL
                    );
                    """;
            stmt = conn.createStatement();
            stmt.execute(sql);

            stmt.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error creating DB file");
        }
    }

    public void resetDB() {
        // default the musicdata
        File musicdata = new File(store.getAppDataPath() + "musicdata.db");
        musicdata.delete();
        createDBandSourceTables(store.getDBpath());
    }

    public void migrateDB() {
        // on start: create DB if not exist, check DB structure, if different -> create new from template and refill with all data possible
        final String templateFilePath = store.getAppDataPath() + "DBTemplate.db";
        final String DBtemplatePath = "jdbc:sqlite:" + templateFilePath;
        final String DBfilePath = store.getAppDataPath() + "musicdata.db";

        File templateFile = new File(templateFilePath);
        templateFile.delete();
        createDBandSourceTables(store.getDBpath());
        createDBandSourceTables(DBtemplatePath);

        // if different structure, fill template artist table data from musicdata and then rename/delete, make new template
        // this only preserves "artists" data and assumes that the insertion logic will be adjusted after any changes
        // made to the "artists" table: change in order of columns, adding/removing a column or changing a column's name
        Map<String, ArrayList<String>> DBMap = getDBStructure(store.getDBpath());
        Map<String, ArrayList<String>> DBtemplateMap = getDBStructure(DBtemplatePath);
        if (!DBMap.equals(DBtemplateMap)) {
            try (
                    Connection connDB = DriverManager.getConnection(store.getDBpath());
                    Connection connDBtemplate = DriverManager.getConnection(DBtemplatePath)
            ) {
                // insert data from musicdata column to template column
                String sql = "SELECT * FROM artists LIMIT 1000";
                Statement stmt = connDB.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                sql = "insert into artists(artist, urlmusicbrainz, urlbeatport, urljunodownload, urlyoutube) values(?, ?, ?, ?, ?)";
                PreparedStatement pstmt = connDBtemplate.prepareStatement(sql);
                ArrayList<String> columnList = DBMap.get("artists");
                // cycling table rows
                while (rs.next()) {
                    // construct sql query for every column, add to batch
                    for (int i = 0; i < columnList.size(); i++) {
                        String column = columnList.get(i);
                        pstmt.setString(i + 1, rs.getString(column));
                    }
                    pstmt.addBatch();
                }
                connDBtemplate.setAutoCommit(false);
                pstmt.executeBatch();
                connDBtemplate.commit();
                connDBtemplate.setAutoCommit(true);
                pstmt.clearBatch();
                pstmt.close();
            } catch (Exception e) {
                log.error(e, ErrorLogging.Severity.SEVERE, "error updating DB file");
            }
            try {
                File oldFile = new File(DBfilePath);
                File newFile = new File(templateFilePath);
                // delete old musicdata
                oldFile.delete();
                // rename template to musicdata
                newFile.renameTo(oldFile);
            } catch (Exception e) {
                log.error(e, ErrorLogging.Severity.SEVERE, "error renaming/deleting DB files");
            }
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

}
