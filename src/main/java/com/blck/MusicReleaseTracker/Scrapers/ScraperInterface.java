package com.blck.MusicReleaseTracker.Scrapers;

import java.net.SocketTimeoutException;

public interface ScraperInterface {

    void scrape() throws ScraperTimeoutException;

    private void reduceToID() {};

    /** Returns (reduced) ID. */
    String getID();

}
