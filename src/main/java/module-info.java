module com.blck.MusicReleaseTracker {
    requires org.jsoup;
    requires java.sql;
    requires typesafe.config;
    requires spring.web;
    requires spring.context;
    requires spring.beans;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.webmvc;

    opens com.blck.MusicReleaseTracker;

    exports com.blck.MusicReleaseTracker;
    exports com.blck.MusicReleaseTracker.Core;
    opens com.blck.MusicReleaseTracker.Core;
    opens com.blck.MusicReleaseTracker.Scraping;
    exports com.blck.MusicReleaseTracker.Scraping;
    exports com.blck.MusicReleaseTracker.FrontendAPI;
    opens com.blck.MusicReleaseTracker.FrontendAPI;
    exports com.blck.MusicReleaseTracker.DataModels;
    opens com.blck.MusicReleaseTracker.DataModels;
}

