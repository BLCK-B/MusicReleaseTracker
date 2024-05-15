package com.blck.MusicReleaseTracker.Scraping.Scrapers;

import com.blck.MusicReleaseTracker.Scraping.ScraperTimeoutException;

public interface ScraperInterface {

    void scrape(int timeout) throws ScraperTimeoutException;

    private void reduceToID() {}

    /** Returns (reduced) ID. */
    String getID();

}
