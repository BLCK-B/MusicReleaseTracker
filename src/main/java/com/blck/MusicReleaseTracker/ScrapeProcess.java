package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.Scrapers.*;
import com.blck.MusicReleaseTracker.Simple.ErrorLogging;
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
    private final ErrorLogging log;
    private final ConfigTools config;
    private final DBtools DB;
    private final SSEController SSE;

    @Autowired
    public ScrapeProcess(ValueStore valueStore, ErrorLogging errorLogging, ConfigTools configTools, DBtools DB, SSEController sseController) {
        this.store = valueStore;
        this.log = errorLogging;
        this.config = configTools;
        this.DB = DB;
        this.SSE = sseController;
    }

    public boolean scrapeCancel = false;
    public void scrapeData() {
        config.readConfig(ConfigTools.configOptions.longTimeout);
        scrapeCancel = false;
        // clear tables to prepare for new data
        DB.clearDB();
        // creating a list of scraper objects: one scraper holds one URL
        ArrayList<ScraperParent> scrapers = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(store.getDBpath())) {
            String sql = "SELECT artistname FROM artists";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet artistResults = pstmt.executeQuery();
            // cycling artists
            while (artistResults.next()) {
                String artist = artistResults.getString("artistname");
                // cycling sources
                for (String webSource : store.getSourceTables()) {
                    sql = "SELECT * FROM artists WHERE artistname = ?";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, artist);
                    ResultSet rs = pstmt.executeQuery();
                    String url = rs.getString("url" + webSource);
                    switch (webSource) {
                        case "musicbrainz" -> scrapers.add(new MusicbrainzScraper(store, log, artist, url));
                        case "beatport" -> scrapers.add(new BeatportScraper(store, log, artist, url));
                        case "junodownload" -> scrapers.add(new JunodownloadScraper(store, log, artist, url));
                        case "youtube" -> scrapers.add(new YoutubeScraper(store, log, artist, url));
                    }
                }
            }
            pstmt.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error creating scrapers list");
        }
        // triggering scrapers
        double progress = 0;
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
                    break;
                }
                catch (ScraperTimeoutException e) {
                    if (i == 1)
                        log.error(e, ErrorLogging.Severity.INFO, scraper + " timed out " + e);
                    else
                        log.error(e, ErrorLogging.Severity.INFO, scraper + " second time out " + e + ", moving on");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            // calculated delay at the end of source cycle
            if (scraper instanceof YoutubeScraper) {
                double elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime <= 2800) {
                    try {
                        Thread.sleep((long) (2800 - elapsedTime));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            progress++;
            // 40 cycles / 80 scrapers = 50%
            double state = progress / scrapers.size();
            SSE.sendProgress(state);
        }
        scrapers = null;
        System.gc();
    }

    public void fillCombviewTable() {
        // assembles table for combined view: filters unwanted words, looks for duplicates
        // load filterwords and entrieslimit
        if (!store.getDBpath().contains("testing"))
            config.readConfig(ConfigTools.configOptions.filters);
        // clear table
        String sql = null;
        Statement stmt = null;
        try (Connection conn = DriverManager.getConnection(store.getDBpath())) {
            sql = "DELETE FROM combview";
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        }  catch (Exception e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error cleaning combview table");
        }

        // song object list with data from all sources
        ArrayList<SongClass> songObjectList = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(store.getDBpath())) {
            for (String source : store.getSourceTables()) {
                sql = "SELECT * FROM " + source + " ORDER BY date DESC LIMIT 200";
                stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    String songName = rs.getString("song");
                    String songArtist = rs.getString("artist");
                    String songDate = rs.getString("date");
                    String songType = null;
                    if (source.equals("beatport"))
                        songType = rs.getString("type");

                    if (filterWords(songName, songType)) {
                        switch (source) {
                            case "beatport" -> songObjectList.add(new SongClass(songName, songArtist, songDate, songType));
                            case "musicbrainz", "junodownload", "youtube" -> songObjectList.add(new SongClass(songName, songArtist, songDate));
                        }
                    }
                }
            }
        }  catch (Exception e) {
            log.error(e, ErrorLogging.Severity.WARNING, "error filtering keywords");
        }
        // map songObjectList to get rid of name-artist duplicates, prefer older, example key: neverenoughbensley
        // eg: Never Enough - Bensley - 2023-05-12 : Never Enough - Bensley - 2022-12-16 = Never Enough - Bensley - 2022-12-16
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, SongClass> nameArtistMap = songObjectList.stream()
                .collect(Collectors.toMap(
                        song -> song.getName().replaceAll("\\s+", "").toLowerCase() + song.getArtist().replaceAll("\\s+", "").toLowerCase(),
                        song -> song, (existingValue, newValue) -> {
                            try {
                                Date existingDate = dateFormat.parse(existingValue.getDate());
                                Date newDate = dateFormat.parse(newValue.getDate());
                                if (existingDate.compareTo(newDate) < 0)
                                    return existingValue;
                                else
                                    return newValue;
                            } catch (ParseException e) {
                                log.error(e, ErrorLogging.Severity.WARNING, "error in parsing dates");
                                return existingValue;
                            }
                        }
                ));
        // map nameArtistMap.values to merge name-date duplicates, example key: theoutlines2023-06-23
        // eg: The Outlines - Koven - 2023-06-23 : The Outlines - Circadian - 2023-06-23 = The Outlines - Circadian, Koven - 2023-06-23
        Map<String, SongClass> nameDateMap = nameArtistMap.values().stream()
                .collect(Collectors.toMap(
                        song -> song.getName().replaceAll("\\s+", "").toLowerCase() + song.getDate(),
                        song -> song, (existingValue, newValue) -> {
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
        try (Connection conn = DriverManager.getConnection(store.getDBpath())) {
            sql = "insert into combview(song, artist, date) values(?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            // precomitting batch insert is way faster
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

            songObjectList = null;
            stmt.close();
            pstmt.close();
        } catch (Exception e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error inserting data to combview");
        }
        System.gc();
    }

    public boolean filterWords(String songName, String songType) {
        // filtering user-selected keywords
        for (String checkword : store.getFilterWords()) {
            if (songType != null) {
                if ((songType.toLowerCase()).contains(checkword.toLowerCase()))
                    return false;
            }
            if ((songName.toLowerCase()).contains(checkword.toLowerCase()))
                return false;
        }
        return true;
    }

}