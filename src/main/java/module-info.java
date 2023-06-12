module com.blck.musictrackergradle {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jsoup;
    requires java.sql;

    opens com.blck.musictrackergradle to javafx.fxml;
    exports com.blck.musictrackergradle;
}