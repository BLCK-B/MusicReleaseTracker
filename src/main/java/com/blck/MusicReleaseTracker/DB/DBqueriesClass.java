package com.blck.MusicReleaseTracker.DB;

import com.blck.MusicReleaseTracker.ConfigTools;
import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.SourcesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DataObjects.Song;
import com.blck.MusicReleaseTracker.DataObjects.TableModel;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.sqlite.core.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

public class DBqueriesClass implements DBqueries {

    private final String DBpath;

    private final ValueStore store;
    private final ErrorLogging log;
    private final ManageMigrateDB manageDB;
    private final ConfigTools config;

    @Autowired
    public DBqueriesClass(ValueStore valueStore, ErrorLogging errorLogging, ConfigTools configTools, ManageMigrateDB manageDB) {
        this.store = valueStore;
        this.log = errorLogging;
        this.manageDB = manageDB;
        this.config = configTools;
        DBpath = store.getDBpath();
    }

    @Override
    public List<String> getArtistList() {
        List<String> dataList = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DBpath)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT artist FROM artists ORDER BY artist LIMIT 500");
            while (rs.next()) {
                dataList.add(rs.getString("artist"));
            }
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error loading list");
        }
        return dataList;
    }

    @Override
    public List<TableModel> loadTable(SourcesEnum source, String name) {
        // adding data to tableContent
        List<TableModel> tableContent = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DBpath)) {
            String sql = "SELECT song, date FROM " + source + " WHERE artist = ? ORDER BY date DESC LIMIT 100";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String songsCol = rs.getString("song");
                String datesCol = rs.getString("date");
                tableContent.add(new TableModel(songsCol, null, datesCol));
            }
            pstmt.close();
            rs.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error loading table");
        }
        return tableContent;
    }

    @Override
    public List<TableModel> loadCombviewTable() {
        List<TableModel> tableContent = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DBpath)) {
            String sql = "SELECT song, artist, date FROM combview ORDER BY date DESC, artist, song LIMIT 1000";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String songsCol = rs.getString("song");
                String artistsCol = rs.getString("artist");
                String datesCol = rs.getString("date");
                tableContent.add(new TableModel(songsCol, artistsCol, datesCol));
            }
            pstmt.close();
            rs.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error loading combview table");
        }
        return tableContent;
    }

    @Override
    public void insertIntoArtistList(String name) {
        try (Connection conn = DriverManager.getConnection(DBpath)) {
            String sql = "INSERT INTO artists (artist) values(?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("artist already exists");
        }
    }

    @Override
    public void updateArtistSourceID(String name, SourcesEnum source, String newID) {
        String sql;
        if (newID == null)
            sql = "UPDATE artists SET url" + source + " = NULL WHERE artist = ?";
        else
            sql = "UPDATE artists SET url" + source + " = ? WHERE artist = ?";
        try (Connection conn = DriverManager.getConnection(DBpath)) {
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

    @Override
    public String getArtistSourceID(String name, SourcesEnum source) {
        String ID = null;
        String sql = "SELECT url" + source + " FROM artists WHERE artist = ?";
        try (Connection conn = DriverManager.getConnection(DBpath)) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            ID = pstmt.executeQuery().getString(1);
            pstmt.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.WARNING, "error checking url existence");
        }
        return ID;
    }

    @Override
    public void clearArtistDataFrom(String name, String table) {
        try (Connection conn = DriverManager.getConnection(DBpath)) {
            String sql = "DELETE FROM " + table + " WHERE artist = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error deleting artists data in " + table);
        }
    }

    @Override
    public void removeArtist(String name) {
        for (String tableName : manageDB.getDBStructure(DBpath).keySet())
            clearArtistDataFrom(name, tableName);
    }

    @Override
    public void truncateScrapeData(boolean all) {
        try (Connection conn = DriverManager.getConnection(DBpath)) {
            if (all) {
                for (SourcesEnum sourceTable : SourcesEnum.values()) {
                    String sql = "DELETE FROM " + sourceTable;
                    Statement stmt = conn.createStatement();
                    stmt.executeUpdate(sql);
                }
            }
            String sql = "DELETE FROM combview";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.WARNING, "error clearing DB");
        }
    }

    @Override
    public ArrayList<Song> getAllSourceTableData() {
        config.readConfig(ConfigTools.configOptions.filters);
        ArrayList<Song> songObjectList = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DBpath)) {
            for (SourcesEnum source : SourcesEnum.values()) {
                String sql = "SELECT * FROM " + source + " ORDER BY date DESC LIMIT 200";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    String songName = rs.getString("song");
                    String songArtist = rs.getString("artist");
                    String songDate = rs.getString("date");
                    String songType = null;
                    if (source == SourcesEnum.beatport)
                        songType = rs.getString("type");

                    if (filterWords(songName, songType)) {
                        switch (source) {
                            case beatport ->
                                    songObjectList.add(new Song(songName, songArtist, songDate, songType));
                            case musicbrainz, junodownload, youtube ->
                                    songObjectList.add(new Song(songName, songArtist, songDate));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e, ErrorLogging.Severity.WARNING, "error filtering keywords");
        }
        return songObjectList;
    }

    @Override
    public LinkedList<Scraper> getAllScrapers() {
        // creating a list of scraper objects: one scraper holds one URL
        LinkedList<Scraper> scrapers = new LinkedList<>();
        try (Connection conn = DriverManager.getConnection(DBpath)) {
            String sql = "SELECT artist FROM artists LIMIT 500";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet artistResults = pstmt.executeQuery();
            // cycling artists
            while (artistResults.next()) {
                String artist = artistResults.getString("artist");
                // cycling sources
                for (SourcesEnum webSource : SourcesEnum.values()) {
                    sql = "SELECT * FROM artists WHERE artist = ? LIMIT 100";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, artist);
                    ResultSet rs = pstmt.executeQuery();
                    String url = rs.getString("url" + webSource);
                    if (url == null)
                        continue;
                    switch (webSource) {
                        case musicbrainz    -> scrapers.add(new ScraperMusicbrainz(log, this, artist, url));
                        case beatport       -> scrapers.add(new ScraperBeatport(log, this, artist, url));
                        case junodownload   -> scrapers.add(new ScraperJunodownload(log, this, artist, url));
                        case youtube        -> scrapers.add(new ScraperYoutube(log, this, artist, url));
                    }
                }
            }
            pstmt.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error creating scrapers list");
        }
        return scrapers;
    }

    private boolean filterWords(String songName, String songType) {
        // filtering user-selected keywords
        for (String checkword : store.getFilterWords()) {
            if (songType != null) {
                if ((songType.toLowerCase()).contains(checkword.toLowerCase()))
                    return false;
            }
            if ((songName.toLowerCase()).contains(checkword.toLowerCase()))
                return false;
        }
        return true;
    }

    @Override
    public void batchInsertSongs(ArrayList<Song> songList, SourcesEnum source, int limit) {
        try (Connection conn = DriverManager.getConnection(DBpath)) {
            int i = 0;
            String sql;
            PreparedStatement pstmt = null;
            for (Song songObject : songList) {
                if (i == limit)
                    break;
                if (songObject.getType() != null && source != null) {
                    sql = "insert into " + source + "(song, artist, date, type) values(?, ?, ?, ?)";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, songObject.getName());
                    pstmt.setString(2, songObject.getArtist());
                    pstmt.setString(3, songObject.getDate());
                    pstmt.setString(4, songObject.getType());
                } else {
                    if (source != null)
                        sql = "insert into " + source + "(song, artist, date) values(?, ?, ?)";
                    else
                        sql = "insert into combview(song, artist, date) values(?, ?, ?)";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, songObject.getName());
                    pstmt.setString(2, songObject.getArtist());
                    pstmt.setString(3, songObject.getDate());
                }
                pstmt.executeUpdate();
                i++;
            }
            conn.setAutoCommit(false);
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error inserting a set of songs");
        }
    }

    @Override
    public void vacuum() {
        try (Connection conn = DriverManager.getConnection(DBpath)) {
            String sql = "VACUUM;";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.execute();
            pstmt.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.WARNING, "vacuum error");
        }
    }
}



