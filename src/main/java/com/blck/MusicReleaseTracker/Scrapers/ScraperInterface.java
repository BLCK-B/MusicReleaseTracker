package com.blck.MusicReleaseTracker.Scrapers;

public interface ScraperInterface {

    void scrape() throws ScraperTimeoutException;

    private void reduceToID() {}

    /** Returns (reduced) ID. */
    String getID();

}
