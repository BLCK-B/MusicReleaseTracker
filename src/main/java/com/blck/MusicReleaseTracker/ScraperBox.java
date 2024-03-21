package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Core.SourcesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.Scrapers.*;
import com.blck.MusicReleaseTracker.Simple.ErrorLogging;
import java.sql.*;
import java.util.Iterator;
import java.util.LinkedList;

public class ScraperBox {
    private final ValueStore store;
    private final ErrorLogging log;
    private final int initSize;
    private final LinkedList<ScraperParent> scrapers = new LinkedList<>();
    Iterator<ScraperParent> iter;

    // middleware abstraction for scraping with exception handling
    public ScraperBox(ValueStore store, ErrorLogging log) {
        this.store = store;
        this.log = log;
        // creating a list of scraper objects: one scraper holds one URL or null
        try (Connection conn = DriverManager.getConnection(store.getDBpath())) {
            String sql = "SELECT artistname FROM artists LIMIT 500";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet artistResults = pstmt.executeQuery();
            // cycling artists
            while (artistResults.next()) {
                String artist = artistResults.getString("artistname");
                // cycling sources
                for (SourcesEnum webSource : SourcesEnum.values()) {
                    sql = "SELECT * FROM artists WHERE artistname = ? LIMIT 100";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, artist);
                    ResultSet rs = pstmt.executeQuery();
                    String url = rs.getString("url" + webSource);
                    switch (webSource) {
                        case musicbrainz    -> scrapers.add(new MusicbrainzScraper(store, log, artist, url));
                        case beatport       -> scrapers.add(new BeatportScraper(store, log, artist, url));
                        case junodownload   -> scrapers.add(new JunodownloadScraper(store, log, artist, url));
                        case youtube        -> scrapers.add(new YoutubeScraper(store, log, artist, url));
                    }
                }
            }
            pstmt.close();
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error creating scrapers list");
        }
        initSize = scrapers.size();
        iter = scrapers.iterator();
    }

    public int getInitSize() {
        return initSize;
    }

    public int scrapeNext() {
        if (!iter.hasNext())
            return -1;

        double startTime = System.currentTimeMillis();
        ScraperParent scraper = iter.next();
        if (scraper == null)
            return scrapers.size();
        for (int i = 0; i <= 2; i++) {
            try {
                scraper.scrape();
                // will not break if exception
                break;
            }
            catch (ScraperTimeoutException e) {
                switch (i) {
                    case 0 -> log.error(e, ErrorLogging.Severity.INFO, scraper + " timed out");
                    case 1 -> log.error(e, ErrorLogging.Severity.INFO, scraper + " second time out");
                    default -> {
                        log.error(e, ErrorLogging.Severity.INFO, "final time out, disabling source " + scraper);
                        // remove all scrapers of faulty source
                        scrapers.removeIf(s -> s.getClass().equals(scraper.getClass()));
                    }
                }
                try {
                    Thread.sleep(2200);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        // calculated delay at the end of source cycle
        if (scrapers.size() % SourcesEnum.values().length == 0) {
            System.out.println(scraper.getClass());
            double elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime <= 2800) {
                try {
                    Thread.sleep((long) (2800 - elapsedTime));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        iter.remove();
        return scrapers.size();
    }
}
