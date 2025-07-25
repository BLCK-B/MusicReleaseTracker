/*
 *         MusicReleaseTracker
 *         Copyright (C) 2023 - 2025 BLCK
 *         This program is free software: you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation, either version 3 of the License, or
 *         (at your option) any later version.
 *         This program is distributed in the hope that it will be useful,
 *         but WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *         GNU General Public License for more details.
 *         You should have received a copy of the GNU General Public License
 *         along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.blck.MusicReleaseTracker.Scraping;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.TablesEnum;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.Scraper;

import java.util.HashMap;
import java.util.LinkedList;

public class ScraperManager {

    private final ErrorLogging log;
    private final DBqueries DB;
    private final int jsoupTimeout = 25000; // ms
    private final HashMap<String, Double> sourceTimes = new HashMap<>();
    private LinkedList<Scraper> scrapers;
    private int minDelay = 2800; // ms

    /**
     *
     * @param log error logging
     * @param DB database
     */
    public ScraperManager(ErrorLogging log, DBqueries DB) {
        this.log = log;
        this.DB = DB;
        for (TablesEnum source : TablesEnum.values()) {
            if (source == TablesEnum.combview)
                continue;
            sourceTimes.put(source.toString(), 0.0);
        }
    }

    /**
     * Custom minimum delay time for testing purposes.
     *
     * @param log error logging
     * @param DB database
     * @param customSleepTime ms
     */
    public ScraperManager(ErrorLogging log, DBqueries DB, int customSleepTime) {
        this.log = log;
        this.DB = DB;
        minDelay = customSleepTime;
        for (TablesEnum source : TablesEnum.values()) {
            if (source == TablesEnum.combview)
                continue;
            sourceTimes.put(source.toString(), 0.0);
        }
    }

    /**
     * Loads a new list of scrapers from DB.
     *
     * @return number of loaded scrapers
     */
    public int loadWithScrapers() {
        scrapers = DB.getAllScrapers();
        return scrapers.size();
    }

    /**
     * Initiate one scraping, jump to the next scraper. Applies dynamic blocking delay based on {@code minDelay}.
     *
     * @return scrapers remaining
     */
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

    /**
     * Handling of scraper fail cases.
     *
     * @param i retry attempt, {@code i == 2} removes all scrapers of this source from queue
     * @param scraper failing Scraper object
     * @param e exception thrown by {@code scraper}
     */
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

    /**
     * Calculates dynamic delay length based on last invocation and {@code minDelay}.
     *
     * @param source {@code scraper.toString()}
     * @return delay length, can be 0 if last invocation > {@code minDelay}
     */
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
