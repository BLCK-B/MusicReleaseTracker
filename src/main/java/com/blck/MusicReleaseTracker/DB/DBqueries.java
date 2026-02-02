package com.blck.MusicReleaseTracker.DB;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.TablesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DataObjects.Album;
import com.blck.MusicReleaseTracker.DataObjects.MediaItem;
import com.blck.MusicReleaseTracker.DataObjects.Song;
import com.blck.MusicReleaseTracker.JsonSettings.SettingsIO;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.Scraper;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.ScraperBeatport;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.ScraperMusicbrainz;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.ScraperYoutube;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DBqueries {

    private final ValueStore store;

    private final ErrorLogging log;

    private final MigrateDB manageDB;

    private final SettingsIO settingsIO;

    private final int combviewSize = 100;

    @Autowired
    public DBqueries(ValueStore valueStore, ErrorLogging errorLogging, SettingsIO settingsIO, MigrateDB manageDB) {
        this.store = valueStore;
        this.log = errorLogging;
        this.manageDB = manageDB;
        this.settingsIO = settingsIO;
    }

    /**
     *
     * @return list of artist names
     */
    public List<String> getArtistList() {
        List<String> dataList = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + store.getDBpath())) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT artist FROM artists ORDER BY artist LIMIT 500");
            while (rs.next())
                dataList.add(rs.getString("artist"));
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error loading list");
        }
        return dataList;
    }

    /**
     *
     * @param source web source
     * @param name   artist name
     * @return list of {@code MediaItem}, in case of source table only songs are returned
     * @see MediaItem
     */
    public List<MediaItem> loadTable(TablesEnum source, String name) {
        List<MediaItem> tableContent = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + store.getDBpath())) {
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT song, date FROM " + source + " WHERE artist = ? ORDER BY date DESC, song LIMIT 50");
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableContent.add(new Song(
                        rs.getString("song"),
                        name,
                        rs.getString("date"),
                        null,
                        null)
                );
            }
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error loading table");
        }
        return tableContent;
    }

    /**
     *
     * @return songs and albums as {@code MediaItem}
     * @see MediaItem
     */
    public List<MediaItem> loadCombviewTable() {
        if (disableR() != null) return disableR();
        ArrayList<MediaItem> tableContent = new ArrayList<>();
        tableContent.addAll(readCombviewAlbums());
        tableContent.addAll(readCombviewSingles());
        return tableContent.stream()
                .sorted(Comparator.comparing(MediaItem::getDate)
                        .thenComparing(MediaItem::getName, Comparator.reverseOrder()))
                .toList().reversed();
    }

    /**
     *
     * @return {@code Song} list from combview table
     */
    public List<Song> readCombviewSingles() {
        List<Song> singles = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + store.getDBpath())) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT song, artist, date, thumbnail FROM combview WHERE album IS NULL ORDER BY date DESC, song LIMIT 1000"
            );
            while (rs.next()) {
                singles.add(new Song(
                        rs.getString("song"),
                        rs.getString("artist"),
                        rs.getString("date"),
                        null,
                        rs.getString("thumbnail"))
                );
            }
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error loading combview table");
        }
        return singles;
    }

    /**
     *
     * @return {@code Album} list from combview table
     */
    public List<Album> readCombviewAlbums() {
        List<Album> albums = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + store.getDBpath())) {
            Statement stmt = conn.createStatement();
            ResultSet rs1 = stmt.executeQuery(
                    "SELECT DISTINCT album FROM combview WHERE album IS NOT NULL ORDER BY date LIMIT 300"
            );
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT song, artist, date, thumbnail FROM combview WHERE album = ? ORDER BY song LIMIT 100"
            );
            while (rs1.next()) {
                final String albumName = rs1.getString("album");
                pstmt.setString(1, albumName);
                ResultSet rs2 = pstmt.executeQuery();
                ArrayList<Song> albumSongs = new ArrayList<>();
                while (rs2.next())
                    albumSongs.add(new Song(
                            rs2.getString("song"),
                            rs2.getString("artist"),
                            rs2.getString("date"),
                            null,
                            rs2.getString("thumbnail"))
                    );
                albums.add(new Album(albumName, albumSongs));
            }
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error loading combview albums");
        }
        return albums;
    }

    /**
     *
     * @param name artist name
     */
    public void insertIntoArtistList(String name) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + store.getDBpath())) {
            PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO artists (artist) values(?)");
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.INFO, "artist already exists");
        }
    }

    /**
     * Sets an artist's ID for a specific source in {@code artists} table.
     *
     * @param name   artist name
     * @param source web source
     * @param newID  new ID for building the URL
     */
    public void updateArtistSourceID(String name, TablesEnum source, String newID) {
        String sql;
        if (newID == null)
            sql = "UPDATE artists SET url" + source + " = NULL WHERE artist = ?";
        else
            sql = "UPDATE artists SET url" + source + " = ? WHERE artist = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + store.getDBpath())) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            if (newID == null) {
                pstmt.setString(1, name);
            } else {
                pstmt.setString(1, newID);
                pstmt.setString(2, name);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.WARNING, "could not save URL");
        }
    }

    /**
     *
     * @param name   artist name
     * @param source web source
     * @return artist source ID
     */
    public Optional<String> getArtistSourceID(String name, TablesEnum source) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + store.getDBpath())) {
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT url" + source + " FROM artists WHERE artist = ?");
            pstmt.setString(1, name);
            return Optional.ofNullable(pstmt.executeQuery().getString(1));
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.WARNING, "error checking url existence");
        }
        return Optional.empty();
    }

    /**
     * Clears artist-related entries from a source table (best effort: appended artists). </br>
     *
     * @param name  artist name
     * @param table any table
     */
    public void clearArtistDataFrom(String name, TablesEnum table) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + store.getDBpath())) {
            PreparedStatement pstmt = conn.prepareStatement(
                    "DELETE FROM " + table + " WHERE artist = ?");
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error deleting artists data in " + table);
        }
    }

    /**
     *
     * @param name artist name
     */
    public void removeArtistFromAllTables(String name) {
        for (TablesEnum table : TablesEnum.values())
            clearArtistDataFrom(name, table);
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + store.getDBpath())) {
            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM artists WHERE artist = ?");
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error deleting " + name + "in artists");
        }
    }

    /**
     * Truncate all source tables, including {@code combview}. <br/>
     * {@code artists} table is unaffected.
     */
    public void truncateAllTables() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + store.getDBpath())) {
            Statement stmt = conn.createStatement();
            for (TablesEnum table : TablesEnum.values())
                stmt.addBatch("DELETE FROM " + table);
            conn.setAutoCommit(false);
            stmt.executeBatch();
            conn.setAutoCommit(true);
            stmt.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.WARNING, "error clearing DB");
        }
    }

    /**
     * Truncate combview only.
     */
    public void truncateCombview() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + store.getDBpath())) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("DELETE FROM combview");
            stmt.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.WARNING, "error clearing DB");
        }
    }

    /**
     *
     * @return list of {@code Song} since this gathers data only from source tables
     */
    public ArrayList<Song> getSourceTablesDataForCombview() {
        var filterWords = settingsIO.getFilterValues();
        ArrayList<Song> songObjectList = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + store.getDBpath())) {
            for (TablesEnum table : TablesEnum.values()) {
                if (table == TablesEnum.combview)
                    continue;
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM " + table + " ORDER BY date DESC LIMIT 200");
                while (rs.next()) {
                    String songName = rs.getString("song");
                    String songArtist = rs.getString("artist");
                    String songDate = rs.getString("date");
                    String songType = null;
                    try {
                        songType = rs.getString("type");
                    } catch (Exception ignored) {
                    } // check column count?
                    String songThumbnail = rs.getString("thumbnail");

                    if (songPassesFilterCheck(new Song(songName, songArtist, songDate, songType, songThumbnail), filterWords))
                        songObjectList.add(new Song(songName, songArtist, songDate, songType, songThumbnail));
                }
            }
        } catch (Exception e) {
            log.error(e, ErrorLogging.Severity.WARNING, "error filtering keywords");
        }
        return songObjectList;
    }

    /**
     * Checks if the song name of type contains anyn of the enabled filters.
     *
     * @param song        object
     * @param filterWords may contain both true and false filters
     * @return true if no matches found
     */
    public boolean songPassesFilterCheck(Song song, HashMap<String, String> filterWords) {
        Set<String> disabledWords = filterWords.entrySet().stream()
                .filter(entry -> Boolean.parseBoolean(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        if (song.getType() == null) {
            return disabledWords.stream()
                    .map(this::getRealFilterName)
                    .noneMatch(disabledWord -> song.getName().toLowerCase().contains(disabledWord.toLowerCase()));
        } else {
            return disabledWords.stream()
                    .map(this::getRealFilterName)
                    .noneMatch(disabledWord -> song.getType().toLowerCase().contains(disabledWord.toLowerCase()) ||
                            song.getName().toLowerCase().contains(disabledWord.toLowerCase()));
        }
    }

    private String getRealFilterName(String settingName) {
        return settingName.replace("filter", "").trim();
    }

    /**
     * Creates a list of scraper objects from {@code artists} table where one scraper holds one UR.
     *
     * @return list of scraper objects
     * @see Scraper
     */
    public LinkedList<Scraper> getAllScrapers() {
        LinkedList<Scraper> scrapers = new LinkedList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + store.getDBpath())) {
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT artist FROM artists LIMIT 500");
            ResultSet artistResults = pstmt.executeQuery();
            // cycling artists
            while (artistResults.next()) {
                String artist = artistResults.getString("artist");
                // cycling sources
                for (TablesEnum webSource : TablesEnum.values()) {
                    if (webSource == TablesEnum.combview)
                        continue;
                    pstmt = conn.prepareStatement(
                            "SELECT * FROM artists WHERE artist = ? LIMIT 100");
                    pstmt.setString(1, artist);
                    ResultSet rs = pstmt.executeQuery();
                    String url = rs.getString("url" + webSource);
                    if (url == null)
                        continue;
                    switch (webSource) {
                        case musicbrainz -> scrapers.add(new ScraperMusicbrainz(store, log, this, artist, url));
                        case beatport -> scrapers.add(new ScraperBeatport(store, log, this, artist, url));
                        case youtube -> scrapers.add(new ScraperYoutube(store, log, this, artist, url));
                    }
                }
            }
            pstmt.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error creating scrapers list");
        }
        return scrapers;
    }

    /**
     * Batch inserts songs to a source table from a list depending on the song dates - prefers newer.
     *
     * @param songList songs
     * @param source   source table
     * @param limit    max number of songs from {@code songList}
     */
    public void batchInsertSongs(List<Song> songList, TablesEnum source, int limit) {
        if (source == null)
            throw new NullPointerException("null table");
        if (source == TablesEnum.combview)
            throw new RuntimeException("use dedicated combview insert method");
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + store.getDBpath())) {
            String sql;
            if (songList.getFirst().getType() != null) {
                sql = "insert into " + source + "(song, artist, date, type, thumbnail) values(?, ?, ?, ?, ?)";
            } else {
                sql = "insert into " + source + "(song, artist, date, thumbnail) values(?, ?, ?, ?)";
            }
            PreparedStatement pstmt = conn.prepareStatement(sql);
            int i = 0;
            for (Song songObject : songList) {
                if (i == limit) break;

                pstmt.setString(1, songObject.getName());
                pstmt.setString(2, songObject.getArtists());
                pstmt.setString(3, songObject.getDate());

                if (songList.getFirst().getType() != null) {
                    pstmt.setString(4, songObject.getType());
                    pstmt.setString(5, songObject.getThumbnailUrl());
                } else {
                    pstmt.setString(4, songObject.getThumbnailUrl());
                }
                ++i;
                pstmt.addBatch();
            }
            conn.setAutoCommit(false);
            pstmt.executeBatch();
            conn.setAutoCommit(true);
            pstmt.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error inserting a batch of songs");
        }
    }

    /**
     * Batch inserts songs to combview table from a list depending on the song dates - prefers newer.
     *
     * @param songList songs
     */
    public void batchInsertCombview(List<Song> songList) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + store.getDBpath())) {
            PreparedStatement pstmt = conn.prepareStatement(
                    "insert into combview (song, artist, date, album, thumbnail) values(?, ?, ?, ?, ?)"
            );
            int i = 0;
            for (Song songObject : songList) {
                if (i == combviewSize)
                    break;
                String thumbnailUrl = songObject.getThumbnailUrl();
                pstmt.setString(1, songObject.getName());
                pstmt.setString(2, songObject.getArtists());
                pstmt.setString(3, songObject.getDate());
                pstmt.setString(4, songObject.getAlbum());
                pstmt.setString(5, thumbnailUrl);
                ++i;
                pstmt.addBatch();
            }
            conn.setAutoCommit(false);
            pstmt.executeBatch();
            conn.setAutoCommit(true);
            pstmt.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error inserting a batch of songs");
        }
    }

    /**
     * SQLite DB command for optimisation.
     */
    public void vacuum() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + store.getDBpath())) {
            PreparedStatement pstmt = conn.prepareStatement("VACUUM;");
            pstmt.execute();
            pstmt.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.WARNING, "vacuum error");
        }
    }

    private List<MediaItem> disableR() {
        if (Locale.getDefault().getLanguage().equals("ru")) {
            return List.of(
                    new Song("For security, russian is disallowed.", "", "01-01-2000", null, null)
            );
        }
        return null;
    }
}



