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
import com.blck.MusicReleaseTracker.Core.TablesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DataObjects.Album;
import com.blck.MusicReleaseTracker.DataObjects.MediaItem;
import com.blck.MusicReleaseTracker.DataObjects.Song;
import com.blck.MusicReleaseTracker.JsonSettings.SettingsIO;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.*;
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

    @Autowired
    public DBqueries(ValueStore valueStore, ErrorLogging errorLogging, SettingsIO settingsIO, MigrateDB manageDB) {
        this.store = valueStore;
        this.log = errorLogging;
        this.manageDB = manageDB;
        this.settingsIO = settingsIO;
    }

    public List<String> getArtistList() {
        List<String> dataList = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(store.getDBpathString())) {
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

    public List<MediaItem> loadTable(TablesEnum source, String name) {
        List<MediaItem> tableContent = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(store.getDBpathString())) {
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT song, date FROM " + source + " WHERE artist = ? ORDER BY date DESC, song LIMIT 50");
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next())
                tableContent.add(new Song(
                        rs.getString("song"),
                        name,
                        rs.getString("date")));
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error loading table");
        }
        return tableContent;
    }

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

    public List<Song> readCombviewSingles() {
        List<Song> singles = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(store.getDBpathString())) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT song, artist, date FROM combview WHERE album IS NULL ORDER BY date DESC, song LIMIT 1000"
            );
            while (rs.next())
                singles.add(new Song(
                        rs.getString("song"),
                        rs.getString("artist"),
                        rs.getString("date")));
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error loading combview table");
        }
        return singles;
    }

    public List<Album> readCombviewAlbums() {
        List<Album> albums = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(store.getDBpathString())) {
            Statement stmt = conn.createStatement();
            ResultSet rs1 = stmt.executeQuery(
                    "SELECT DISTINCT album FROM combview WHERE album IS NOT NULL ORDER BY date LIMIT 300"
            );
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT song, artist, date FROM combview WHERE album = ? ORDER BY song LIMIT 100"
            );
            while (rs1.next()) {
                final String albumName =  rs1.getString("album");
                pstmt.setString(1, albumName);
                ResultSet rs2 = pstmt.executeQuery();
                ArrayList<Song> albumSongs = new ArrayList<>();
                while (rs2.next())
                    albumSongs.add(new Song(
                            rs2.getString("song"),
                            rs2.getString("artist"),
                            rs2.getString("date")));
                albums.add(new Album(albumName, albumSongs));
            }
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error loading combview albums");
        }
        return albums;
    }

    public void insertIntoArtistList(String name) {
        try (Connection conn = DriverManager.getConnection(store.getDBpathString())) {
            PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO artists (artist) values(?)");
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.INFO, "artist already exists");
        }
    }

    public void updateArtistSourceID(String name, TablesEnum source, String newID) {
        String sql;
        if (newID == null)
            sql = "UPDATE artists SET url" + source + " = NULL WHERE artist = ?";
        else
            sql = "UPDATE artists SET url" + source + " = ? WHERE artist = ?";
        try (Connection conn = DriverManager.getConnection(store.getDBpathString())) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            if (newID == null) {
                pstmt.setString(1, name);
            }
            else {
                pstmt.setString(1, newID);
                pstmt.setString(2, name);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.WARNING, "could not save URL");
        }
    }

    public Optional<String> getArtistSourceID(String name, TablesEnum source) {
        try (Connection conn = DriverManager.getConnection(store.getDBpathString())) {
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT url" + source + " FROM artists WHERE artist = ?");
            pstmt.setString(1, name);
            return Optional.ofNullable(pstmt.executeQuery().getString(1));
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.WARNING, "error checking url existence");
        }
        return Optional.empty();
    }

    public void clearArtistDataFrom(String name, TablesEnum table) {
        try (Connection conn = DriverManager.getConnection(store.getDBpathString())) {
            PreparedStatement pstmt = conn.prepareStatement(
                    "DELETE FROM " + table + " WHERE artist = ?");
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error deleting artists data in " + table);
        }
    }

    public void removeArtistFromAllTables(String name) {
        for (TablesEnum table : TablesEnum.values())
            clearArtistDataFrom(name, table);
        try (Connection conn = DriverManager.getConnection(store.getDBpathString())) {
            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM artists WHERE artist = ?");
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error deleting " + name + "in artists");
        }
    }

    public void truncateAllTables() {
        try (Connection conn = DriverManager.getConnection(store.getDBpathString())) {
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

    public void truncateCombview() {
        try (Connection conn = DriverManager.getConnection(store.getDBpathString())) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("DELETE FROM combview");
            stmt.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.WARNING, "error clearing DB");
        }
    }

    public ArrayList<Song> getSourceTablesDataForCombview() {
        var filterWords = settingsIO.getFilterValues();
        ArrayList<Song> songObjectList = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(store.getDBpathString())) {
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
                    } catch (Exception ignored){} // check column count?

                    if (songPassesFilterCheck(new Song(songName, songArtist, songDate, songType), filterWords))
                        songObjectList.add(new Song(songName, songArtist, songDate, songType));
                }
            }
        } catch (Exception e) {
            log.error(e, ErrorLogging.Severity.WARNING, "error filtering keywords");
        }
        return songObjectList;
    }

    public boolean songPassesFilterCheck(Song song, HashMap<String, String> filterWords) {
        Set<String> disabledWords = filterWords.entrySet().stream()
                .filter(entry -> Boolean.parseBoolean(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        if (song.getType().isEmpty())
            return disabledWords.stream()
                    .map(this::getRealFilterName)
                    .noneMatch(disabledWord -> song.getName().toLowerCase().contains(disabledWord.toLowerCase()));
        else
            return disabledWords.stream()
                    .map(this::getRealFilterName)
                    .noneMatch(disabledWord -> song.getType().get().toLowerCase().contains(disabledWord.toLowerCase()) ||
                            song.getName().toLowerCase().contains(disabledWord.toLowerCase()));
    }

    private String getRealFilterName(String settingName) {
        return settingName.replace("filter", "").trim();
    }

    public LinkedList<Scraper> getAllScrapers() {
        // creating a list of scraper objects: one scraper holds one URL
        LinkedList<Scraper> scrapers = new LinkedList<>();
        try (Connection conn = DriverManager.getConnection(store.getDBpathString())) {
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
                        case junodownload -> scrapers.add(new ScraperJunodownload(store, log, this, artist, url));
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

    public void batchInsertSongs(List<Song> songList, TablesEnum source, int limit) {
        if (source == null)
            throw new NullPointerException("null table");
        if (source == TablesEnum.combview)
            throw new RuntimeException("use dedicated combview insert method");
        try (Connection conn = DriverManager.getConnection(store.getDBpathString())) {
            String sql;
            if (songList.getFirst().getType().isPresent())
                sql = "insert into " + source + "(song, artist, date, type) values(?, ?, ?, ?)";
            else
                sql = "insert into " + source + "(song, artist, date) values(?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            int i = 0;
            for (Song songObject : songList) {
                if (i == limit)
                    break;
                pstmt.setString(1, songObject.getName());
                pstmt.setString(2, songObject.getArtists());
                pstmt.setString(3, songObject.getDate());
                if (songList.getFirst().getType().isPresent())
                    pstmt.setString(4, songObject.getType().get());
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

    public void batchInsertCombview(List<Song> songList) {
        try (Connection conn = DriverManager.getConnection(store.getDBpathString())) {
            PreparedStatement pstmt = conn.prepareStatement(
                    "insert into combview (song, artist, date, album) values(?, ?, ?, ?)"
            );
            int i = 0;
            for (Song songObject : songList) {
                if (i == 115)
                    break;
                pstmt.setString(1, songObject.getName());
                pstmt.setString(2, songObject.getArtists());
                pstmt.setString(3, songObject.getDate());
                pstmt.setString(4, songObject.getAlbum());
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

    public void vacuum() {
        try (Connection conn = DriverManager.getConnection(store.getDBpathString())) {
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
                new Song("For security, russian is disallowed.", "", "01-01-2000"),
                new Song("This can be disabled by changing system language.", "", "01-02-2000"));
        }
        return null;
    }
}



