package com.blck.MusicReleaseTracker.Scraping;

public class ScraperTimeoutException extends Exception {

    public ScraperTimeoutException(String url) {
        super(url);
    }

}