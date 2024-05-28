package com.blck.MusicReleaseTracker.Scraping;

import com.blck.MusicReleaseTracker.Core.SourcesEnum;
import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.*;

import java.util.*;

public class ScraperManager {

    private final ErrorLogging log;
    private final DBqueries DB;
    private LinkedList<Scraper> scrapers;
    private int minDelay = 2800; // ms
    private final int jsoupTimeout = 25000; // ms
    private final HashMap<String, Double> sourceTimes = new HashMap<>();

    public ScraperManager(ErrorLogging log, DBqueries DB) {
        this.log = log;
        this.DB = DB;
        for (SourcesEnum source : SourcesEnum.values()) {
            sourceTimes.put(source.toString(), 0.0);
        }
    }
    public ScraperManager(ErrorLogging log, DBqueries DB, int customSleepTime) {
        this.log = log;
        this.DB = DB;
        minDelay = customSleepTime;
        for (SourcesEnum source : SourcesEnum.values()) {
            sourceTimes.put(source.toString(), 0.0);
        }
    }

    public int loadWithScrapers() {
        scrapers = DB.getAllScrapers();
        return scrapers.size();
    }

    public int scrapeNext() {
        double startTime = System.currentTimeMillis();
        Scraper scraper = scrapers.getFirst();
        for (int i = 0; i <= 2; ++i) {
            try {
                scraper.scrape(jsoupTimeout);
                break;
            } catch (Exception e) {
                scrapeErrorLaunder(i, scraper, e);
            }
            if (scrapers.isEmpty())
                return 0;
        }
        double elapsedTime = System.currentTimeMillis() - startTime;
        sourceTimes.replaceAll((key, value) -> value + elapsedTime);
        try {
            Thread.sleep(delays(scraper.toString()));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (scraper.equals(scrapers.getFirst()))
            scrapers.removeFirst();

        return scrapers.size();
    }

    private void scrapeErrorLaunder(int i, Scraper scraper, Exception e) {
        if (e instanceof ScraperTimeoutException)
            log.error(e, ErrorLogging.Severity.INFO, scraper + " scraper threw " + e + " " + i + " times");
        else
            log.error(e, ErrorLogging.Severity.WARNING, scraper + " scraper threw " + e + " " + i + " times");
        // remove scrapers of a faulty source
        if (i == 2)
            scrapers.removeIf(s -> s.getClass().equals(scraper.getClass()));
        try {
            Thread.sleep(minDelay);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public long delays(String source) {
        long waitTime = (long) (minDelay - sourceTimes.get(source));
        if (waitTime > 0) {
            sourceTimes.replaceAll((key, value) -> value + waitTime);
            return waitTime;
        }
        sourceTimes.replace(source, 0.0);
        return 0;
    }
}
