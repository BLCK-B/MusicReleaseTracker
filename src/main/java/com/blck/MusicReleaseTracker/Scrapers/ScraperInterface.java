package com.blck.MusicReleaseTracker.Scrapers;

public interface ScraperInterface {

    void scrape();

    private void reduceToID() {};

    /** Returns reduced ID. */
    String reduceToID(String ID);


}
