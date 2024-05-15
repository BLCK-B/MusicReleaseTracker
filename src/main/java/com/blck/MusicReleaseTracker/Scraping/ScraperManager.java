package com.blck.MusicReleaseTracker.Scraping;

import com.blck.MusicReleaseTracker.Core.SourcesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.DBqueries;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.*;

import java.util.*;

public class ScraperManager {
    private final ValueStore store;
    private final ErrorLogging log;
    private final DBqueries DB;
    private LinkedList<Scraper> scrapers;
    private final HashMap<String, Double> sourceTimes = new HashMap<>();

    // middleware abstraction for scraping with exception handling
    public ScraperManager(ValueStore store, ErrorLogging log, DBqueries DB) {
        this.store = store;
        this.log = log;
        this.DB = DB;
        for (SourcesEnum source : SourcesEnum.values()) {
            sourceTimes.put(source.toString(), 0.0);
        }
    }

    public int loadWithScrapers() {
        scrapers = DB.getAllScrapers();
        return scrapers.size();
    }

    public int scrapeNext() {
        if (scrapers.isEmpty())
            return -1;

        double startTime = System.currentTimeMillis();
        Scraper scraper = scrapers.getFirst();
        for (int i = 0; i <= 2; i++) {
            try {
                scraper.scrape(store.getTimeout());
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

        if (scraper.equals(scrapers.getFirst()))
            scrapers.removeFirst();

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
