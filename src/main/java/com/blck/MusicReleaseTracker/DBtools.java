package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.SourcesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DataObjects.TableModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.*;
import java.util.ArrayList;
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

public class DBtools {

    private final ValueStore store;
    private final ErrorLogging log;
    private final ManageMigrateDB manageDB;

    @Autowired
    public DBtools(ValueStore valueStore, ErrorLogging errorLogging, ManageMigrateDB manageDB) {
        this.store = valueStore;
        this.log = errorLogging;
        this.manageDB = manageDB;
    }

    public List<String> getArtistList() {
        List<String> dataList = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(store.getDBpath())) {
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

    public List<TableModel> loadTable(SourcesEnum source, String name) {
        // adding data to tableContent
        List<TableModel> tableContent = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(store.getDBpath())) {
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

    public List<TableModel> loadCombviewTable() {
        List<TableModel> tableContent = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(store.getDBpath())) {
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

    public void insertIntoArtistList(String name) {
        try (Connection conn = DriverManager.getConnection(store.getDBpath())) {
            String sql = "INSERT INTO artists (artist) values(?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("artist already exists");
        }
    }

    public void updateArtistSourceID(String name, SourcesEnum source, String ID) {
        String sql;
        if (ID == null)
            sql = "UPDATE artists SET url" + source + " = NULL WHERE artist = ?";
        else
            sql = "UPDATE artists SET url" + source + " = ? WHERE artist = ?";
        try (Connection conn = DriverManager.getConnection(store.getDBpath())) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            if (ID == null) {
                pstmt.setString(1, name);
            }
            else {
                pstmt.setString(1, ID);
                pstmt.setString(2, name);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.WARNING, "could not save URL");
        }
    }

    public String getArtistSourceID(String name, SourcesEnum source) {
        String ID = null;
        String sql = "SELECT url" + source + " FROM artists WHERE artist = ?";
        try (Connection conn = DriverManager.getConnection(store.getDBpath())) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            ID = pstmt.executeQuery().getString(1);
            pstmt.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.WARNING, "error checking url existence");
        }
        return ID;
    }

    public void clearArtistDataFrom(String name, String table) {
        try (Connection conn = DriverManager.getConnection(store.getDBpath())) {
            String sql = "DELETE FROM " + table + " WHERE artist = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error deleting artists data in " + table);
        }
    }

    public void removeArtist(String name) {
        for (String tableName : manageDB.getDBStructure(store.getDBpath()).keySet())
            clearArtistDataFrom(name, tableName);
    }

    public void truncateAllScrapeData() {
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

    public void vacuum() {
        try (Connection conn = DriverManager.getConnection(store.getDBpath())) {
            String sql = "VACUUM;";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.execute();
            pstmt.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.WARNING, "vacuum error");
        }
    }
}



