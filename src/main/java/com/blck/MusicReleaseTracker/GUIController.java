package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.ModelsEnums.TableModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//class with methods called from ApiController
@Service
public class GUIController {

    private String lastClickedArtist;
    private String selectedSource;
    private List<TableModel> tableContent = new ArrayList<>();

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
        input = input.replace("=" , "").trim();
        if (input.isEmpty() || input.isBlank() || input.length() > 30 || input.equals("[]"))
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

    public void clickAddURL(String url) {
        url = url.replace("=" , "").trim();
        if (url.isEmpty() || url.isBlank())
            return;

        String sql = null;
        int artistIndex;
        //reduce to base form then modify
        switch (selectedSource) {
            case "musicbrainz" -> {
                sql = "UPDATE artists SET urlbrainz = ? WHERE artistname = ?";
                artistIndex = url.indexOf("/artist/");
                if (artistIndex != -1 && url.contains("musicbrainz.org")) {
                    int artistIdIndex = url.indexOf('/', artistIndex + "/artist/".length());
                    if (artistIdIndex != -1)
                        url = url.substring(0, artistIdIndex);
                }
                else
                    return;
                //modify link to latest releases
                if(!url.contains("page="))
                    url += "/releases/?page=20";
            }
            case "beatport" -> {
                sql = "UPDATE artists SET urlbeatport = ? WHERE artistname = ?";
                artistIndex = url.indexOf("/artist/");
                if (artistIndex != -1 && url.contains("beatport.com")) {
                    int artistIdIndex = url.indexOf('/', artistIndex + "/artist/".length());
                    if (artistIdIndex != -1) {
                        artistIdIndex = url.indexOf('/', artistIdIndex + 1); // skip one '/' and find the next '/'
                        if (artistIdIndex != -1) {
                            url = url.substring(0, artistIdIndex); // remove the trailing '/'
                        }
                    }
                }
                else
                    return;
                url += "/tracks";
            }
            case "junodownload" -> {
                sql = "UPDATE artists SET urljunodownload = ? WHERE artistname = ?";
                artistIndex = url.indexOf("/artists/");
                if (artistIndex != -1 && url.contains("junodownload.com")) {
                    int artistIdIndex = url.indexOf('/', artistIndex + "/artists/".length());
                    if (artistIdIndex != -1)
                        url = url.substring(0, artistIdIndex + 1);
                }
                else
                    return;
                url += "releases/?music_product_type=single&laorder=date_down";
            }
        }
        saveUrl(sql, url);
    }

    public void saveUrl(String sql, String url) {
        //validation of links and saving to db
        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/").timeout(40000).get();
        } catch (IOException e) {
            System.out.println("link verification: task timed out");
            return;
        }
        switch (selectedSource) {
            case "musicbrainz" -> {
                Elements songs = doc.select("[href*=/release/]");
                String[] songsArray = songs.eachText().toArray(new String[0]);
                songs.clear();
                doc.empty();
                if (songsArray.length == 0 || songsArray == null)
                    return;
            }
            case "beatport" -> {
                Elements script = doc.select("script#__NEXT_DATA__[type=application/json]");
                String JSON = script.first().data();
                Pattern pattern = Pattern.compile(
                        "\"mix_name\"\\s*:\\s*\"([^\"]+)\",\\s*" +
                                "\"name\"\\s*:\\s*\"([^\"]+)\",\\s*" +
                                "\"new_release_date\"\\s*:\\s*\"([^\"]+)\""
                );
                Matcher matcher = pattern.matcher(JSON);
                List<String> songsArray = new ArrayList<>();
                while (matcher.find()) {
                    songsArray.add(matcher.group(2));
                }
                doc.empty();
                script.clear();
                if (songsArray.isEmpty())
                    return;
            }

            case "junodownload" -> {
                Elements songs = doc.select("a.juno-title");
                String[] songsArray = songs.eachText().toArray(new String[0]);
                songs.clear();
                doc.empty();
                if (songsArray.length == 0 || songsArray == null)
                    return;
            }
            default -> {
                return;
            }
        }

        try {
            Connection conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, url);
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
            System.out.println("catastrophic error during scraping");
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

}