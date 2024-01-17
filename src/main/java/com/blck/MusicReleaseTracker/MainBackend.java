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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
                throw new RuntimeException("error in DBtools path method", e);
            }
            try {
                DBtools.createTables();
            } catch (Exception e) {
                DBtools.logError(e, "SEVERE", "error in DBtools createTables method");
            }
            try {
                DBtools.updateSettings();
            } catch (Exception e) {
                DBtools.logError(e, "WARNING", "error handling config file");
            }
            //open port in web browser
            try {
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    String[] cmd = {"cmd.exe", "/c", "start", "http://localhost:8080"};
                    Runtime.getRuntime().exec(cmd);
                }
                else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                    String[] cmd = {"xdg-open", "http://localhost:8080"};
                    Runtime.getRuntime().exec(cmd);
                }
            } catch (Exception e) {
                DBtools.logError(e, "WARNING", "could not open port in browser");
            }
        }
    }

    public static boolean scrapeCancel = false;
    public static void scrapeData() throws SQLException, InterruptedException {
        DBtools.readConfig("longTimeout");
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
        sql = "DELETE FROM youtube";
        stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        pstmt.close();
        artistnameResults.close();
        conn.close();
        stmt.close();
        double progress = 0;
        //list for source urls (incl null) - one artist at a time
        ArrayList<String> eachArtistUrls = new ArrayList<>();
        //after 2 fail-try-again, dont scrape source anymore
        int brainzFails = 0;
        int beatportFails = 0;
        int junoFails = 0;
        int youtubeFails = 0;
        for (String songArtist : artistnameList) {
            conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
            eachArtistUrls.clear();
            String[] urlSources = {"urlbrainz", "urlbeatport", "urljunodownload", "urlyoutube"};
            for (String urlName : urlSources) {
                sql = "SELECT * FROM artists WHERE artistname = ? ";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, songArtist);
                ResultSet rs = pstmt.executeQuery();
                eachArtistUrls.add(rs.getString(urlName));
            }
            conn.close();
            pstmt.close();
            //rs.close();
            //calling scrapers
            int i = 1;
            double startTime = System.currentTimeMillis();
            for (String oneUrl : eachArtistUrls) {
                //if clicked cancel
                if (brainzFails == 2 && beatportFails == 2 && junoFails == 2 && youtubeFails == 2)
                    scrapeCancel = true;
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
                            case 1 -> {
                                if (brainzFails != 2)
                                    scrapeBrainz(oneUrl, songArtist);
                            }
                            case 2 -> {
                                if (beatportFails != 2)
                                    scrapeBeatport(oneUrl, songArtist);
                            }
                            case 3 -> {
                                if (junoFails != 2)
                                    scrapeJunodownload(oneUrl, songArtist);
                            }
                            case 4 -> {
                                if (youtubeFails != 2)
                                    scrapeYoutube(oneUrl,songArtist);
                            }
                        }
                    } catch (Exception e) {
                        //on fail, try once more
                        DBtools.logError(e, "INFO", "error scraping source " + i +", trying again");
                        Thread.sleep(2000);
                        try {
                            switch(i) {
                                case 1 -> scrapeBrainz(oneUrl, songArtist);
                                case 2 -> scrapeBeatport(oneUrl, songArtist);
                                case 3 -> scrapeJunodownload(oneUrl, songArtist);
                                case 4 -> scrapeYoutube(oneUrl,songArtist);
                            }
                        } catch (Exception e2) {
                            switch(i) {
                                case 1 -> brainzFails++;
                                case 2 -> beatportFails++;
                                case 3 -> junoFails++;
                                case 4 -> youtubeFails++;
                            }
                            DBtools.logError(e2, "WARNING", "error re-scraping, moving on");
                        }
                    }
                }
                //calculated delay at the end of every cycle
                if (i == 4) {
                    double endTime = System.currentTimeMillis();
                    double elapsedTime = (endTime - startTime);
                    if (2800 - elapsedTime >= 0)
                        Thread.sleep((long) (2800 - elapsedTime));
                }
                //calculating progressbar value
                progress++;
                //40 cycles (10 artists * 4 sources) / 20 total artists / 4 sources = 50%
                double state = progress / artistnameList.size() / 4;
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
        //extracting ID and creating link for API
        int startIndex = oneUrl.indexOf("/artist/");
        int endIndex = oneUrl.indexOf('/', startIndex + "/artist/".length());
        String artistID = null;
        if (endIndex != -1)
            artistID = oneUrl.substring(startIndex + "/artist/".length(), endIndex);
        else
            artistID = oneUrl.substring(startIndex + "/artist/".length());

        oneUrl = "https://musicbrainz.org/ws/2/release-group?artist=" + artistID + "&type=single&limit=400";
        //https://musicbrainz.org/ws/2/release-group?artist=773c3b3b-4368-4659-963a-4c8194ec9b1c&type=single&limit=400

        Document doc = null;
        try {
            doc = Jsoup.connect(oneUrl).userAgent("MusicReleaseTracker ( https://github.com/BLCK-B/MusicReleaseTracker )")
            .timeout(DBtools.settingsStore.getTimeout()).get();
        } catch (SocketTimeoutException e) {
            DBtools.logError(e, "INFO", "scrapeBrainz timed out " + oneUrl);
        }
        Elements songs = doc.select("title");
        Elements dates = doc.select("first-release-date");
        String[] songsArray = songs.eachText().toArray(new String[0]);
        String[] datesArray = dates.eachText().toArray(new String[0]);

        //create arraylist of song objects
        ArrayList<SongClass> songList = new ArrayList<SongClass>();
        for (int i = 0; i < Math.min(songsArray.length, datesArray.length); i++) {
            if (songsArray[i] != null && datesArray[i] != null)
                songList.add(new SongClass(songsArray[i], songArtist, datesArray[i]));
        }

        doc.empty();
        songs.clear();
        dates.clear();
        datesArray = null;
        songsArray = null;

        processInfo(songList, "musicbrainz");
    }

    public static void scrapeBeatport(String oneUrl, String songArtist) throws IOException {
        //scraper for beatport
        Document doc = null;
        try {
            doc = Jsoup.connect(oneUrl).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/")
            .timeout(DBtools.settingsStore.getTimeout()).get();
        } catch (SocketTimeoutException e) {
            DBtools.logError(e, "INFO", "scrapeBeatport timed out " + oneUrl);
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

        //create arraylist of song objects
        ArrayList<SongClass> songList = new ArrayList<SongClass>();
        for (int i = 0; i < Math.min(songsArrayList.size(), datesArrayList.size()); i++) {
            if (songsArrayList.get(i) != null && datesArrayList.get(i) != null && typesArrayList.get(i) != null)
                songList.add(new SongClass(songsArrayList.get(i), songArtist, datesArrayList.get(i), typesArrayList.get(i)));
        }

        script.clear();
        JSON = null;
        songsArrayList.clear();
        typesArrayList.clear();
        datesArrayList.clear();

        processInfo(songList, "beatport");
    }

    public static void scrapeJunodownload(String oneUrl, String songArtist) throws IOException {
        //scraper for junodownload
        Document doc = null;
        try {
            doc = Jsoup.connect(oneUrl).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/")
            .timeout(DBtools.settingsStore.getTimeout()).get();
        } catch (SocketTimeoutException e) {
            DBtools.logError(e, "INFO", "scrapeJunodownload timed out " + oneUrl);
        }
        Elements songs = doc.select("a.juno-title");
        Elements dates = doc.select("div.text-sm.text-muted.mt-3");
        String[] songsArray = songs.eachText().toArray(new String[0]);

        String[] datesArray = new String[dates.size()];
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
                DBtools.logError(e,"WARNING", "error processing junodownload date");
            }
        }

        //create arraylist of song objects
        ArrayList<SongClass> songList = new ArrayList<SongClass>();
        for (int i = 0; i < Math.min(songsArray.length, datesArray.length); i++) {
            if (songsArray[i] != null && datesArray[i] != null)
                songList.add(new SongClass(songsArray[i], songArtist, datesArray[i]));
        }

        songs.clear();
        dates.clear();
        songsArray = null;
        datesArray = null;

        processInfo(songList, "junodownload");
    }

    public static void scrapeYoutube(String oneUrl, String songArtist) throws IOException {
        //scraper for youtube
        oneUrl = "https://www.youtube.com/feeds/videos.xml?channel_id=" + oneUrl;

        Document doc = null;
        try {
            doc = Jsoup.connect(oneUrl).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/")
            .timeout(DBtools.settingsStore.getTimeout()).get();
        } catch (SocketTimeoutException e) {
            DBtools.logError(e, "INFO", "scrapeYoutube timed out " + oneUrl);
        }
        Elements songs = doc.select("title");
        Elements dates = doc.select("published");
        String[] songsArray = songs.eachText().toArray(new String[0]);
        String[] datesDirtyArray = dates.eachText().toArray(new String[0]);

        //cut date to yyyy-MM-dd
        String[] datesArray = Arrays.stream(datesDirtyArray)
                .map(date -> date.substring(0, 10))
                .toArray(String[]::new);
        //first index is channel name
        songsArray = Arrays.copyOfRange(songsArray, 1, songsArray.length);
        datesArray = Arrays.copyOfRange(datesArray, 1, datesArray.length);

        //create arraylist of song objects
        ArrayList<SongClass> songList = new ArrayList<SongClass>();
        for (int i = 0; i < Math.min(songsArray.length, datesArray.length); i++) {
            if (songsArray[i] != null && datesArray[i] != null)
                songList.add(new SongClass(songsArray[i], songArtist, datesArray[i]));
        }

        processInfo(songList, "youtube");
    }

    public static void processInfo(ArrayList<SongClass> songList, String source) {
        //unify apostrophes
        for (SongClass object : songList) {
            String songName = object.getName().replace("’", "'");
            object.setName(songName);
        }
        //discard objects with an incorrect date format
        songList.removeIf(obj -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            try {
                LocalDate.parse(obj.getDate(), formatter);
                return false;
            } catch (DateTimeParseException e) {
                return true;
            }
        });
        //sort by date from oldest
        songList.sort((obj1, obj2) -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date1 = LocalDate.parse(obj1.getDate(), formatter);
            LocalDate date2 = LocalDate.parse(obj2.getDate(), formatter);
            return date1.compareTo(date2);
        });
        //remove name duplicates
        Set<String> recordedNames = new HashSet<>();
        songList.removeIf(obj -> {
            String name = obj.getName().toLowerCase();
            if (recordedNames.contains(name))
                return true;
            else {
                recordedNames.add(name);
                return false;
            }
        });
        //reverse to newest-oldest
        Collections.reverse(songList);

        if (!source.equals("test"))
            insertSet(songList, source);
    }

    public static void insertSet(ArrayList<SongClass> songList, String source) {
        PreparedStatement pstmt = null;
        //insert a set of songs to a source table
        try {
            Connection conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
            int i = 0;
            for (SongClass songObject : songList) {
                if (i == 15)
                    break;
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
                i++;
            }
            conn.setAutoCommit(false);
            conn.commit();
            conn.setAutoCommit(true);
            conn.close();
        } catch (SQLException e) {
            DBtools.logError(e, "SEVERE", "error inserting a set of songs");
        }
    }

    public static void fillCombviewTable(String testPath) {
        //assembles table for combined view: filters unwanted words, looks for duplicates
        //load filterwords and entrieslimit
        if (testPath == null) {
            try {
                DBtools.readConfig("filters");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //clear table
        Connection conn = null;
        String sql = null;
        Statement stmt = null;
        try {
            if (testPath == null)
                conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
            else
                conn = DriverManager.getConnection(testPath);
            sql = "DELETE FROM combview";
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            conn.close();
        }  catch (Exception e) {
            DBtools.logError(e, "SEVERE", "error cleaning combview table");
        }

        String[] sourceTables = {"beatport", "musicbrainz", "junodownload", "youtube"};

        //creating song object list with data from all sources
        ArrayList<SongClass> songObjectList = new ArrayList<>();

        try {
            if (testPath == null)
                conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
            else
                conn = DriverManager.getConnection(testPath);
            for (String source : sourceTables) {
                sql = "SELECT * FROM " + source + " ORDER BY date DESC LIMIT 200";
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

                    //filtering user-selected keywords
                    if (testPath == null) {
                        for (String checkword : DBtools.settingsStore.getFilterWords()) {
                            if (songType != null) {
                                if ((songType.toLowerCase()).contains(checkword.toLowerCase()))
                                    continue RScycle;
                            }
                            if ((songName.toLowerCase()).contains(checkword.toLowerCase()))
                                continue RScycle;
                        }
                    }
                    else {
                        String checkword = "XXXXX";
                        if (songType != null) {
                            if ((songType.toLowerCase()).contains(checkword.toLowerCase()))
                                continue;
                        }
                        if ((songName.toLowerCase()).contains(checkword.toLowerCase()))
                            continue;
                    }

                    switch (source) {
                        case "beatport" -> songObjectList.add(new SongClass(songName, songArtist, songDate, songType));
                        case "musicbrainz", "junodownload", "youtube" -> songObjectList.add(new SongClass(songName, songArtist, songDate));
                    }
                }
            }
            conn.close();
        }  catch (Exception e) {
            DBtools.logError(e, "WARNING", "error in filtering keywords");
        }
        //map songObjectList to get rid of name-artist duplicates, prefer older, example key: neverenoughbensley
        //eg: Never Enough - Bensley - 2023-05-12 : Never Enough - Bensley - 2022-12-16 = Never Enough - Bensley - 2022-12-16
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, SongClass> nameArtistMap = songObjectList.stream()
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
                                DBtools.logError(e, "WARNING", "error in parsing dates");
                                return existingValue;
                            }
                        }
                ));
        //map nameArtistMap.values to merge name-date duplicates, example key: theoutlines2023-06-23
        //eg: The Outlines - Koven - 2023-06-23 : The Outlines - Circadian - 2023-06-23 = The Outlines - Circadian, Koven - 2023-06-23
        Map<String, SongClass> nameDateMap = nameArtistMap.values().stream()
                .collect(Collectors.toMap(
                        song -> song.getName().replaceAll("\\s+", "").toLowerCase() + song.getDate(),
                        song -> song,
                        (existingValue, newValue) -> {
                            //append artist from duplicate song to the already existing object in map
                            String newArtist = newValue.getArtist();
                            if (!existingValue.getArtist().contains(newArtist))
                                existingValue.appendArtist(newArtist);
                            return existingValue;
                        }
                ));
        //create a list of SongClass objects sorted by date from map
        List<SongClass> finalSortedList = nameDateMap.values().stream()
                .sorted(Comparator.comparing(SongClass::getDate, Comparator.reverseOrder()))
                .toList();

        nameDateMap.clear();
        nameArtistMap.clear();

        //insert data into table
        try {
            //precomitting batch insert is way faster
            if (testPath == null)
                conn = DriverManager.getConnection(DBtools.settingsStore.getDBpath());
            else
                conn = DriverManager.getConnection(testPath);
            sql = "insert into combview(song, artist, date) values(?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            int i = 0;
            for (SongClass item : finalSortedList) {
                if (i == 115)
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
            DBtools.logError(e, "SEVERE", "error inserting data to combview");
        }
        System.gc();
    }

}