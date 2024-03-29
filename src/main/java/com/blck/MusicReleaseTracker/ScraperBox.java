package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Core.SourcesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.Scrapers.*;
import com.blck.MusicReleaseTracker.Simple.ErrorLogging;
import java.sql.*;
import java.util.*;

public class ScraperBox {
    private final ValueStore store;
    private final ErrorLogging log;
    private final int initSize;
    private final LinkedList<ScraperParent> scrapers = new LinkedList<>();
    private final HashMap<String, Double> sourceTimes = new HashMap<>();

    // middleware abstraction for scraping with exception handling
    public ScraperBox(ValueStore store, ErrorLogging log) {
        this.store = store;
        this.log = log;
        // creating a list of scraper objects: one scraper holds one URL
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
                    if (url == null)
                        continue;
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
        for (SourcesEnum source : SourcesEnum.values()) {
            sourceTimes.put(source.toString(), 0.0);
        }
    }

    public int getInitSize() {
        return initSize;
    }

    public int scrapeNext() {
        if (scrapers.isEmpty())
            return -1;

        double startTime = System.currentTimeMillis();
        ScraperParent scraper = scrapers.get(0);
        for (int i = 0; i <= 2; i++) {
            try {
                scraper.scrape();
                break; // exception = will not break
            }
            catch (ScraperTimeoutException e) {
                switch (i) {
                    case 0 -> log.error(e, ErrorLogging.Severity.INFO, scraper + " timed out");
                    case 1 -> log.error(e, ErrorLogging.Severity.INFO, scraper + " second time out");
                    default -> {
                        log.error(e, ErrorLogging.Severity.INFO, "final time out, disabling source " + scraper);
                        // remove all scrapers of a faulty source
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
        double elapsedTime = System.currentTimeMillis() - startTime;
        sourceTimes.replaceAll((key, value) -> value + elapsedTime);
        delays(scraper.toString());

        if (scraper.equals(scrapers.get(0)))
            scrapers.remove(0);

        return scrapers.size();
    }

    public void delays(String source) {
        // every source has an enforced min delay
        double timeLastScrape = sourceTimes.get(source);
        if (timeLastScrape < 2800) {
            long waitTime = (long) (2800 - timeLastScrape);
            try {
                Thread.sleep(waitTime);
                sourceTimes.replaceAll((key, value) -> value + waitTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        sourceTimes.replace(source, 0.0);
    }
}
