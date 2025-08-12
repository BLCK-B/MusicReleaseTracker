module com.blck.MusicReleaseTracker {
    requires org.jsoup;
    requires java.sql;
    requires spring.web;
    requires spring.core;
    requires spring.context;
    requires spring.beans;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.webmvc;
    requires org.xerial.sqlitejdbc;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;
    requires java.net.http;
    requires jakarta.annotation;

    opens com.blck.MusicReleaseTracker;
    opens com.blck.MusicReleaseTracker.Core;
    opens com.blck.MusicReleaseTracker.DataObjects;
    opens com.blck.MusicReleaseTracker.DB;
    opens com.blck.MusicReleaseTracker.FrontendAPI;
    opens com.blck.MusicReleaseTracker.JsonSettings;
    opens com.blck.MusicReleaseTracker.Scraping;

    exports com.blck.MusicReleaseTracker;
    exports com.blck.MusicReleaseTracker.Core;
    exports com.blck.MusicReleaseTracker.DataObjects;
    exports com.blck.MusicReleaseTracker.DB;
    exports com.blck.MusicReleaseTracker.FrontendAPI;
    exports com.blck.MusicReleaseTracker.JsonSettings;
    exports com.blck.MusicReleaseTracker.Scraping;
    exports com.blck.MusicReleaseTracker.Scraping.Thumbnails;
    opens com.blck.MusicReleaseTracker.Scraping.Thumbnails;
}

