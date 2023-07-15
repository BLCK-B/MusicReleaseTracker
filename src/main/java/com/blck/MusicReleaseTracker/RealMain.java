package com.blck.MusicReleaseTracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/blck/MusicReleaseTracker/mygui.fxml"));
        Parent root = loader.load();
        GUIController = loader.getController();
        Scene scene = new Scene(root);
        Image icon = new Image(getClass().getResourceAsStream("/MRTlogo.png"));
        primaryStage.getIcons().add(icon);
        primaryStage.setTitle("MusicReleaseTracker");
        primaryStage.setHeight(680);
        primaryStage.setWidth(800);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        try {
            DBtools.path();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            DBtools.createTables();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            DBtools.updateSettingsDB();
        } catch (Exception e) {
            System.out.println("error handling config file");
            e.printStackTrace();
        }
        launch(args);
    }

    //entryLimit: how many entries per artist
    private static final int entryLimit = 15;
    public static boolean scrapeCancel = false;

    public static void scrapeData() throws SQLException, InterruptedException {
        scrapeCancel = false;
        //for each artistname: check 3 urls and load them into a list
        Connection conn = DriverManager.getConnection(DBtools.DBpath);
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
        ListView<String> artistList = new ListView<>();
        for (String artistnamerow : artistnameList) {
            GUIController.currentlyScrapedArtist(artistnamerow);
            conn = DriverManager.getConnection(DBtools.DBpath);
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
            for (String oneurl : eachArtistUrls) {
                if (scrapeCancel) {
                    eachArtistUrls.clear();
                    artistnameList.clear();
                    GUIController.removeScrapedCss();
                    System.gc();
                    return;
                }

                switch (i) {
                    case 1 -> {
                        if (oneurl != null) {
                            try {
                                scrapeBrainz(oneurl, artistnamerow);
                            } catch (Exception e) {
                                System.out.println("error scraping musicbrainz - trying again");
                                try {
                                    scrapeBrainz(oneurl, artistnamerow);
                                } catch (Exception e2) {
                                    System.out.println("error re-scraping musicbrainz, moving on");
                                    e2.printStackTrace();
                                }
                            }
                        }
                    }
                    case 2 -> {
                        if (oneurl != null) {
                            try {
                                scrapeBeatport(oneurl, artistnamerow);
                            } catch (Exception e) {
                                System.out.println("error scraping beatport - trying again");
                                try {
                                    scrapeBeatport(oneurl, artistnamerow);
                                } catch (Exception e2) {
                                    System.out.println("erorr re-scraping beatport, moving on");
                                    e2.printStackTrace();
                                }
                            }
                        }
                    }
                    case 3 -> {
                        if (oneurl != null) {
                            try {
                                scrapeJunodownload(oneurl, artistnamerow);
                            } catch (Exception e) {
                                System.out.println("error scraping junodownload - trying again");
                                try {
                                    scrapeBeatport(oneurl, artistnamerow);
                                } catch (Exception e2) {
                                    System.out.println("error re-scraping junodownload, moving on");
                                    e2.printStackTrace();
                                }
                            }
                        }
                        Thread.sleep(1200);
                    }
                }
                i++;
            }
            progress++;
            double state = progress / artistnameList.size();
            GUIController.updateProgressBar(state);
        }
        GUIController.removeScrapedCss();
        eachArtistUrls.clear();
        artistnameList.clear();
        System.gc();
    }

    private static void scrapeBrainz(String oneurl, String artistnamerow) throws IOException, SQLException {
        //scraper for musicbrainz
        Document doc = null;
        try {
            doc = Jsoup.connect(oneurl).timeout(40000).get();
        } catch (SocketTimeoutException e) {
            System.out.println("scrapeBrainz timed out " + oneurl);
        }
        Elements songs = doc.select("[href*=/release/]");
        Elements dates = doc.select("ul.release-events > li:first-child").select("span.release-date");
        String[] songsArray = songs.eachText().toArray(new String[0]);
        String[] datesArray = dates.eachText().toArray(new String[0]);
        doc.empty();
        Connection conn = DriverManager.getConnection(DBtools.DBpath);
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
        songs.clear();
        dates.clear();
        songsArray = null;
        datesArray = null;
    }

    private static void scrapeBeatport(String oneurl, String artistnamerow) throws IOException, SQLException {
        //scraper for beatport
        Document doc = null;
        try {
            doc = Jsoup.connect(oneurl).timeout(40000).get();
        } catch (SocketTimeoutException e) {
            System.out.println("scrapeBeatport timed out " + oneurl);
        }
        //pattern matching to make sense of the mess that is JSON extracted from script
        Elements script = doc.select("script#__NEXT_DATA__[type=application/json]");
        String JSON = script.first().data();
        Pattern pattern = Pattern.compile(
                "\"mix_name\"\\s*:\\s*\"([^\"]+)\",\\s*" +
                        "\"name\"\\s*:\\s*\"([^\"]+)\",\\s*" +
                        "\"new_release_date\"\\s*:\\s*\"([^\"]+)\""
        );
        Matcher matcher = pattern.matcher(JSON);
        List<String> typeArray = new ArrayList<>();
        List<String> songsArray = new ArrayList<>();
        List<String> datesArray = new ArrayList<>();
        while (matcher.find()) {
            typeArray.add(matcher.group(1));
            songsArray.add(matcher.group(2).replace("\\u0026", "&"));
            datesArray.add(matcher.group(3));
        }
        doc.empty();

        Connection conn = DriverManager.getConnection(DBtools.DBpath);
        //fill table
        int entriesInserted = 0;
        int i = 0;
        mainloop: while (entriesInserted < entryLimit) {
            if (i == songsArray.size())
                break;
            String songname = songsArray.get(i);
            String songdate = datesArray.get(i);
            String songtype = typeArray.get(i);
            //checking if rereleased
            for (int x = songsArray.size() - 1; x > i; x--) {
                if (songname.equals(songsArray.get(x))) {
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
        script.clear();
        JSON = null;
        songsArray = null;
        typeArray = null;
        datesArray = null;
    }

    private static void scrapeJunodownload(String oneurl, String artistnamerow) throws IOException, SQLException {
        //scraper for junodownload
        Document doc = null;
        try {
            doc = Jsoup.connect(oneurl).timeout(40000).get();
        } catch (SocketTimeoutException e) {
            System.out.println("scrapeJunodownload timed out " + oneurl);
        }
        Elements songs = doc.select("a.juno-title");
        Elements dates = doc.select("div.text-sm.mb-3.mb-lg-3");
        String[] songsArray = songs.eachText().toArray(new String[0]);
        doc.empty();
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
        Connection conn = DriverManager.getConnection(DBtools.DBpath);
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
        conn.close();
        songs.clear();
        dates.clear();
        songsArray = null;
        datesArray = null;
    }

    public static void fillCombviewTable() throws SQLException {
        //assembles table for combined view with source-specific processing
        //checks entries from each source table from newest by date to entriesLimit: filters unwanted words, looks for duplicates
        int entriesInserted = 0;
        //clear table
        Connection conn = DriverManager.getConnection(DBtools.DBpath);
        String sql = "DELETE FROM combview";
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);

        ArrayList<String> insertedSongs = new ArrayList<>();
        ArrayList<String> insertedDates = new ArrayList<>();

        try {
            DBtools.readCombviewConfig();
        } catch (Exception e) {
            System.out.println("Error loading combview config: " + e);
        }

        //beatport
        //fill source array
        sql = "SELECT * FROM beatport ORDER BY date DESC";
        stmt = conn.createStatement();
        ResultSet RSinsertSongs = stmt.executeQuery(sql);
        cycle: while (RSinsertSongs.next()) {
            if (entriesInserted == DBtools.entriesLimit)
                break;
            String songname = RSinsertSongs.getString("song");
            String artist = RSinsertSongs.getString("artist");
            String date = RSinsertSongs.getString("date");
            String songtype = RSinsertSongs.getString("type");
            //filtering words
            for (String checkword : DBtools.filterWords) {
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
            if (entriesInserted == DBtools.entriesLimit)
                break;
            String songname = RSinsertSongs.getString("song");
            String artist = RSinsertSongs.getString("artist");
            String date = RSinsertSongs.getString("date");
            //filtering words
            for (String checkword : DBtools.filterWords) {
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
            if (entriesInserted == DBtools.entriesLimit)
                break;
            String songname = RSinsertSongs.getString("song");
            String artist = RSinsertSongs.getString("artist");
            String date = RSinsertSongs.getString("date");
            //filtering words
            for (String checkword : DBtools.filterWords) {
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
        System.gc();
    }

}