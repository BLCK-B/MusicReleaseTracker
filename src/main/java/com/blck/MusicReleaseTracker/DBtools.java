package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Core.SourcesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.Core.ErrorLogging;
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

/** class for DB operations */
public class DBtools {

    private final ValueStore store;
    private final ErrorLogging log;
    final String slash = File.separator;

    @Autowired
    public DBtools(ValueStore valueStore, ErrorLogging errorLogging) {
        this.store = valueStore;
        this.log = errorLogging;
    }

    public void path() {
        String appData = null;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) // Windows
            appData = System.getenv("APPDATA");
        else if (os.contains("nix") || os.contains("nux") || os.contains("mac"))  // Linux
            appData = System.getProperty("user.home");
        else
            throw new UnsupportedOperationException("unsupported OS");

        // paths
        String appDataPath = appData + slash + "MusicReleaseTracker" + slash;
        String DBpath =             "jdbc:sqlite:" + appDataPath + "musicdata.db";
        String configPath =         appDataPath + "MRTsettings.hocon";
        String errorLogsPath =          appDataPath + "errorlogs.txt";
        // save to settingsStore
        store.setAppDataPath(appDataPath);
        store.setConfigPath(configPath);
        store.setDBpath(DBpath);
        store.setErrorLogsPath(errorLogsPath);

        // appdata folder
        File folder = new File(appDataPath);
        if (!folder.exists())
            folder.mkdirs();
        // junk folder because sqlite did not delete temp files in "temp"
        File tempfolder = new File(appDataPath + "temp");
        if (!tempfolder.exists())
            tempfolder.mkdirs();
        File[] tempfiles = tempfolder.listFiles();
        for (File file : tempfiles) {
            file.delete();
        }
        System.setProperty("org.sqlite.tmpdir", appDataPath + "temp");
    }

    public void createTables() {
        // on start: create DB if not exist, check DB structure, if different -> create new from template and refill with all data possible
        final String templateFilePath = store.getAppDataPath() + "DBTemplate.db";
        final String DBtemplatePath = "jdbc:sqlite:" + templateFilePath;
        final String DBfilePath = store.getAppDataPath() + "musicdata.db";

        File templateFile = new File(templateFilePath);
        templateFile.delete();
        createDB(store.getDBpath());
        createDB(DBtemplatePath);

        // if different structure, fill template artist table data from musicdata and then rename/delete, make new template
        // this only preserves "artists" data and assumes that the insertion logic will be adjusted after any changes
        // made to the "artists" table: change in order of columns, adding/removing a column or changing a column's name
        Map<String, ArrayList<String>> DBMap = getDBStructure(store.getDBpath());
        Map<String, ArrayList<String>> DBtemplateMap = getDBStructure(DBtemplatePath);
        if (!DBMap.equals(DBtemplateMap)) {
            try (
                    Connection connDB = DriverManager.getConnection(store.getDBpath());
                    Connection connDBtemplate = DriverManager.getConnection(DBtemplatePath)
                )
            {
                // insert data from musicdata's column to template's column
                String sql = "SELECT * FROM artists LIMIT 1000";
                Statement stmt = connDB.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                sql = "insert into artists(artistname, urlmusicbrainz, urlbeatport, urljunodownload, urlyoutube) values(?, ?, ?, ?, ?)";
                PreparedStatement pstmt = connDBtemplate.prepareStatement(sql);
                ArrayList<String> columnList = DBMap.get("artists");
                // cycling table rows
                while (rs.next()) {
                    // construct sql query for every column, add to batch
                    for (int i = 0; i < columnList.size(); i++) {
                        String column = columnList.get(i);
                        pstmt.setString(i + 1 , rs.getString(column));
                    }
                    pstmt.addBatch();
                }
                connDBtemplate.setAutoCommit(false);
                pstmt.executeBatch();
                connDBtemplate.commit();
                connDBtemplate.setAutoCommit(true);
                pstmt.clearBatch();
                pstmt.close();
            } catch(Exception e) {
                log.error(e, ErrorLogging.Severity.SEVERE, "error updating DB file");
            }
            try {
                File oldFile = new File(DBfilePath);
                File newFile = new File(templateFilePath);
                // delete old musicdata
                oldFile.delete();
                // rename template to musicdata
                newFile.renameTo(oldFile);
            } catch(Exception e) {
                log.error(e, ErrorLogging.Severity.SEVERE, "error renaming/deleting DB files");
            }
        }
    }

    private void createDB(String path) {
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
                artistname text PRIMARY KEY,
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

    public void clearDB() {
        // clear source tables and combview
        try (Connection conn = DriverManager.getConnection(store.getDBpath())) {
            for (SourcesEnum sourceTable : SourcesEnum.values()) {
                String sql = "DELETE FROM " + sourceTable;
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(sql);
            }
            String sql = "DELETE FROM combview";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.WARNING, "error clearing DB");
        }
    }

    public Map<String, ArrayList<String>> getDBStructure(String path) {
        // assembles a structure of tables and their columns
        HashMap<String, ArrayList<String>> tableMap = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(path)) {
            String sql = "SELECT name FROM sqlite_master WHERE type='table'";
            Statement stmt = conn.createStatement();
            ResultSet rsTables = stmt.executeQuery(sql);
            ArrayList<String> tablesList = new ArrayList<>();

            while(rsTables.next())
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
       // default the musicdata
       File musicdata = new File(store.getAppDataPath() + "musicdata.db");
       musicdata.delete();
       createDB(store.getDBpath());
    }
}



