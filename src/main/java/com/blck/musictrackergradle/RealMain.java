package com.blck.musictrackergradle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*      MusicReleaseTrcker
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

public class RealMain extends Application {

    private static GUIController GUIController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/blck/musictrackergradle/mygui.fxml"));
        Parent root = loader.load();
        GUIController = loader.getController();
        Scene scene = new Scene(root);
        Image icon = new Image(getClass().getResourceAsStream("/MRTlogo.png"));
        primaryStage.getIcons().add(icon);
        primaryStage.setTitle("Music Release Tracker");
        primaryStage.setScene(scene);
        primaryStage.setMinHeight(520);
        primaryStage.setMinWidth(770);
        primaryStage.setMaxWidth(880);
        primaryStage.show();
    }

    public static void main(String[] args) throws SQLException {
        DBtools.path();
        DBtools.createTables();
        launch(args);
    }

    //entryLimit: how many entries per artist
    private static final int entryLimit = 15;

    public static void scrapeData() throws SQLException, InterruptedException, IOException {
        //for each artistname: check 3 urls and load them into a list
        Connection conn = DriverManager.getConnection(DBtools.path);
        String sql = "SELECT artistname FROM artists";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet artistnameResults = pstmt.executeQuery();
        ArrayList<String> artistnameList = new ArrayList<>();
        double progress = 0;
        while (artistnameResults.next()) {
            artistnameList.add(artistnameResults.getString("artistname"));
        }
        if (artistnameList.size() == 0)
            return;
        //clear entries from tables
        sql = "DELETE FROM musicbrainz";
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        sql = "DELETE FROM beatport";
        stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        sql = "DELETE FROM junodownload";
        stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        pstmt.close();
        artistnameResults.close();
        conn.close();
        stmt.close();
        //list for storing source urls (incl null) of one artist at a time
        ArrayList<String> eachArtistUrls = new ArrayList<>();
        for (String artistnamerow : artistnameList) {
            conn = DriverManager.getConnection(DBtools.path);
            eachArtistUrls.clear();
            sql = "SELECT urlbrainz FROM artists WHERE artistname = ? ";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, artistnamerow);
            ResultSet RSeachArtistUrls = pstmt.executeQuery();
            eachArtistUrls.add(RSeachArtistUrls.getString("urlbrainz"));
            sql = "SELECT urlbeatport FROM artists WHERE artistname = ? ";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, artistnamerow);
            RSeachArtistUrls = pstmt.executeQuery();
            eachArtistUrls.add(RSeachArtistUrls.getString("urlbeatport"));
            sql = "SELECT urljunodownload FROM artists WHERE artistname = ? ";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, artistnamerow);
            RSeachArtistUrls = pstmt.executeQuery();
            eachArtistUrls.add(RSeachArtistUrls.getString("urljunodownload"));
            conn.close();
            pstmt.close();
            RSeachArtistUrls.close();
            //calling scrapers
            int i = 1;
            for (String oneurl : eachArtistUrls)
            {
                switch (i) {
                    case 1 -> {
                        if (oneurl != null)
                            scrapeBrainz(oneurl, artistnamerow);
                    }
                    case 2 -> {
                        if (oneurl != null)
                            scrapeBeatport(oneurl, artistnamerow);
                    }
                    case 3 -> {
                        if (oneurl != null)
                            scrapeJunodownload(oneurl, artistnamerow);
                        Thread.sleep(1200);
                    }
                }
                i++;
            }
            progress++;
            double state = progress / artistnameList.size();
            GUIController.updateProgressBar(state);
        }
    }
    private static void scrapeBrainz(String oneurl, String artistnamerow) throws IOException, SQLException {
        System.out.println(oneurl);
        //scraper for musicbrainz
        Document doc = null;
        try {
            doc = Jsoup.connect(oneurl).timeout(40000).get();
        } catch (SocketTimeoutException e) {
            System.out.println("Task timed out");
            return;
        }
        Elements songs = doc.select("[href*=/release/]");
        Elements dates = doc.select("ul.release-events > li:first-child").select("span.release-date");
        String[] songsArray = songs.eachText().toArray(new String[0]);
        String[] datesArray = dates.eachText().toArray(new String[0]);
        Connection conn = DriverManager.getConnection(DBtools.path);
        //fill table
        int entriesInserted = 0;
        int i = 0;
        while (entriesInserted < entryLimit) {
            if (i == songsArray.length - 1)
                break;
            String songname = songsArray[songsArray.length - i - 1];
            String songdate = datesArray[datesArray.length - i - 1];
            //discard songs with missing dates - band-aid
            if (songdate.equals("-") || songdate == null) {
                i++;
                continue;
            }
            String sql = "SELECT COUNT(*) FROM musicbrainz WHERE song = ? AND artist = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, songname);
            pstmt.setString(2, artistnamerow);
            ResultSet result = pstmt.executeQuery();
            //if not duplicate add new song
            if (result.getInt(1) == 0) {
                sql = "insert into musicbrainz(song, artist, date) values(?, ?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, songname);
                pstmt.setString(2, artistnamerow);
                pstmt.setString(3, songdate);
                pstmt.executeUpdate();
                entriesInserted++;
            }
            i++;
            pstmt.close();
            result.close();
        }
        conn.close();
    }

    private static void scrapeBeatport(String oneurl, String artistnamerow) throws IOException, SQLException {
        System.out.println(oneurl);
        //scraper for beatport
        Document doc = null;
        try {
            doc = Jsoup.connect(oneurl).timeout(40000).get();
        } catch (SocketTimeoutException e) {
            System.out.println("Task timed out");
            return;
        }
        Elements songs = doc.select("span.buk-track-primary-title");
        Elements types = doc.select("span.buk-track-remixed");
        Elements dates = doc.select("p.buk-track-released");
        String[] songsArray = songs.eachText().toArray(new String[0]);
        String[] typeArray = types.eachText().toArray(new String[0]);
        String[] datesArray = dates.eachText().toArray(new String[0]);
        Connection conn = DriverManager.getConnection(DBtools.path);
        //fill table
        int entriesInserted = 0;
        int i = 0;
        mainloop: while (entriesInserted < entryLimit) {
            if (i == songsArray.length - 1)
                break;
            String songname = songsArray[i];
            String songdate = datesArray[i + 1];
            String songtype = typeArray[i];
            //checking if rereleased
            for (int x = (songsArray.length - 1); x > i; x--) {
                if (songname.equals(songsArray[x])) {
                    i++;
                    continue mainloop;
                }
            }
            String sql = "SELECT COUNT(*) FROM beatport WHERE song = ? AND artist = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, songname);
            pstmt.setString(2, artistnamerow);
            ResultSet result = pstmt.executeQuery();
            //if not duplicate add new song
            if (result.getInt(1) == 0) {
                sql = "insert into beatport(song, artist, date, type) values(?, ?, ?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, songname);
                pstmt.setString(2, artistnamerow);
                pstmt.setString(3, songdate);
                pstmt.setString(4, songtype);
                pstmt.executeUpdate();
                entriesInserted++;
            }
            i++;
            pstmt.close();
            result.close();
        }
        conn.close();
    }
    private static void scrapeJunodownload(String oneurl, String artistnamerow) throws IOException, SQLException {
        System.out.println(oneurl);
        //scraper for junodownload
        Document doc = null;
        try {
            doc = Jsoup.connect(oneurl).timeout(40000).get();
        } catch (SocketTimeoutException e) {
            System.out.println("Task timed out");
            return;
        }
        Elements songs = doc.select("a.juno-title");
        Elements dates = doc.select("div.text-sm.mb-3.mb-lg-3");
        String[] songsArray = songs.eachText().toArray(new String[0]);
        //dates: select including <br>, select correct substring
        Map<String, String> monthMap = new HashMap<>();
        monthMap.put("Jan", "01");
        monthMap.put("Feb", "02");
        monthMap.put("Mar", "03");
        monthMap.put("Apr", "04");
        monthMap.put("May", "05");
        monthMap.put("Jun", "06");
        monthMap.put("Jul", "07");
        monthMap.put("Aug", "08");
        monthMap.put("Sep", "09");
        monthMap.put("Oct", "10");
        monthMap.put("Nov", "11");
        monthMap.put("Dec", "12");
        String[] datesArray = new String[dates.size()];
        //loop over the dates, format them
        for (int i = 0; i < dates.size(); i++) {
            try {
            String dateStr = dates.get(i).outerHtml().substring(dates.get(i).outerHtml().indexOf("<br>") + 4, dates.get(i).outerHtml().lastIndexOf("<br>")).trim();
            String[] parts = dateStr.split(" ");
            String month = parts[1];
            String monthNumber = monthMap.get(month);
            String result = null;
            result = "20" + parts[2] + "-" + monthNumber + "-" + parts[0];
            datesArray[i] = result;
            } catch (Exception e) {
                datesArray[i] = null;
            }
        }

        Connection conn = DriverManager.getConnection(DBtools.path);
        //fill table
        int entriesInserted = 0;
        int i = 0;
        while (entriesInserted < entryLimit) {
            if (i == songsArray.length)
                break;
            String songname = songsArray[i];
            String songdate = datesArray[i];
            String sql = "SELECT COUNT(*) FROM junodownload WHERE song = ? AND artist = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, songname);
            pstmt.setString(2, artistnamerow);
            ResultSet result = pstmt.executeQuery();
            //if not duplicate add new song
            if (result.getInt(1) == 0) {
                sql = "insert into junodownload(song, artist, date) values(?, ?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, songname);
                pstmt.setString(2, artistnamerow);
                pstmt.setString(3, songdate);
                pstmt.executeUpdate();
                entriesInserted++;
            }
            i++;
            pstmt.close();
            result.close();
        }
        conn.close();
    }

    public static void fillCombviewTable() throws SQLException {
        //assembles table for combined view with source-specific processing
        //checks entries from each source table from newest by date to entriesLimit: filters unwanted words, looks for duplicates
        final int entriesLimit = 15;
        int entriesInserted = 0;
        //unwanted words list
        String[] filterWords = {};
        //clear table
        Connection conn = DriverManager.getConnection(DBtools.path);
        String sql = "DELETE FROM combview";
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);

        ArrayList<String> insertedSongs = new ArrayList<>();
        ArrayList<String> insertedDates = new ArrayList<>();
        //beatport
        //fill source array
        sql = "SELECT * FROM beatport ORDER BY date DESC";
        stmt = conn.createStatement();
        ResultSet RSinsertSongs = stmt.executeQuery(sql);
        cycle: while (RSinsertSongs.next()) {
            if (entriesInserted == entriesLimit)
                break;
            String songname = RSinsertSongs.getString("song");
            String artist = RSinsertSongs.getString("artist");
            String date = RSinsertSongs.getString("date");
            String songtype = RSinsertSongs.getString("type");
            //filtering words
            for (String checkword : filterWords) {
                if ((songtype.toLowerCase()).contains(checkword.toLowerCase()))
                    continue cycle;
                if ((songname.toLowerCase()).contains(checkword.toLowerCase()))
                    continue cycle;
            }
            //finding duplicates
            if (insertedSongs.contains(songname.toLowerCase())) {
                if (insertedDates.contains(date))
                    continue;
            }
            insertedSongs.add(songname.toLowerCase());
            insertedDates.add(date);
            //success: adding to combview table
            sql = "insert into combview(song, artist, date) values(?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, songname);
            pstmt.setString(2, artist);
            pstmt.setString(3, date);
            pstmt.executeUpdate();
            entriesInserted++;
        }
        entriesInserted = 0;

        //musicbrainz
        //fill source array
        sql = "SELECT * FROM musicbrainz ORDER BY date DESC";
        stmt = conn.createStatement();
        RSinsertSongs = stmt.executeQuery(sql);
        cycle: while (RSinsertSongs.next()) {
            if (entriesInserted == entriesLimit)
                break;
            String songname = RSinsertSongs.getString("song");
            String artist = RSinsertSongs.getString("artist");
            String date = RSinsertSongs.getString("date");
            //filtering words
            for (String checkword : filterWords) {
                if (songname.toLowerCase().contains(checkword.toLowerCase()))
                    continue cycle;
            }
            //finding duplicates
            if (insertedSongs.contains(songname.toLowerCase())) {
                if (insertedDates.contains(date))
                    continue;
            }
            insertedSongs.add(songname.toLowerCase());
            insertedDates.add(date);
            //success: adding to combview table
            sql = "insert into combview(song, artist, date) values(?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, songname);
            pstmt.setString(2, artist);
            pstmt.setString(3, date);
            pstmt.executeUpdate();
            entriesInserted++;
        }
        entriesInserted = 0;

        //junodownload
        //fill source array
        sql = "SELECT * FROM junodownload ORDER BY date DESC";
        stmt = conn.createStatement();
        RSinsertSongs = stmt.executeQuery(sql);
        cycle: while (RSinsertSongs.next()) {
            if (entriesInserted == entriesLimit)
                break;
            String songname = RSinsertSongs.getString("song");
            String artist = RSinsertSongs.getString("artist");
            String date = RSinsertSongs.getString("date");
            //filtering words
            for (String checkword : filterWords) {
                if (songname.toLowerCase().contains(checkword.toLowerCase()))
                    continue cycle;
            }
            //finding duplicates
            if (insertedSongs.contains(songname.toLowerCase())) {
                if (insertedDates.contains(date))
                    continue;
            }
            insertedSongs.add(songname.toLowerCase());
            insertedDates.add(date);
            //success: adding to combview table
            sql = "insert into combview(song, artist, date) values(?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, songname);
            pstmt.setString(2, artist);
            pstmt.setString(3, date);
            pstmt.executeUpdate();
            entriesInserted++;
        }
        entriesInserted = 0;
        conn.close();
        stmt.close();
        RSinsertSongs.close();
        insertedSongs.clear();
        insertedDates.clear();
    }

}