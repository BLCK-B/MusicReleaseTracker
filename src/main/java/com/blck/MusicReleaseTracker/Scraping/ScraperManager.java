package com.blck.MusicReleaseTracker.Scraping;

import com.blck.MusicReleaseTracker.Core.SourcesEnum;
import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.*;

import java.util.*;

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
        for (SourcesEnum source : SourcesEnum.values())
            sourceTimes.put(source.toString(), 0.0);
    }
    public ScraperManager(ErrorLogging log, DBqueries DB, int customSleepTime) {
        this.log = log;
        this.DB = DB;
        minDelay = customSleepTime;
        for (SourcesEnum source : SourcesEnum.values())
            sourceTimes.put(source.toString(), 0.0);
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
