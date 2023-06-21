module com.blck.MusicReleaseTracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jsoup;
    requires java.sql;

    opens com.blck.MusicReleaseTracker to javafx.fxml;
    exports com.blck.MusicReleaseTracker;
}