package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.ModelsEnums.MonthNumbers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

public class MainBackend {

    private static final SSEController sseController = new SSEController();
    private static GUIController GUIController;

    @Component
    public static class StartupRunner implements CommandLineRunner {
        //on startup of springboot server
        @Override
        public void run(String... args) {
            System.out.println("----------LOCAL SERVER STARTED----------");
            System.out.println("  __  __ ____ _____ \n" +
                    " |  \\/  |  _ \\_   _|\n" +
                    " | |\\/| | |_) || |  \n" +
                    " | |  | |  _ < | |  \n" +
                    " |_|  |_|_| \\_\\|_|  \n");

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
            //open port in web browser
            try {
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    String[] cmd = {"cmd.exe", "/c", "start", "http://localhost:8080"};
                    Runtime.getRuntime().exec(cmd);
                }
                else if (os.contains("nix")) {
                    String[] cmd = {"/usr/bin/open", "http://localhost:8080"};
                    Runtime.getRuntime().exec(cmd);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //entryLimit: how many entries per artist
    private static final int entryLimit = 15;
    public static boolean scrapeCancel = false;

    public static void scrapeData() throws SQLException, InterruptedException {
        //calling method for scrapers, based on artist URLs
        scrapeCancel = false;
        //for each artistname: check all urls and load them into a list
        Connection conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
        String sql = "SELECT artistname FROM artists";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet artistnameResults = pstmt.executeQuery();
        ArrayList<String> artistnameList = new ArrayList<>();
        while (artistnameResults.next()) {
            artistnameList.add(artistnameResults.getString("artistname"));
        }
        //in case of no URLs
        if (artistnameList.isEmpty())
            return;
        //clear tables to prepare for new data
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
        double progress = 0;
        //list for source urls (incl null) - one artist at a time
        ArrayList<String> eachArtistUrls = new ArrayList<>();
        for (String songArtist : artistnameList) {
            conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
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
            double startTime = System.currentTimeMillis();
            for (String oneUrl : eachArtistUrls) {
                //if clicked cancel
                if (scrapeCancel) {
                    SSEController.sendProgress(1.0);
                    eachArtistUrls.clear();
                    artistnameList.clear();
                    System.gc();
                    return;
                }
                //cycling scrapers
                if (oneUrl != null) {
                    try {
                        switch(i) {
                            case 1 -> scrapeBrainz(oneUrl, songArtist);
                            case 2 -> scrapeBeatport(oneUrl, songArtist);
                            case 3 -> scrapeJunodownload(oneUrl, songArtist);
                        }
                    } catch (Exception e) {
                        //on fail, try once more
                        System.out.println("error scraping source" + oneUrl + ",trying again");
                        try {
                            switch(i) {
                                case 1 -> scrapeBrainz(oneUrl, songArtist);
                                case 2 -> scrapeBeatport(oneUrl, songArtist);
                                case 3 -> scrapeJunodownload(oneUrl, songArtist);
                            }
                        } catch (Exception e2) {
                            System.out.println("error re-scraping, moving on");
                            e2.printStackTrace();
                        }
                    }
                }
                //calculated delay at the end of every cycle
                if (i == 3) {
                    double endTime = System.currentTimeMillis();
                    double elapsedTime = (endTime - startTime);
                    if (2700 - elapsedTime >= 0)
                        Thread.sleep((long) (2700 - elapsedTime));
                }
                //calculating progressbar value
                progress++;
                double state = progress / artistnameList.size() / 3;
                i++;
                SSEController.sendProgress(state);
            }
        }
        eachArtistUrls.clear();
        artistnameList.clear();
        System.gc();
    }

    public static void scrapeBrainz(String oneUrl, String songArtist) throws IOException {
        //scraper for musicbrainz
        Document doc = null;
        try {
            doc = Jsoup.connect(oneUrl).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/").timeout(40000).get();
        } catch (SocketTimeoutException e) {
            System.out.println("scrapeBrainz timed out " + oneUrl);
            return;
        }
        Elements songs = doc.select("[href*=/release/]");
        Elements dates = doc.select("ul.release-events > li:first-child").select("span.release-date");

        String[] songsDirtyArray = songs.eachText().toArray(new String[0]);
        String[] datesDirtyArray = dates.eachText().toArray(new String[0]);
        String[] typesArray = null;

        ArrayList<String> songsArrayList = new ArrayList<>();
        ArrayList<String> datesArrayList = new ArrayList<>();

        //avoid songs without a date - and preliminarily duplicates while ensuring newer rereleases dont make the cut
        ArrayList<String> insertedSongs = new ArrayList<>();
        int i = 0;
        while (i < datesDirtyArray.length) {
            if (datesDirtyArray[i].length() != 10 || insertedSongs.contains(songsDirtyArray[i])) {
                i++;
                continue;
            }
            else
                insertedSongs.add(songsDirtyArray[i].replace("’", "'"));

            songsArrayList.add(songsDirtyArray[i].replace("’", "'"));
            datesArrayList.add(datesDirtyArray[i]);
            i++;
        }

        doc.empty();
        insertedSongs.clear();

        Collections.reverse(songsArrayList);
        Collections.reverse(datesArrayList);
        String[] songsArray = songsArrayList.toArray(new String[0]);
        String[] datesArray = datesArrayList.toArray(new String[0]);

        fillSongClassList(songsArray, datesArray, typesArray, songArtist, "musicbrainz");

        songs.clear();
        dates.clear();
        songsArray = null;
        datesArray = null;
    }

    public static void scrapeBeatport(String oneUrl, String songArtist) throws IOException {
        //scraper for beatport
        Document doc = null;
        try {
            doc = Jsoup.connect(oneUrl).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/").timeout(40000).get();
        } catch (SocketTimeoutException e) {
            System.out.println("scrapeBeatport timed out " + oneUrl);
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
            songsArrayList.add(matcher.group(2).replace("\\u0026", "&").replace("’", "'"));
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

    public static void scrapeJunodownload(String oneUrl, String songArtist) throws IOException {
        //scraper for junodownload
        Document doc = null;
        try {
            doc = Jsoup.connect(oneUrl).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/").timeout(40000).get();
        } catch (SocketTimeoutException e) {
            System.out.println("scrapeJunodownload timed out " + oneUrl);
            return;
        }
        Elements songs = doc.select("a.juno-title");
        Elements dates = doc.select("div.text-sm.mb-3.mb-lg-3");
        String[] songsArray = songs.eachText().toArray(new String[0]);
        for (int i = 0; i < songsArray.length; i++) {
            songsArray[i] = songsArray[i].replace("’", "'");
        }
        String[] datesArray = new String[dates.size()];
        String[] typesArray = null;
        doc.empty();
        //processing dates into correct format
        /* example:
            <div class="text-sm mb-3 mb-lg-3">
             LIQ 202
             <br>
             28 Jun 23
             <br>
             Drum &amp; Bass / Jungle
            </div>
        */
        for (int i = 0; i < dates.size(); i++) {
            try {
                String cleanWhitespace = String.valueOf(dates.get(i))
                        .replaceAll("<br>", " ")
                        .replaceAll("\\s+", " ")
                        .trim();
                //cleanWhitespace: <div class="text-sm mb-3 mb-lg-3"> LIQ 202 28 Jun 23 Drum &amp; Bass / Jungle </div>

                Pattern pattern = Pattern.compile("\\b (\\d{1,2} [A-Za-z]{3} \\d{2}) \\b");
                Matcher matcher = pattern.matcher(cleanWhitespace);
                String extractedDate = null;
                if (matcher.find())
                    extractedDate = matcher.group(1);
                //extractedDate: 28 Jun 23

                String[] parts = extractedDate.split(" ");
                MonthNumbers monthEnum = MonthNumbers.valueOf(parts[1].toUpperCase());
                String monthNumber = monthEnum.getCode();
                //only assuming songs from 21st century
                datesArray[i] = "20" + parts[2] + "-" + monthNumber + "-" + parts[0];
                //datesArray[i]: 2023-06-28
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
        //fills SongClassList with song objects, up to entryLimit
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

            songName = songsArray[i];
            songDate = datesArray[i];
            if (typesArray != null)
                songType = typesArray[i];
            i++;
            //avoid duplicates
            if (previousNames.contains(songName.toLowerCase()))
                continue;

            if (typesArray == null)
                SongClassList.add(new SongClass(songName, songArtist, songDate));
            else if (typesArray != null)
                SongClassList.add(new SongClass(songName, songArtist, songDate, songType));
            //keep track of added
            previousNames.add(songName.toLowerCase());
            inserted++;
        }
        songsArray = null;
        datesArray = null;
        typesArray = null;

        //pass objects to insert data into source table
        insertSet(SongClassList, source);
    }

    public static void insertSet(ArrayList<SongClass> SongClassList, String source) {
        PreparedStatement pstmt = null;
        //insert set of songs to a source table
        try {
            Connection conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
            for (SongClass songObject : SongClassList) {
                if (songObject.getType() != null) {
                    String sql = "insert into " + source + "(song, artist, date, type) values(?, ?, ?, ?)";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, songObject.getName());
                    pstmt.setString(2, songObject.getArtist());
                    pstmt.setString(3, songObject.getDate());
                    pstmt.setString(4, songObject.getType());
                }
                else {
                    String sql = "insert into " + source + "(song, artist, date) values(?, ?, ?)";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, songObject.getName());
                    pstmt.setString(2, songObject.getArtist());
                    pstmt.setString(3, songObject.getDate());
                }
                pstmt.executeUpdate();
            }
            conn.setAutoCommit(false);
            conn.commit();
            conn.setAutoCommit(true);
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void fillCombviewTable() {
        //assembles table for combined view: filters unwanted words, looks for duplicates, sorts by date, other processing
        //load filterwords and entrieslimit
        try {
            DBtools.readCombviewConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int entriesInserted = 0;
        int entriesLimit = DBtools.settingsStore.getEntriesLimit();
        //clear table
        Connection conn = null;
        String sql = null;
        Statement stmt = null;
        try {
            conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
            sql = "DELETE FROM combview";
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            conn.close();
        }  catch (Exception e) {
            e.printStackTrace();
        }

        String[] sourceTables = {"beatport", "musicbrainz", "junodownload"};

        //creating song object list from all sources
        ArrayList<SongClass> songObjectList = new ArrayList<>();

        try {
            conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
            for (String source : sourceTables) {
                sql = "SELECT * FROM " + source + " ORDER BY date DESC LIMIT " + entriesLimit * 5;
                stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                RScycle: while (rs.next()) {
                    String songName = rs.getString("song");
                    String songArtist = rs.getString("artist");
                    String songDate = rs.getString("date");
                    String songType = null;
                    if (source.equals("beatport")) {
                        songType = rs.getString("type");
                    }

                    //filtering user-defined keywords
                    for (String checkword : DBtools.settingsStore.getFilterWords()) {
                        if (songType != null) {
                            if ((songType.toLowerCase()).contains(checkword.toLowerCase()))
                                continue RScycle;
                        }
                        if ((songName.toLowerCase()).contains(checkword.toLowerCase()))
                            continue RScycle;
                    }

                    switch (source) {
                        case "beatport" -> songObjectList.add(new SongClass(songName, songArtist, songDate, songType));
                        case "musicbrainz", "junodownload" -> songObjectList.add(new SongClass(songName, songArtist, songDate));
                    }
                }
            }
            conn.close();
        }  catch (Exception e) {
            e.printStackTrace();
        }

        //map songObjectList to get rid of name-date duplicates, example key: ascension2023-06-13
        //eg: The Outlines - Koven - 2023-06-23 : The Outlines - Circadian - 2023-06-23 = The Outlines - Circadian, Koven - 2023-06-23
        Map<String, SongClass> nameDateMap = songObjectList.stream()
                .collect(Collectors.toMap(
                        song -> song.getName().replaceAll("\\s+", "").toLowerCase() + song.getDate(),
                        song -> song,
                        (existingValue, newValue) -> {
                            //append artist from duplicate song to the already existing object in map
                            existingValue.appendArtist(newValue.getArtist());
                            return existingValue;
                        }
                ));
        //map nameDateMap.values to get rid of name-artist duplicates, example key: ascensionkoansound
        //eg: Never Enough - Bensley - 2023-05-12 : Never Enough - Bensley - 2022-12-16 = Never Enough - Bensley - 2022-12-16
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, SongClass> nameArtistMap = nameDateMap.values().stream()
                .collect(Collectors.toMap(
                        song -> song.getName().replaceAll("\\s+", "").toLowerCase() + song.getArtist().replaceAll("\\s+", "").toLowerCase(),
                        song -> song,
                        (existingValue, newValue) -> {
                            try {
                                Date existingDate = dateFormat.parse(existingValue.getDate());
                                Date newDate = dateFormat.parse(newValue.getDate());
                                if (existingDate.compareTo(newDate) < 0)
                                    return existingValue;
                                else
                                    return  newValue;
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        }
                ));

        //create a list of SongClass objects sorted by date from map
        List<SongClass> finalSortedList = nameArtistMap.values().stream()
                .sorted(Comparator.comparing(SongClass::getDate, Comparator.reverseOrder()))
                .toList();

        nameDateMap.clear();
        nameArtistMap.clear();

        //insert data into table
        try {
            //precomitting batch insert is way faster
            conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
            sql = "insert into combview(song, artist, date) values(?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            int i = 0;
            for (SongClass item : finalSortedList) {
                if (i == entriesLimit * sourceTables.length)
                    break;
                pstmt.setString(1, item.getName());
                pstmt.setString(2, item.getArtist());
                pstmt.setString(3, item.getDate());
                pstmt.addBatch();
                i++;
            }
            conn.setAutoCommit(false);
            pstmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            pstmt.clearBatch();

            songObjectList.clear();
            stmt.close();
            pstmt.close();
            conn.close();
        }  catch (Exception e) {
            e.printStackTrace();
        }
        System.gc();
    }

}