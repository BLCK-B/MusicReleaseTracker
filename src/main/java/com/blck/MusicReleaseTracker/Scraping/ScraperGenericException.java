package com.blck.MusicReleaseTracker.Scraping;

public class ScraperGenericException extends Exception {

    private final String url;
    private final String errorMessage;

    public ScraperGenericException(String url, String message) {
        super(message);
        this.url = url;
        this.errorMessage = message;
    }

    @Override
    public String toString() {
        return "generic exception";
    }

    @Override
    public String getMessage() {
        return this.errorMessage;
    }

}
