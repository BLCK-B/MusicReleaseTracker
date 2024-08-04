module com.blck.MusicReleaseTracker {
    requires org.jsoup;
    requires java.sql;
    requires spring.web;
    requires spring.context;
    requires spring.beans;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.webmvc;
    requires org.xerial.sqlitejdbc;
    requires com.fasterxml.jackson.databind;

    opens com.blck.MusicReleaseTracker;

    exports com.blck.MusicReleaseTracker;
    exports com.blck.MusicReleaseTracker.Core;
    opens com.blck.MusicReleaseTracker.Core;
    opens com.blck.MusicReleaseTracker.Scraping;
    exports com.blck.MusicReleaseTracker.Scraping;
    exports com.blck.MusicReleaseTracker.FrontendAPI;
    opens com.blck.MusicReleaseTracker.FrontendAPI;
    exports com.blck.MusicReleaseTracker.DataObjects;
    opens com.blck.MusicReleaseTracker.DataObjects;
    exports com.blck.MusicReleaseTracker.DB;
    opens com.blck.MusicReleaseTracker.DB;
}

