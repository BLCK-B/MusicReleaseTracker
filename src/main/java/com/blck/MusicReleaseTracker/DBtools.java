package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Simple.ErrorLogging;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

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

/** class for DB operations and error logs */
public class DBtools {

private final ValueStore store;

@Autowired
public DBtools(ValueStore valueStore) {
    this.store = valueStore;
}

public void logError(Exception e, String level, String message) {
    Logger logger = Logger.getLogger(ErrorLogging.class.getName());
    String errorLogs = store.getErrorLogs();
    try {
        // filehandler logging the error
        FileHandler fileHandler = new FileHandler(errorLogs, true);
        fileHandler.setFormatter(new SimpleFormatter());
        // clear log when it reaches approx 0.1 MB
        final long logFileSize = Files.size(Paths.get(errorLogs));
        if (logFileSize > 100000) {
            Files.write(Paths.get(errorLogs), new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
        }
        // log the error
        logger.addHandler(fileHandler);
        switch (level) {
            case ("SEVERE") -> logger.log(Level.SEVERE, message, e);
            case ("WARNING") -> logger.log(Level.WARNING, message, e);
            case ("INFO") -> logger.log(Level.INFO, message);
        }
        fileHandler.close();
    } catch (IOException ioException) {
        throw new RuntimeException(ioException);
    }
    if (level.equals("SEVERE")) {
        throw new RuntimeException(e);
    }
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
    // assemble paths for all appdata files
    File folder = new File(appData + File.separator + "MusicReleaseTracker");
    if (!folder.exists())
        folder.mkdirs();
    // junk folder because sqlite did not delete temp files in "temp"
    File tempfolder = new File(appData + File.separator + "MusicReleaseTracker" + File.separator + "temp");
    if (!tempfolder.exists())
        tempfolder.mkdirs();
    File[] tempfiles = tempfolder.listFiles();
    for (File file : tempfiles) {
        file.delete();
    }
    System.setProperty("org.sqlite.tmpdir", appData + File.separator + "MusicReleaseTracker" + File.separator + "temp");

    String basePath = appData + File.separator + "MusicReleaseTracker" + File.separator;
    String DBpath =             "jdbc:sqlite:" + basePath + "musicdata.db";
    String DBtemplatePath =     "jdbc:sqlite:" + basePath + "DBTemplate.db";
    String configPath =         basePath + "MRTsettings.hocon";
    String configFolder =       basePath + File.separator;
    String errorLogs =          basePath + "errorlogs.txt";
    // save paths to settingsStore
    store.setConfigFolder(configFolder);
    store.setConfigPath(configPath);
    store.setDBpath(DBpath);
    store.setDBTemplatePath(DBtemplatePath);
    store.setErrorLogs(errorLogs);
}

public void createTables() {
    // on start: create DB if not exist, check DB structure, if different -> create new from template and refill with all data possible
    File templateFile = new File(store.getDBTemplatePath().substring(12));
    templateFile.delete();
    createDB(store.getDBpath());
    createDB(store.getDBTemplatePath());

    // if different structure, fill template artist table data from musicdata and then rename/delete, make new template
    // this only preserves "artists" data and assumes that the insertion logic will be adjusted after any changes
    // made to the "artists" table: change in order of columns, adding/removing a column or changing a column's name
    Map<String, ArrayList<String>> DBMap = getDBStructure(store.getDBpath());
    Map<String, ArrayList<String>> DBtemplateMap = getDBStructure(store.getDBTemplatePath());
    if (!DBMap.equals(DBtemplateMap)) {
        try {
            Connection connDB = DriverManager.getConnection(store.getDBpath());
            Connection connDBtemplate = DriverManager.getConnection(store.getDBTemplatePath());

            // insert data from musicdata column to a template column
            String sql = "SELECT * FROM artists";
            Statement stmt = connDB.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            sql = "insert into artists(artistname, urlmusicbrainz, urlbeatport, urljunodownload, urlyoutube) values(?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connDBtemplate.prepareStatement(sql);
            ArrayList<String> columnList = DBMap.get("artists");
            // cycling table rows
            while (rs.next()) {
                // fill sql query row data and add to batch
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
            connDB.close();
            connDBtemplate.close();
        } catch(Exception e) {
            logError(e, "SEVERE", "error updating DB file");
        }
        try {
            File oldFile = new File(store.getDBpath().substring(12));
            File newFile = new File(store.getDBTemplatePath().substring(12));
            // delete old musicdata
            oldFile.delete();
            // rename template to musicdata
            newFile.renameTo(oldFile);
        } catch(Exception e) {
            logError(e, "SEVERE", "error renaming/deleting DB files");
        }
    }
}

private void createDB(String path) {
    try {
        Connection conn = DriverManager.getConnection(path);

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
        conn.close();
    } catch (SQLException e) {
       logError(e, "SEVERE", "error creating DB file");
    }
}

public void clearDB() {
    try {
        Connection conn = DriverManager.getConnection(store.getDBpath());
        for (String sourceTable : store.getSourceTables()) {
            String sql = "DELETE FROM " + sourceTable;
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        }
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
}

public Map<String, ArrayList<String>> getDBStructure(String path) {
    HashMap<String, ArrayList<String>> tableMap = new HashMap<String, ArrayList<String>>();
    try {
        Connection conn = DriverManager.getConnection(path);
        String sql = "SELECT name FROM sqlite_master WHERE type='table'";
        Statement stmt = conn.createStatement();
        ResultSet rsTables = stmt.executeQuery(sql);
        ArrayList<String> tablesList = new ArrayList<String>();
        while(rsTables.next())
            tablesList.add(rsTables.getString(1));

        for (String tableName : tablesList) {
            ArrayList<String> tableColumnsList = new ArrayList<String>();
            ResultSet rsColumns = stmt.executeQuery("PRAGMA table_info(" + tableName + ")");
            while (rsColumns.next())
                tableColumnsList.add(rsColumns.getString("name"));
            tableMap.put(tableName, tableColumnsList);
        }
        stmt.close();
        conn.close();
    } catch (SQLException e) {
        logError(e, "SEVERE", "error parsing DB structure");
    }
    return tableMap;
}

public void resetDB() {
   // default the musicdata
   File musicdata = new File(store.getDBpath().substring(12));
   musicdata.delete();
   createDB(store.getDBpath());
}
}



