package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.ModelsEnums.TableModel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

//class with methods called from ApiController
@Service
public class GUIController {

    private String lastClickedArtist;
    private String selectedSource;
    private final List<TableModel> tableContent = new ArrayList<>();
    private String tempUrl;

    public List<String> loadList() throws SQLException {
        List<String> dataList = new ArrayList<>();
        Connection conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
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
    public void artistAddConfirm(String input) {
        //add new artist typed by user
        if (input.isEmpty() || input.isBlank())
            return;
        try {
            Connection conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
            String sql = "insert into artists(artistname) values(?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, input);
            pstmt.executeUpdate();
            conn.close();
            pstmt.close();
            lastClickedArtist = null;
        } catch (SQLException e) {
            System.out.println("artist already exists");
        }
    }
    public void artistClickDelete() {
        //delete last selected artist and all entries from artist
        if (lastClickedArtist != null) {
            try {
                Map<String, ArrayList<String>> tableMap = DBtools.getDBStructure(DBtools.settingsStore.getDBpath());
                Connection conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
                String sql;
                for (String tableName : tableMap.keySet()) {
                    if (tableName.equals("artists"))
                        sql = "DELETE FROM artists WHERE artistname = ?";
                    else
                        sql = "DELETE FROM " + tableName + " WHERE artist = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, lastClickedArtist);
                    pstmt.executeUpdate();
                }
                conn.close();
                lastClickedArtist = null;
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void deleteUrl() {
        //set null specific URL, delete related set
        if (lastClickedArtist != null && selectedSource != null) {
            try {
                Connection conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
                String sql = null;
                switch (selectedSource) {
                    case "musicbrainz" -> sql = "UPDATE artists SET urlbrainz = NULL WHERE artistname = ?";
                    case "beatport" -> sql = "UPDATE artists SET urlbeatport = NULL WHERE artistname = ?";
                    case "junodownload" -> sql = "UPDATE artists SET urljunodownload = NULL WHERE artistname = ?";
                    case "youtube" -> sql = "UPDATE artists SET urlyoutube = NULL WHERE artistname = ?";
                    default -> {
                        return;
                    }
                }
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, lastClickedArtist);
                pstmt.executeUpdate();
                sql = "DELETE FROM " + selectedSource + " WHERE artist = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, lastClickedArtist);
                pstmt.execute();
                conn.close();
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void cleanArtistSource() {
        //clear artist entries from a source table, used by scrape preview
        try {
            Connection conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
            String sql = "DELETE FROM " + selectedSource + " WHERE artist = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, lastClickedArtist);
            pstmt.executeUpdate();
            conn.close();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<TableModel> artistListClick(String artist) {
        //when artist and source selected, load respective table
        lastClickedArtist = artist;
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
        return tableContent;
    }

    public List<TableModel> sourceTabClick(String source) {
        //when source and artist selected, load respective table
        selectedSource = source;
        if (!selectedSource.equals("combview")) {
            try {
                loadTable();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (selectedSource.equals("combview")) {
            try {
                loadCombviewTable();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return tableContent;
    }

    public void loadTable() throws SQLException {
        tableContent.clear();
        //adding data to tableContent
        Connection conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
        String sql = "SELECT song, date FROM " + selectedSource + " WHERE artist = ? ORDER BY date DESC";
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
        Connection conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
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
    public void fillCombview() {
        MainBackend.fillCombviewTable(null);
    }

    public void clickAddURL(String url) {
        if (lastClickedArtist == null || selectedSource == null)
            return;
        tempUrl = null;
        //processing user-pasted url
        url = url.replace("=" , "").trim();
        if (url.isEmpty() || url.isBlank())
            return;

        int artistIndex;
        //reduce to base form then modify
        switch (selectedSource) {
            case "musicbrainz" -> {
                //https://musicbrainz.org/artist/ad110705-cbe6-4c47-9b99-8526e6db0f41/recordings
                artistIndex = url.indexOf("/artist/");
                if (artistIndex != -1 && url.contains("musicbrainz.org")) {
                    //index of following "/" after "/artist/" - starting from the index that is sum of artistIndex and the length of "/artist/"
                    //"/artist/".length() is to skip the "/artist/" part and start the search from the beginning of the ID
                    int artistIdIndex = url.indexOf('/', artistIndex + "/artist/".length());
                    if (artistIdIndex != -1)
                        url = url.substring(0, artistIdIndex);
                //https://musicbrainz.org/artist/ad110705-cbe6-4c47-9b99-8526e6db0f41
                }
                else
                    return;
                try {
                    MainBackend.scrapeBrainz(url, lastClickedArtist);
                } catch (IOException e) {
                    System.out.println("incorrect link");
                }
            }
            case "beatport" -> {
                //https://beatport.com/artist/koven/245904/charts
                artistIndex = url.indexOf("/artist/");
                if (artistIndex != -1 && url.contains("beatport.com")) {
                    int artistIdIndex = url.indexOf('/', artistIndex + "/artist/".length());
                    if (artistIdIndex != -1) {
                        artistIdIndex = url.indexOf('/', artistIdIndex + 1); //skip one '/' and find the next '/'
                        if (artistIdIndex != -1) {
                            url = url.substring(0, artistIdIndex); //remove the trailing '/'
                        }
                //https://beatport.com/artist/koven/245904
                    }
                }
                else
                    return;
                //necessary tab
                url += "/tracks";
                //https://beatport.com/artist/koven/245904/tracks
                try {
                    MainBackend.scrapeBeatport(url, lastClickedArtist);
                } catch (IOException e) {
                    System.out.println("incorrect link");
                }
            }
            case "junodownload" -> {
                //https://www.junodownload.com/artists/Koven/releases/
                artistIndex = url.indexOf("/artists/");
                if (artistIndex != -1 && url.contains("junodownload.com")) {
                    int artistIdIndex = url.indexOf('/', artistIndex + "/artists/".length());
                    if (artistIdIndex != -1)
                        url = url.substring(0, artistIdIndex + 1);
                //https://www.junodownload.com/artists/Koven/
                }
                else
                    return;
                //necessary filters
                url += "releases/?music_product_type=single&laorder=date_down";
                //https://www.junodownload.com/artists/Koven/releases/?music_product_type=single&laorder=date_down
                try {
                    MainBackend.scrapeJunodownload(url, lastClickedArtist);
                } catch (IOException e) {
                    System.out.println("incorrect link");
                }
            }
            case "youtube" -> {
                try {
                    MainBackend.scrapeYoutube(url, lastClickedArtist);
                } catch (IOException e) {
                    System.out.println("incorrect link");
                }
            }
        }
        tempUrl = url;
    }

    public void saveUrl() {
        //save artist url to db
        String sql = null;
        switch (selectedSource) {
            case "musicbrainz" -> sql = "UPDATE artists SET urlbrainz = ? WHERE artistname = ?";
            case "beatport" -> sql = "UPDATE artists SET urlbeatport = ? WHERE artistname = ?";
            case "junodownload" -> sql = "UPDATE artists SET urljunodownload = ? WHERE artistname = ?";
            case "youtube" -> sql = "UPDATE artists SET urlyoutube = ? WHERE artistname = ?";
            default -> {
                return;
            }
        }
        try {
            Connection conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, tempUrl);
            pstmt.setString(2, lastClickedArtist);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public boolean checkExistURL() {
        //check for existence of url to determine showing url dialog
        boolean urlExists = false;
        try {
            Connection conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
            String sql = null;
            switch (selectedSource) {
                case "musicbrainz" -> sql = "SELECT urlbrainz FROM artists WHERE artistname = ?";
                case "beatport" -> sql = "SELECT urlbeatport FROM artists WHERE artistname = ?";
                case "junodownload" -> sql = "SELECT urljunodownload FROM artists WHERE artistname = ?";
                case "youtube" -> sql = "SELECT urlyoutube FROM artists WHERE artistname = ?";
                default -> {
                    return urlExists;
                }
            }
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, lastClickedArtist);
            ResultSet rs = pstmt.executeQuery();

            if (rs.getString(1) != null)
                urlExists = true;

            rs.close();
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return urlExists;
    }

    public void clickScrape() {
        //launch scraping in backend, then fill and load table
        try {
            MainBackend.scrapeData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            MainBackend.fillCombviewTable(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
            String sql = "VACUUM;";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.execute();
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void cancelScrape() {
        MainBackend.scrapeCancel = true;
    }

    public HashMap<String, Boolean> settingsOpened() {
        //gather all settings states and return them to frontend when settings are opened
        DBtools.readConfig("filters");
        HashMap<String, Boolean> configData = new HashMap<>();

        ArrayList<String> filterWords = DBtools.settingsStore.getFilterWords();
        String[] allFilters = new String[]{"Acoustic", "Extended", "Instrumental", "Remaster", "Remix", "VIP"};
        for (String filter : allFilters) {
            if (filterWords.contains(filter))
                configData.put(filter, true);
            else
                configData.put(filter, false);
        }
        DBtools.readConfig("longTimeout");
        if (DBtools.settingsStore.getTimeout() > 20000)
            configData.put("longTimeout", true);
        else
            configData.put("longTimeout", false);
        DBtools.readConfig("isoDates");
        configData.put("isoDates", DBtools.settingsStore.getIsoDates());

        return configData;
    }

    public void setSetting(String name, String value) {
        //write any setting in config, note: "name" = config name
        DBtools.writeSingleConfig(name, value);
    }
    public Map<String,String> getThemeConfig() {
        DBtools.readConfig("themes");
        return DBtools.settingsStore.getThemes();
    }
    public String getScrapeDate() {
        DBtools.readConfig("lastScrape");
        return DBtools.settingsStore.getScrapeDate();
    }
    public String getLastArtist() {
        return lastClickedArtist;
    }
    public void resetSettings() {
        DBtools.resetSettings();
    }
    public void resetDB() {
        DBtools.resetDB();
    }
}