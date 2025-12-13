
package com.blck.MusicReleaseTracker.DB;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MigrateDB {

    private final ValueStore store;
    private final ErrorLogging log;

    @Autowired
    public MigrateDB(ValueStore valueStore, ErrorLogging errorLogging) {
        this.store = valueStore;
        this.log = errorLogging;
    }

    /**
     * Creates a DB file (if doesnt exist) with all tables.
     *
     * @param path DB file path
     */
    public void createDBandTables(Path path) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + path)) {
            Statement stmt = conn.createStatement();

            stmt.addBatch("""
                    CREATE TABLE IF NOT EXISTS artists (
                    artist text PRIMARY KEY,
                    urlmusicbrainz text,
                    urlbeatport text,
                    urlyoutube text
                    );
                    """);

            stmt.addBatch("""
                    CREATE TABLE IF NOT EXISTS combview (
                    song text NOT NULL,
                    artist text NOT NULL,
                    date text NOT NULL,
                    album text,
                    thumbnail text
                    );
                    """);

            stmt.addBatch("""
                    CREATE TABLE IF NOT EXISTS musicbrainz (
                    song text NOT NULL,
                    artist text NOT NULL,
                    date text NOT NULL,
                    thumbnail text
                    );
                    """);

            stmt.addBatch("""
                    CREATE TABLE IF NOT EXISTS beatport (
                    song text NOT NULL,
                    artist text NOT NULL,
                    date text NOT NULL,
                    type text NOT NULL,
                    thumbnail text
                    );
                    """);

            stmt.addBatch("""
                    CREATE TABLE IF NOT EXISTS youtube (
                    song text NOT NULL,
                    artist text NOT NULL,
                    date text NOT NULL,
                    thumbnail text
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

    /**
     * Create a new DB file to replace {@code DB} if there is a structure difference. <br/>
     * Copies over some data: see {@code copyArtistsData}.
     *
     * @param DB         current DB path
     * @param DBtemplate temporary template DB path
     */
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

    /**
     * Retains only {@code artists} data. Source tables are cleared. <br/>
     * New columns are empty while dropped columns' data is lost.
     *
     * @param sourceDB previous DB with data
     * @param targetDB new DB where data is transferred
     */
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

    /**
     *
     * @param path DB file path
     * @return map with table names as key and their columns list as value
     */
    public Map<String, ArrayList<String>> getDBStructure(Path path) {
        if (Files.notExists(path))
            throw new RuntimeException("file " + path + " does not exist");

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

    /**
     * Deletes DB file and creates an empty file.
     */
    public void resetDB() {
        File musicdata = new File(store.getAppDataPath() + "musicdata.db");
        musicdata.delete();
        createDBandTables(store.getDBpath());
    }

}
