package com.blck.MusicReleaseTracker.Scraping.Scrapers;

import com.blck.MusicReleaseTracker.Scraping.ScraperGenericException;
import com.blck.MusicReleaseTracker.Scraping.ScraperTimeoutException;

public interface ScraperInterface {

    void scrape(int timeout) throws ScraperTimeoutException, ScraperGenericException;

    /** not meant to discard wrong input, instead reduces to id when possible */
    private void reduceToID() {}

    /** Returns (reduced) ID. */
    String getID();

}
