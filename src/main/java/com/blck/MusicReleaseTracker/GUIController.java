package com.blck.MusicReleaseTracker;

import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


@Service
public class GUIController {

    private String lastClickedArtist;
    private String selectedSource;
    private List<TableModel> tableContent = new ArrayList<>();

    public List<String> loadList() throws SQLException {
        List<String> dataList = new ArrayList<>();
        Connection conn = DriverManager.getConnection(DBtools.DBpath);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT artistname FROM artists ORDER BY artistname ASC");
        while (rs.next()) {
            dataList.add(rs.getString("artistname"));
        }
        conn.close();
        stmt.close();
        rs.close();
        return dataList;
    }

    public List<TableModel> artistListClick(String artist) {
        lastClickedArtist = artist;
        if (lastClickedArtist != null && selectedSource != null) {
            if (selectedSource.equals("combview")) {
                try {
                    loadCombviewTable();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    loadTable();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return tableContent;
    }

    public List<TableModel> sourceTabClick(String source) {
        selectedSource = source;
        if (lastClickedArtist != null && selectedSource != null) {
            if (selectedSource.equals("combview")) {
                try {
                    loadCombviewTable();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    loadTable();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return tableContent;
    }

    public void loadTable() throws SQLException {
        tableContent.clear();
        /*Connection conn = DriverManager.getConnection(DBtools.DBpath);
        String sql = null;
        switch (selectedSource) {
            case "musicbrainz" -> sql = "SELECT urlbrainz FROM artists WHERE artistname = ? ";
            case "beatport" -> sql = "SELECT urlbeatport FROM artists WHERE artistname = ? ";
            case "junodownload" -> sql = "SELECT urljunodownload FROM artists WHERE artistname = ? ";
        }
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, lastClickedArtist);
        ResultSet rs = pstmt.executeQuery();
        //if missing url
        if (rs.next()) {
            String link = rs.getString(1);
            conn.close();
            if (link == null || link.isEmpty()) {
                switch (selectedSource) {
                    case "musicbrainz" -> {
                        System.out.println("dialog MB");
                    }
                    case "beatport" -> {
                        System.out.println("dialog BP");
                    }
                    case "junodownload" -> {
                        System.out.println("dialog JD");
                    }
                }
                return;
            }
        }*/
        //populating table
        String sql = null;
        Connection conn = DriverManager.getConnection(DBtools.DBpath);
        switch (selectedSource) {
            case "musicbrainz" -> sql = "SELECT song, date FROM musicbrainz WHERE artist = ? ORDER BY date DESC";
            case "beatport" -> sql = "SELECT song, date FROM beatport WHERE artist = ? ORDER BY date DESC";
            case "junodownload" -> sql = "SELECT song, date FROM junodownload WHERE artist = ? ORDER BY date DESC";
        }
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, lastClickedArtist);
        ResultSet rs = pstmt.executeQuery();
        //loop through the result set and add each row to the data list
        while (rs.next()) {
            String col1Value = rs.getString("song");
            String col2Value = null;
            String col3Value = rs.getString("date");
            tableContent.add(new TableModel(col1Value, col2Value, col3Value));
        }
        conn.close();
        pstmt.close();
        rs.close();
    }

    public void loadCombviewTable() throws SQLException {
        tableContent.clear();
        Connection conn = DriverManager.getConnection(DBtools.DBpath);
        //populating combview table
        String sql = "SELECT * FROM combview ORDER BY date DESC";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        //loop through the result set and add each row to the data list
        while (rs.next()) {
            String col1Value = rs.getString("song");
            String col2Value = rs.getString("artist");
            String col3Value = rs.getString("date");
            tableContent.add(new TableModel(col1Value, col2Value, col3Value));
        }
        conn.close();
        pstmt.close();
        rs.close();
    }

}