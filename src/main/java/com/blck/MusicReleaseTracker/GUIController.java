package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Scrapers.*;
import com.blck.MusicReleaseTracker.Simple.ErrorLogging;
import com.blck.MusicReleaseTracker.Simple.TableModel;
import org.springframework.beans.factory.annotation.Autowired;
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

/** class with methods called from ApiController */
public class GUIController {

    private final ValueStore store;
    private final ErrorLogging log;
    private final ScrapeProcess scrapeProcess;
    private final ConfigTools config;
    private final DBtools DB;
    private final List<TableModel> tableContent = new ArrayList<>();
    private enum sourceEnum {
        combview, beatport, musicbrainz, junodownload, youtube
    }
    private sourceEnum selectedSource;
    private String lastClickedArtist;
    private String tempID;

    @Autowired
    public GUIController(ValueStore valueStore, ErrorLogging errorLogging, ScrapeProcess scrapeProcess, ConfigTools config, DBtools DB) {
        this.store = valueStore;
        this.log = errorLogging;
        this.scrapeProcess = scrapeProcess;
        this.config = config;
        this.DB = DB;
    }

   public List<String> loadList() throws SQLException {
       List<String> dataList = new ArrayList<>();
       try {
            Connection conn = DriverManager.getConnection(store.getDBpath());
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT artistname FROM artists ORDER BY artistname ASC");
            while (rs.next()) {
                dataList.add(rs.getString("artistname"));
            }
            conn.close();
            stmt.close();
            rs.close();
       } catch (SQLException e) {
           log.error(e, ErrorLogging.Severity.SEVERE, "error loading list");
       }
       return dataList;
    }

    public void artistAddConfirm(String input) {
        // add new artist typed by user
        if (input.isEmpty() || input.isBlank())
            return;
        try {
            Connection conn = DriverManager.getConnection(store.getDBpath());
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

    public void artistClickDelete() {
        // delete last selected artist and all entries from artist
        if (store.getDBpath().contains("testing"))
            lastClickedArtist = "Joe";

        if (lastClickedArtist != null) {
            try {
                Map<String, ArrayList<String>> tableMap = null;
                Connection conn = DriverManager.getConnection(store.getDBpath());
                tableMap = DB.getDBStructure(store.getDBpath());
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
                log.error(e, ErrorLogging.Severity.WARNING, "error deleting an artist");
            }
        }
    }

    public void deleteUrl() {
        if (store.getDBpath().contains("testing")) {
            selectedSource = sourceEnum.beatport;
            lastClickedArtist = "Joe";
        }
        // set null specific URL, delete related set
        if (lastClickedArtist != null && selectedSource != null) {
            try {
                Connection conn = DriverManager.getConnection(store.getDBpath());
                String sql = "UPDATE artists SET url" + selectedSource +  " = NULL WHERE artistname = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, lastClickedArtist);
                pstmt.executeUpdate();
                sql = "DELETE FROM " + selectedSource + " WHERE artist = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, lastClickedArtist);
                pstmt.execute();
                conn.close();
            } catch (SQLException e) {
                log.error(e, ErrorLogging.Severity.SEVERE, "error deleting an URL");
            }
        }
    }

    public void cleanArtistSource() {
        // clear artist entries from a source table, used by scrape preview
        try {
            Connection conn = DriverManager.getConnection(store.getDBpath());
            String sql = "DELETE FROM " + selectedSource + " WHERE artist = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, lastClickedArtist);
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error deleting a source URL");
        }
    }

    public List<TableModel> listOrTabClick(String item, String origin) {
        // when source or artist selected, load respective table
        if (origin.equals("list"))
            lastClickedArtist = item;
        else if (origin.equals("tab"))
            selectedSource = sourceEnum.valueOf(item);

        try {
            if (selectedSource == sourceEnum.combview)
                loadCombviewTable();
            else if (selectedSource != null && lastClickedArtist != null)
                loadTable();
        }
        catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.WARNING, "error loading a table");
        }
        return tableContent;
    }

    public void loadTable() throws SQLException {
        tableContent.clear();
        // adding data to tableContent
        Connection conn = DriverManager.getConnection(store.getDBpath());
        String sql = "SELECT song, date FROM " + selectedSource + " WHERE artist = ? ORDER BY date DESC";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, lastClickedArtist);
        ResultSet rs = pstmt.executeQuery();
        // loop through the result set and add each row to the data list
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
        Connection conn = DriverManager.getConnection(store.getDBpath());
        // populating combview table
        String sql = "SELECT * FROM combview ORDER BY date DESC";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        // loop through the result set and add each row to the data list
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
        scrapeProcess.fillCombviewTable();
    }

    public void clickAddURL(String url) {
        if (lastClickedArtist == null || selectedSource == null || url.isBlank())
            return;

        tempID = null;
        String id = null;
        try {
            ScraperParent scraper = null;
            switch(selectedSource) {
                case musicbrainz    -> scraper = new MusicbrainzScraper(null, null, null, url);
                case beatport       -> scraper = new BeatportScraper(null, null, null, url);
                case junodownload   -> scraper = new JunodownloadScraper(null, null, null, url);
                case youtube        -> scraper = new YoutubeScraper(null, null, null, url);
            }
            id = scraper.getID();
            scraper.scrape();
        } catch (Exception e) {
            log.error(e, ErrorLogging.Severity.WARNING, "error scraping " + selectedSource + ", perhaps an incorrect link");
        }

        tempID = id;
    }

    public void saveUrl() {
        // save artist url to db
        if (store.getDBpath().contains("testing")) {
            selectedSource = sourceEnum.beatport;
            lastClickedArtist = "Joe";
            tempID = "testingUrl";
        }
        String sql = "UPDATE artists SET url" + selectedSource + " = ? WHERE artistname = ?";
        try {
            Connection conn = DriverManager.getConnection(store.getDBpath());
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, tempID);
            pstmt.setString(2, lastClickedArtist);
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.WARNING, "could not save URL");
        }
    }

    public boolean checkExistURL() {
        // check for existence of url to determine visibility of url dialog
        boolean urlExists = false;
        try {
            if (store.getDBpath().contains("testing")) {
                selectedSource = sourceEnum.beatport;
                lastClickedArtist = "Joe";
            }
            else if (selectedSource == sourceEnum.combview)
                return urlExists;

            String sql = "SELECT url" + selectedSource + " FROM artists WHERE artistname = ?";
            Connection conn = DriverManager.getConnection(store.getDBpath());
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, lastClickedArtist);
            ResultSet rs = pstmt.executeQuery();

            if (rs.getString(1) != null)
                urlExists = true;

            rs.close();
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error checking whether URL exists");
        }
        return urlExists;
    }

    public void clickScrape() {
        // launch scraping in backend, then fill and load table
        try {
            scrapeProcess.scrapeData();
            scrapeProcess.fillCombviewTable();
        } catch (Exception e) {
            log.error(e, ErrorLogging.Severity.WARNING, "scrapeProcess error");
        }
        try {
            Connection conn = DriverManager.getConnection(store.getDBpath());
            String sql = "VACUUM;";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.execute();
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.WARNING, "vacuum error: clickScrape");
        }
    }

    public void cancelScrape() {
        scrapeProcess.scrapeCancel = true;
    }

    public HashMap<String, Boolean> settingsOpened() {
        // gather all settings states and return them to frontend when settings are opened
        HashMap<String, Boolean> configData = new HashMap<>();

        config.readConfig(ConfigTools.configOptions.filters);
        ArrayList<String> filterWords = store.getFilterWords();
        String[] allFilters = new String[]{"Acoustic", "Extended", "Instrumental", "Remaster", "Remix", "VIP"};
        for (String filter : allFilters) {
            if (filterWords.contains(filter))
                configData.put(filter, true);
            else
                configData.put(filter, false);
        }

        config.readConfig(ConfigTools.configOptions.longTimeout);
        if (store.getTimeout() > 25000)
            configData.put("longTimeout", true);
        else
            configData.put("longTimeout", false);

        config.readConfig(ConfigTools.configOptions.isoDates);
        configData.put("isoDates", store.getIsoDates());
        config.readConfig(ConfigTools.configOptions.systemTheme);
        configData.put("systemTheme", store.getSystemTheme());

        return configData;
    }

    public void setSetting(String name, String value) {
        // write any setting in config, note: "name" = config name
        config.writeSingleConfig(name, value);
    }
    public Map<String,String> getThemeConfig() {
        config.readConfig(ConfigTools.configOptions.themes);
        return store.getThemes();
    }
    public String getScrapeDate() {
        config.readConfig(ConfigTools.configOptions.lastScrape);
        return store.getScrapeDate();
    }
    public String getLastArtist() {
        return lastClickedArtist;
    }
    public void resetSettings() {
        config.resetSettings();
    }
    public void resetDB() {
        DB.resetDB();
    }
}