package com.blck.MusicReleaseTracker.Scraping;

public class ScraperGenericException extends Exception {

    public ScraperGenericException(String url) {
        super(url);
    }

}
