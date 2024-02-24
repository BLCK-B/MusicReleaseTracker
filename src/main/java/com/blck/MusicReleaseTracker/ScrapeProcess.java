package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Scrapers.*;
import com.blck.MusicReleaseTracker.Simple.SSEController;
import com.blck.MusicReleaseTracker.Simple.SongClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
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

/** class with scraping and data processing logic */
@Component
public class ScrapeProcess {

    private final ValueStore store;
    private final ConfigTools config;
    private final DBtools DB;
    private final SSEController SSE;

    @Autowired
    public ScrapeProcess(ValueStore valueStore, ConfigTools configTools, DBtools DB, SSEController sseController) {
        this.store = valueStore;
        this.config = configTools;
        this.DB = DB;
        this.SSE = sseController;
    }

    public boolean scrapeCancel = false;
    public void scrapeData() throws SQLException, InterruptedException {
        config.readConfig("longTimeout");
        // calling method for scrapers, based on artist URLs
        scrapeCancel = false;
        // for each artistname: check all urls and load them into a list
        Connection conn = DriverManager.getConnection(store.getDBpath());
        String sql = "SELECT artistname FROM artists";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet artistnameResults = pstmt.executeQuery();
        ArrayList<String> artistNameList = new ArrayList<>();
        while (artistnameResults.next()) {
            artistNameList.add(artistnameResults.getString("artistname"));
        }
        // in case of no URLs
        if (artistNameList.isEmpty())
            return;
        // clear tables to prepare for new data
        for (String sourceTable : store.getSourceTables()) {
            sql = "DELETE FROM " + sourceTable;
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        }
        pstmt.close();
        artistnameResults.close();
        conn.close();
        double progress = 0;
        // creating a list of scraper objects artist by artist: one scraper holds one URL
        conn = DriverManager.getConnection(store.getDBpath());
        ArrayList<ScraperParent> scrapers = new ArrayList<ScraperParent>();
        for (String artist : artistNameList) {
            for (String webSource : store.getSourceTables()) {
                // selecting entire row
                sql = "SELECT * FROM artists WHERE artistname = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, artist);
                ResultSet rs = pstmt.executeQuery();
                String url = rs.getString("url" + webSource);
                switch (webSource) {
                    case "musicbrainz"   -> scrapers.add(new MusicbrainzScraper(store, DB, artist, url));
                    case "beatport"      -> scrapers.add(new BeatportScraper(store, DB, artist, url));
                    case "junodownload"  -> scrapers.add(new JunodownloadScraper(store, DB, artist, url));
                    case "youtube"       -> scrapers.add(new YoutubeScraper(store, DB, artist, url));
                }
            }
        }
        conn.close();
        // triggering scrapers
        int counter = 0;
        for (ScraperParent scraper : scrapers) {
            double startTime = System.currentTimeMillis();
            // if clicked cancel
            if (scrapeCancel) {
                SSE.sendProgress(1.0);
                System.gc();
                return;
            }
            for (int i = 0; i < 2; i++) {
                try {
                    scraper.scrape();
                } catch (Exception e) {
                    if (i == 1)
                        DB.logError(e, "INFO", "error scraping source " + scraper + ", trying again");
                    else
                        DB.logError(e, "WARNING", "error re-scraping source " + scraper + " moving on");
                    Thread.sleep(2000);
                }
            }
            // calculated delay at the end of every source cycle
            if (counter == 3) {
                double endTime = System.currentTimeMillis();
                double elapsedTime = endTime - startTime;
                if (2800 - elapsedTime >= 0)
                    Thread.sleep((long) (2800 - elapsedTime));
                counter = 0;
            }
            else
                counter++;
            // calculating progressbar value
            progress++;
            // 40 cycles (10 artists * 4 sources) / 20 total artists / 4 sources = 50%
            double state = progress / artistNameList.size() / 4;
            SSE.sendProgress(state);
        }
        System.gc();
    }

    public void fillCombviewTable(String testPath) {
        // assembles table for combined view: filters unwanted words, looks for duplicates
        // load filterwords and entrieslimit
        if (testPath == null) {
            try {
                config.readConfig("filters");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // clear table
        Connection conn = null;
        String sql = null;
        Statement stmt = null;
        try {
            if (testPath == null)
                conn = DriverManager.getConnection(store.getDBpath());
            else
                conn = DriverManager.getConnection(testPath);
            sql = "DELETE FROM combview";
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            conn.close();
        }  catch (Exception e) {
            DB.logError(e, "SEVERE", "error cleaning combview table");
        }

        // song object list with data from all sources
        ArrayList<SongClass> songObjectList = new ArrayList<>();

        try {
            if (testPath == null)
                conn = DriverManager.getConnection(store.getDBpath());
            else
                conn = DriverManager.getConnection(testPath);
            for (String source : store.getSourceTables()) {
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

                    // filtering user-selected keywords
                    if (testPath == null) {
                        for (String checkword : store.getFilterWords()) {
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
            DB.logError(e, "WARNING", "error in filtering keywords");
        }
        // map songObjectList to get rid of name-artist duplicates, prefer older, example key: neverenoughbensley
        // eg: Never Enough - Bensley - 2023-05-12 : Never Enough - Bensley - 2022-12-16 = Never Enough - Bensley - 2022-12-16
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
                                    return newValue;
                            } catch (ParseException e) {
                                DB.logError(e, "WARNING", "error in parsing dates");
                                return existingValue;
                            }
                        }
                ));
        // map nameArtistMap.values to merge name-date duplicates, example key: theoutlines2023-06-23
        // eg: The Outlines - Koven - 2023-06-23 : The Outlines - Circadian - 2023-06-23 = The Outlines - Circadian, Koven - 2023-06-23
        Map<String, SongClass> nameDateMap = nameArtistMap.values().stream()
                .collect(Collectors.toMap(
                        song -> song.getName().replaceAll("\\s+", "").toLowerCase() + song.getDate(),
                        song -> song,
                        (existingValue, newValue) -> {
                            // append artist from duplicate song to the already existing object in map
                            String newArtist = newValue.getArtist();
                            if (!existingValue.getArtist().contains(newArtist))
                                existingValue.appendArtist(newArtist);
                            return existingValue;
                        }
                ));
        // create a list of SongClass objects sorted by date from map
        List<SongClass> finalSortedList = nameDateMap.values().stream()
                .sorted(Comparator.comparing(SongClass::getDate, Comparator.reverseOrder()))
                .toList();

        nameDateMap.clear();
        nameArtistMap.clear();

        // insert data into table
        try {
            // precomitting batch insert is way faster
            if (testPath == null)
                conn = DriverManager.getConnection(store.getDBpath());
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
        } catch (Exception e) {
            DB.logError(e, "SEVERE", "error inserting data to combview");
        }
        System.gc();
    }

}