module com.blck.MusicReleaseTracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jsoup;
    requires java.sql;
    requires typesafe.config;

    opens com.blck.MusicReleaseTracker to javafx.fxml;
    exports com.blck.MusicReleaseTracker;
    exports com.blck.MusicReleaseTracker.ModelsEnums;
    opens com.blck.MusicReleaseTracker.ModelsEnums to javafx.fxml;
}