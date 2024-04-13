package com.blck.MusicReleaseTracker.Scrapers;

public class ScraperTimeoutException extends Exception {

    public ScraperTimeoutException(String url) {
        super(url);
    }

}