package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.ModelsEnums.TableModel;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValueFactory;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
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
            throw new RuntimeException(e);
        }
    }
    public void artistClickDelete() {
        //delete last selected artist and all entries from artist
        if (lastClickedArtist != null) {
            try {
                Connection conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
                String sql = "DELETE FROM artists WHERE artistname = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, lastClickedArtist);
                pstmt.executeUpdate();
                sql = "DELETE FROM musicbrainz WHERE artist = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, lastClickedArtist);
                pstmt.executeUpdate();
                sql = "DELETE FROM beatport WHERE artist = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, lastClickedArtist);
                pstmt.executeUpdate();
                sql = "DELETE FROM junodownload WHERE artist = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, lastClickedArtist);
                pstmt.executeUpdate();
                conn.close();
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void cleanArtistSource() {
        //clean artist from source table
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
        String sql = null;
        Connection conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
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
        MainBackend.fillCombviewTable();
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
                    int artistIdIndex = url.indexOf('/', artistIndex + "/artist/".length());
                    if (artistIdIndex != -1)
                        url = url.substring(0, artistIdIndex);
                //https://musicbrainz.org/artist/ad110705-cbe6-4c47-9b99-8526e6db0f41
                }
                else
                    return;
                //for latest releases
                if(!url.contains("page="))
                    url += "/releases/?page=20";
                //https://musicbrainz.org/artist/ad110705-cbe6-4c47-9b99-8526e6db0f41/releases/?page=20
                try {
                    MainBackend.scrapeBrainz(url, lastClickedArtist);
                } catch (IOException e) {
                    throw new RuntimeException(e);
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
                    throw new RuntimeException(e);
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
                    throw new RuntimeException(e);
                }
            }
        }
        tempUrl = url;
    }

    public void saveUrl() {
        //save artist url to db
        String sql;
        switch (selectedSource) {
            case "musicbrainz" -> sql = "UPDATE artists SET urlbrainz = ? WHERE artistname = ?";
            case "beatport" -> sql = "UPDATE artists SET urlbeatport = ? WHERE artistname = ?";
            case "junodownload" -> sql = "UPDATE artists SET urljunodownload = ? WHERE artistname = ?";
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

    public void clickScrape() {
        try {
            MainBackend.scrapeData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            MainBackend.fillCombviewTable();
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
        //gather all settings states and return them to frontend
        DBtools.readCombviewConfig();
        HashMap<String, Boolean> configData = new HashMap<>();

        ArrayList<String> filterWords = DBtools.settingsStore.getFilterWords();
        String[] allFilters = new String[]{"Acoustic", "Extended", "Instrumental", "Remaster", "Remix", "VIP"};
        for (String filter : allFilters) {
            if (filterWords.contains(filter))
                configData.put(filter, true);
            else
                configData.put(filter, false);
        }

        return configData;
    }

    public void toggleFilter(String filter, Boolean value) {
        //change config filter state
        Config config = ConfigFactory.parseFile(new File(DBtools.settingsStore.getConfigPath()));
        config = config.withValue("filters." + filter, ConfigValueFactory.fromAnyRef(value));
        ConfigRenderOptions renderOptions = ConfigRenderOptions.defaults().setOriginComments(false).setJson(false).setFormatted(true);

        try (PrintWriter writer = new PrintWriter(new FileWriter(DBtools.settingsStore.getConfigPath()))) {
            writer.write(config.root().render(renderOptions));
        } catch (IOException e) {
            System.out.println("could not save filter change");
            e.printStackTrace();
        }
    }

}