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
    private String tempID;

    public List<String> loadList(String testPath) throws SQLException {
        List<String> dataList = new ArrayList<>();
        Connection conn = null;
        if (testPath.isBlank())
            conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
        else
            conn = DriverManager.getConnection(testPath);
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
    public void artistAddConfirm(String input, String testPath) {
        //add new artist typed by user
        if (input.isEmpty() || input.isBlank())
            return;
        try {
            Connection conn = null;
            if (testPath.isBlank())
                conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
            else
                conn = DriverManager.getConnection(testPath);
            String sql = "INSERT INTO artists (artistname) values(?)";
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
    public void artistClickDelete(String testPath) {
        //delete last selected artist and all entries from artist
        if (!testPath.isBlank())
            lastClickedArtist = "Joe";

        if (lastClickedArtist != null) {
            try {
                Map<String, ArrayList<String>> tableMap = null;
                Connection conn = null;
                if (testPath.isBlank()) {
                    tableMap = DBtools.getDBStructure(DBtools.settingsStore.getDBpath());
                    conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
                }
                else {
                    tableMap = DBtools.getDBStructure(testPath);
                    conn = DriverManager.getConnection(testPath);
                }
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
                DBtools.logError(e, "WARNING", "error deleting an artist");
            }
        }
    }
    public void deleteUrl(String testPath) {
        if (!testPath.isBlank()) {
            selectedSource = "beatport";
            lastClickedArtist = "Joe";
        }
        //set null specific URL, delete related set
        if (lastClickedArtist != null && selectedSource != null) {
            try {
                Connection conn = null;
                if (testPath.isBlank())
                    conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
                else
                    conn = DriverManager.getConnection(testPath);
                String sql = "UPDATE artists SET url" + selectedSource +  " = NULL WHERE artistname = ?";
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
                DBtools.logError(e, "SEVERE", "error deleting an URL");
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
            DBtools.logError(e, "SEVERE", "error deleting a source URL");
        }
    }

    public List<TableModel> listOrTabClick(String item, String origin) {
        //when source or artist selected, load respective table
        if (origin.equals("list"))
            lastClickedArtist = item;
        else if (origin.equals("tab"))
            selectedSource = item;

        if (selectedSource.equals("combview")) {
            try {
                loadCombviewTable();
            } catch (SQLException e) {
                DBtools.logError(e, "WARNING", "error loading combview");
            }
        }
        else if (selectedSource != null &&lastClickedArtist != null) {
            try {
                loadTable();
            } catch (SQLException e) {
                DBtools.logError(e, "WARNING", "error loading a table");
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
        tempID = null;

        url = url.replace("=" , "").trim();
        if (url.isEmpty() || url.isBlank())
            return;

        String id = MainBackend.reduceToID(url, selectedSource);
        if (id == null)
            return;

        try {
            switch(selectedSource) {
                case "musicbrainz" -> MainBackend.scrapeBrainz(id, lastClickedArtist);
                case "beatport" -> MainBackend.scrapeBeatport(id, lastClickedArtist);
                case "junodownload" -> MainBackend.scrapeJunodownload(id, lastClickedArtist);
                case "youtube" -> MainBackend.scrapeYoutube(id, lastClickedArtist);
            }
        } catch (IOException e) {
            DBtools.logError(e, "WARNING", "error scraping " + selectedSource + ", perhaps an incorrect link");
        }

        tempID = id;
    }

    public void saveUrl(String testPath) {
        //save artist url to db
        if (!testPath.isBlank()) {
            selectedSource = "beatport";
            lastClickedArtist = "Joe";
            tempID = "testingUrl";
        }
        String sql = "UPDATE artists SET url" + selectedSource + " = ? WHERE artistname = ?";
        try {
            Connection conn;
            if (testPath.isBlank())
                conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
            else
                conn = DriverManager.getConnection(testPath);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, tempID);
            pstmt.setString(2, lastClickedArtist);
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            DBtools.logError(e, "WARNING", "could not save URL");
        }
    }
    public boolean checkExistURL(String testPath) {
        //check for existence of url to determine showing url dialog
        boolean urlExists = false;
        try {
            Connection conn;
            if (testPath.isBlank())
                conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
            else {
                conn = DriverManager.getConnection(testPath);
                selectedSource = "beatport";
                lastClickedArtist = "Joe";
            }
            String sql = null;
            if (!selectedSource.equals("combview"))
                sql = "SELECT url" + selectedSource + " FROM artists WHERE artistname = ?";
            else
                return urlExists;
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, lastClickedArtist);
            ResultSet rs = pstmt.executeQuery();

            if (rs.getString(1) != null)
                urlExists = true;

            rs.close();
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            DBtools.logError(e, "SEVERE", "error checking whether URL exists");
        }
        return urlExists;
    }

    public void clickScrape() {
        //launch scraping in backend, then fill and load table
        try {
            MainBackend.scrapeData();
        } catch (Exception e) {
            DBtools.logError(e, "WARNING", "scrapeData error: clickScrape");
        }
        try {
            MainBackend.fillCombviewTable(null);
        } catch (Exception e) {
            DBtools.logError(e, "WARNING", "fillCombviewTable error: clickScrape");
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
            DBtools.logError(e, "WARNING", "vacuum error: clickScrape");
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