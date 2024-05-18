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
    private int sleepTime = 2800;
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
        sleepTime = customSleepTime;
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
        for (int i = 0; i <= 2; i++) {
            try {
                scraper.scrape(sleepTime);
                break; // exception = will not break
            }
            catch (ScraperTimeoutException e) {
                switch (i) {
                    case 0 -> log.error(e, ErrorLogging.Severity.INFO, scraper + " time out");
                    case 1 -> log.error(e, ErrorLogging.Severity.INFO, scraper + " second time out");
                    default -> {
                        log.error(e, ErrorLogging.Severity.INFO, "final time out, disabling source " + scraper);
                        // remove all scrapers of a faulty source
                        scrapers.removeIf(s -> s.getClass().equals(scraper.getClass()));
                    }
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
            catch (Exception e) {
                switch (i) {
                    case 0 -> log.error(e, ErrorLogging.Severity.WARNING, scraper + " error of scraper");
                    case 1 -> log.error(e, ErrorLogging.Severity.WARNING, scraper + " second error of scraper");
                    default -> {
                        log.error(e, ErrorLogging.Severity.WARNING, "final error of scraper, disabling source " + scraper);
                        // remove all scrapers of a faulty source
                        scrapers.removeIf(s -> s.getClass().equals(scraper.getClass()));
                    }
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
            if (scrapers.isEmpty())
                return -1;
        }
        double elapsedTime = System.currentTimeMillis() - startTime;
        sourceTimes.replaceAll((key, value) -> value + elapsedTime);
        if (sleepTime != 0)
            delays(scraper.toString());

        if (scraper.equals(scrapers.getFirst()))
            scrapers.removeFirst();

        return scrapers.isEmpty() ? -1 : scrapers.size();
    }

    public void delays(String source) {
        // every source has an enforced min delay
        double timeLastScrape = sourceTimes.get(source);
        if (timeLastScrape < sleepTime) {
            long waitTime = (long) (sleepTime - timeLastScrape);
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
