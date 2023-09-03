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
import java.util.*;
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
        if (artistnameList.isEmpty())
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
        for (String songArtist : artistnameList) {
            GUIController.currentlyScrapedArtist(songArtist);
            conn = DriverManager.getConnection(DBtools.DBpath);
            eachArtistUrls.clear();
            sql = "SELECT urlbrainz FROM artists WHERE artistname = ? ";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, songArtist);
            ResultSet RSeachArtistUrls = pstmt.executeQuery();
            eachArtistUrls.add(RSeachArtistUrls.getString("urlbrainz"));
            sql = "SELECT urlbeatport FROM artists WHERE artistname = ? ";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, songArtist);
            RSeachArtistUrls = pstmt.executeQuery();
            eachArtistUrls.add(RSeachArtistUrls.getString("urlbeatport"));
            sql = "SELECT urljunodownload FROM artists WHERE artistname = ? ";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, songArtist);
            RSeachArtistUrls = pstmt.executeQuery();
            eachArtistUrls.add(RSeachArtistUrls.getString("urljunodownload"));
            conn.close();
            pstmt.close();
            RSeachArtistUrls.close();
            //calling scrapers
            int i = 1;
            for (String oneurl : eachArtistUrls) {
                //if clicked cancel
                if (scrapeCancel) {
                    eachArtistUrls.clear();
                    artistnameList.clear();
                    GUIController.removeScrapedCss();
                    System.gc();
                    return;
                }
                //cycling sources
                if (oneurl != null) {
                    try {
                        switch(i) {
                            case 1 -> scrapeBrainz(oneurl, songArtist);
                            case 2 -> scrapeBeatport(oneurl, songArtist);
                            case 3 -> scrapeJunodownload(oneurl, songArtist);
                        }
                    } catch (Exception e) {
                        //on fail, try once more
                        System.out.println("error scraping source" + i + ",trying again (1=musicbrainz 2=beatport 3=junodownload)");
                        try {
                            switch(i) {
                                case 1 -> scrapeBrainz(oneurl, songArtist);
                                case 2 -> scrapeBeatport(oneurl, songArtist);
                                case 3 -> scrapeJunodownload(oneurl, songArtist);
                            }
                        } catch (Exception e2) {
                            System.out.println("error re-scraping source" + i + ", moving on");
                            e2.printStackTrace();
                        }
                    }
                    if (i == 3)
                        Thread.sleep(1600);
                }
                progress++;
                double state = progress / artistnameList.size() / 3;
                GUIController.updateProgressBar(state);
                i++;
            }
        }
        GUIController.removeScrapedCss();
        eachArtistUrls.clear();
        artistnameList.clear();
        System.gc();
    }

    private static void scrapeBrainz(String oneurl, String songArtist) throws IOException {
        //scraper for musicbrainz
        Document doc = null;
        try {
            doc = Jsoup.connect(oneurl).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/").timeout(40000).get();
        } catch (SocketTimeoutException e) {
            System.out.println("scrapeBrainz timed out " + oneurl);
            return;
        }
        Elements songs = doc.select("[href*=/release/]");
        Elements dates = doc.select("ul.release-events > li:first-child").select("span.release-date");
        String[] songsArray = songs.eachText().toArray(new String[0]);
        String[] datesArray = dates.eachText().toArray(new String[0]);
        String[] typesArray = null;
        doc.empty();

        Collections.reverse(Arrays.asList(songsArray));
        Collections.reverse(Arrays.asList(datesArray));

        fillSongClassList(songsArray, datesArray, typesArray, songArtist, "musicbrainz");

        songs.clear();
        dates.clear();
        songsArray = null;
        datesArray = null;
    }

    private static void scrapeBeatport(String oneurl, String songArtist) throws IOException {
        //scraper for beatport
        Document doc = null;
        try {
            doc = Jsoup.connect(oneurl).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/").timeout(40000).get();
        } catch (SocketTimeoutException e) {
            System.out.println("scrapeBeatport timed out " + oneurl);
            return;
        }
        //pattern matching to make sense of the JSON extracted from <script>
        Elements script = doc.select("script#__NEXT_DATA__[type=application/json]");
        String JSON = script.first().data();
        Pattern pattern = Pattern.compile(
                "\"mix_name\"\\s*:\\s*\"([^\"]+)\",\\s*" +
                        "\"name\"\\s*:\\s*\"([^\"]+)\",\\s*" +
                        "\"new_release_date\"\\s*:\\s*\"([^\"]+)\""
        );
        Matcher matcher = pattern.matcher(JSON);
        ArrayList<String> typesArrayList = new ArrayList<>();
        ArrayList<String> songsArrayList = new ArrayList<>();
        ArrayList<String> datesArrayList = new ArrayList<>();

        while (matcher.find()) {
            typesArrayList.add(matcher.group(1));
            songsArrayList.add(matcher.group(2).replace("\\u0026", "&"));
            datesArrayList.add(matcher.group(3));
        }
        doc.empty();
        //converting lists to arrays, to pass them to universal method
        String[] typesArray = typesArrayList.toArray(new String[0]);
        String[] songsArray = songsArrayList.toArray(new String[0]);
        String[] datesArray = datesArrayList.toArray(new String[0]);

        fillSongClassList(songsArray, datesArray, typesArray, songArtist, "beatport");

        script.clear();
        JSON = null;
        songsArrayList.clear();
        typesArrayList.clear();
        datesArrayList.clear();
        datesArray = null;
        songsArray = null;
        typesArray = null;
    }

    private static void scrapeJunodownload(String oneurl, String songArtist) throws IOException {
        //scraper for junodownload
        Document doc = null;
        try {
            doc = Jsoup.connect(oneurl).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/").timeout(40000).get();
        } catch (SocketTimeoutException e) {
            System.out.println("scrapeJunodownload timed out " + oneurl);
            return;
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
        String[] typesArray = null;
        //loop over the dates, format them
        for (int i = 0; i < dates.size(); i++) {
            try {
                String dateStr = dates.get(i).outerHtml().substring(dates.get(i).outerHtml().indexOf("<br>") + 4, dates.get(i).outerHtml().lastIndexOf("<br>")).trim();
                String[] parts = dateStr.split(" ");
                String month = parts[1];
                String monthNumber = monthMap.get(month);
                String result = "20" + parts[2] + "-" + monthNumber + "-" + parts[0];
                datesArray[i] = result;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        fillSongClassList(songsArray, datesArray, typesArray, songArtist, "junodownload");

        songs.clear();
        dates.clear();
        songsArray = null;
        datesArray = null;
    }

    public static void fillSongClassList(String[] songsArray, String[] datesArray, String[] typesArray, String songArtist, String source) {
        //loop that fills SongClassList with song objects, up to entryLimit
        ArrayList<SongClass> SongClassList = new ArrayList<>();
        ArrayList<String> previousNames = new ArrayList<>();
        int inserted = 0;
        int i = 0;
        while (inserted < entryLimit) {
            if (i == songsArray.length)
                break;

            String songName = null;
            String songDate = null;
            String songType = null;
            if (source.equals("musicbrainz") || (source.equals("junodownload"))) {
                songName = songsArray[i];
                songDate = datesArray[i];
            }
            else if (source.equals("beatport")) {
                songName = songsArray[i];
                songDate = datesArray[i];
                songType = typesArray[i];
            }

            i++;
            //avoid duplicates
            if (previousNames.contains(songName.toLowerCase()))
                continue;

            if (source.equals("musicbrainz") || (source.equals("junodownload"))) {
                SongClassList.add(new SongClass(songName, songArtist, songDate));
            }
            else if (source.equals("beatport")) {
                SongClassList.add(new SongClass(songName, songArtist, songDate, songType));
            }
            //keep track of added
            previousNames.add(songName.toLowerCase());
            inserted++;
        }
        //pass objects to insert data into source table
        insertSet(SongClassList, source);

        songsArray = null;
        datesArray = null;
    }

    public static void insertSet(ArrayList<SongClass> SongClassList, String source) {
        //insert set of songs to a source table
        try {
            Connection conn = DriverManager.getConnection(DBtools.DBpath);
            for (SongClass songObject : SongClassList) {
                try {
                    if (songObject.getType() != null) {
                        String sql = "insert into " + source + "(song, artist, date, type) values(?, ?, ?, ?)";
                        PreparedStatement pstmt = conn.prepareStatement(sql);
                        pstmt.setString(1, songObject.getName());
                        pstmt.setString(2, songObject.getArtist());
                        pstmt.setString(3, songObject.getDate());
                        pstmt.setString(4, songObject.getType());
                        pstmt.executeUpdate();
                    }
                    else {
                        String sql = "insert into " + source + "(song, artist, date) values(?, ?, ?)";
                        PreparedStatement pstmt = conn.prepareStatement(sql);
                        pstmt.setString(1, songObject.getName());
                        pstmt.setString(2, songObject.getArtist());
                        pstmt.setString(3, songObject.getDate());
                        pstmt.executeUpdate();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void fillCombviewTable() throws SQLException {
        //assembles table for combined view with source-specific processing
        //checks entries from each source table from newest by date until entriesLimit: filters unwanted words, looks for duplicates
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
                if ((songtype.toLowerCase()).contains(checkword.toLowerCase())) {
                    //beatport has comprehensive song types while others not
                    insertedSongs.add(songname.toLowerCase());
                    insertedDates.add(date);
                    continue cycle;
                }
                if ((songname.toLowerCase()).contains(checkword.toLowerCase()))
                    continue cycle;
            }
            //finding duplicates
            for (String oneSong : insertedSongs) {
                if (oneSong.contains(songname.toLowerCase())) {
                    if (insertedDates.contains(date))
                        continue cycle;
                }
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
            for (String oneSong : insertedSongs) {
                if (oneSong.contains(songname.toLowerCase())) {
                    if (insertedDates.contains(date))
                        continue cycle;
                }
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
            for (String oneSong : insertedSongs) {
                if (oneSong.contains(songname.toLowerCase())) {
                    if (insertedDates.contains(date))
                        continue cycle;
                }
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