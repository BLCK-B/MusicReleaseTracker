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
    exports com.blck.MusicReleaseTracker.ModelsEnums;
    opens com.blck.MusicReleaseTracker.ModelsEnums;
}

