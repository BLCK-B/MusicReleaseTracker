package com.blck.MusicReleaseTracker.Scraping;

public class ScraperTimeoutException extends Exception {

    public ScraperTimeoutException(String url) {
        super(url);
    }

    @Override
    public String toString() {
        return "time out exception";
    }

}