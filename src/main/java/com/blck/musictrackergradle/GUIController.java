package com.blck.musictrackergradle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.blck.musictrackergradle.RealMain.fillCombviewTable;
import static com.blck.musictrackergradle.RealMain.scrapeData;

/*      MusicReleaseTrcker
        Copyright (C) 2023 BLCK
        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.
        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.
        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>.*/

public class GUIController {
    @FXML
    public ListView<String> artistList;
    @FXML
    public Button addButton;
    @FXML
    public Button deleteButton;
    @FXML
    public StackPane addWindow;
    @FXML
    public Button addconfirmButton;
    @FXML
    public Button addcancelButton;
    @FXML
    public TextField artistInputField;
    @FXML
    public StackPane brainzUrlDiag;
    @FXML
    public TextField brainzUrlBar;
    @FXML
    public Button brainzUrlButton;
    @FXML
    public StackPane beatportUrlDiag;
    @FXML
    public TextField beatportUrlBar;
    @FXML
    public Button beatportUrlButton;
    @FXML
    public StackPane junodownloadUrlDiag;
    @FXML
    public TextField junodownloadUrlbar;
    @FXML
    public Button junodownloadUrlButton;
    @FXML
    public Button settingsButton;
    @FXML
    public Button refreshButton;
    @FXML
    public Button combviewButton;
    @FXML
    public Button beatportButton;
    @FXML
    public Button brainzButton;
    @FXML
    public Button junodownloadButton;
    @FXML
    public Button settingsCloseButton;
    @FXML
    public StackPane settingsWindow;
    @FXML
    public Hyperlink linkMusicbrainz;
    @FXML
    public Hyperlink linkBeatport;
    @FXML
    public Hyperlink linkJunodownload;
    @FXML
    public ImageView cancelButton;
    @FXML
    private ProgressBar progressbar;
    @FXML
    private TableView<TableModelcombview> combviewTable;
    @FXML
    public TableColumn<TableModelcombview, String> songColCombview;
    @FXML
    public TableColumn<TableModelcombview, String> artistColCombview;
    @FXML
    public TableColumn<TableModelcombview, String> dateColCombview;
    @FXML
    private TableView<TableModel> mainTable;
    @FXML
    public TableColumn<TableModel, String> songCol;
    @FXML
    public TableColumn<TableModel, String> dateCol;

    private final ObservableList<String> dataList = FXCollections.observableArrayList();
    private final ObservableList<TableModel> dataTable = FXCollections.observableArrayList();
    private final ObservableList<TableModelcombview> dataTablecombview = FXCollections.observableArrayList();
    private ListCell<String> lastClickedCell = null;
    private String lastClickedArtist = null;
    private String selectedSource = null;

    public void updateProgressBar(double state) {
        Platform.runLater(() -> progressbar.setProgress(state));
    }

    public ProgressBar getProgressBar() {
        return progressbar;
    }

    @FXML
    public void initialize() throws SQLException {
        loadList();
        combviewButton.getStyleClass().add("filterclicked");
        loadcombviewTable();
    }

    public void loadList() throws SQLException {
        dataList.clear();
        //populating artist list - from table "artists"
        Connection conn = DriverManager.getConnection(DBtools.path);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT artistname FROM artists");
        Set<String> set = new HashSet<>();
        while (rs.next()) {
            set.add(rs.getString("artistname"));
        }
        dataList.addAll(set);
        artistList.setItems(dataList.sorted());
        conn.close();
        stmt.close();
        rs.close();

        //selecting artists in list
        artistList.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String cellcontent, boolean empty) {
            super.updateItem(cellcontent, empty);
            if (empty || cellcontent == null) {
                setText(null);
                setOnMousePressed(null);
            } else {
                setText(cellcontent);
                setOnMousePressed (event -> {
                        try {
                            artistClick(cellcontent);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        if (lastClickedCell != null) {
                            lastClickedCell.getStyleClass().remove("selected-row");
                        }
                        getStyleClass().add("selected-row");
                        lastClickedCell = this;
                        lastClickedArtist = cellcontent;
                });
            }
            }

        });
    }

    public void loadTable() throws SQLException {
        dataTable.clear();
        hideWindows();
        combviewTable.setVisible(false);
        //url existence check
        Connection conn = DriverManager.getConnection(DBtools.path);
        String sql = null;
        switch (selectedSource) {
            case "musicbrainz" -> sql = "SELECT urlbrainz FROM artists WHERE artistname = ? ";
            case "beatport" -> sql = "SELECT urlbeatport FROM artists WHERE artistname = ? ";
            case "junodownload" -> sql = "SELECT urljunodownload FROM artists WHERE artistname = ? ";
        }
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, lastClickedArtist);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            String link = rs.getString(1);
            conn.close();
            if (link == null || link.isEmpty()) {
                switch (selectedSource) {
                    case "musicbrainz" -> brainzUrlDiag.setVisible(true);
                    case "beatport" -> beatportUrlDiag.setVisible(true);
                    case "junodownload" -> junodownloadUrlDiag.setVisible(true);
                }
                return;
            }
        }

        //populating table - for given artist and given source - launched every list and source click
        conn = DriverManager.getConnection(DBtools.path);
        switch (selectedSource) {
            case "musicbrainz" -> sql = "SELECT song, date FROM musicbrainz WHERE artist = ? ORDER BY date DESC";
            case "beatport" -> sql = "SELECT song, date FROM beatport WHERE artist = ? ORDER BY date DESC";
            case "junodownload" -> sql = "SELECT song, date FROM junodownload WHERE artist = ? ORDER BY date DESC";
        }
        pstmt =conn.prepareStatement(sql);
        pstmt.setString(1, lastClickedArtist);
        rs = pstmt.executeQuery();
        // Loop through the result set and add each row to the data list
        while (rs.next()) {
            String col1Value = rs.getString("song");
            String col2Value = rs.getString("date");
            dataTable.add(new TableModel(col1Value, col2Value));
        }
        songCol.setCellValueFactory(new PropertyValueFactory<>("column1"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("column2"));
        // Set the data to the table view
        mainTable.setItems(dataTable);
        conn.close();
        pstmt.close();
        rs.close();
    }
    public void loadcombviewTable() throws SQLException {
        combviewTable.setVisible(true);
        dataTablecombview.clear();
        Connection conn = DriverManager.getConnection(DBtools.path);
        //populating combview table
        String sql = "SELECT * FROM combview ORDER BY date DESC LIMIT 20";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        // Loop through the result set and add each row to the data list
        while (rs.next()) {
            String col1Value = rs.getString("song");
            String col2Value = rs.getString("artist");
            String col3Value = rs.getString("date");
            dataTablecombview.add(new TableModelcombview(col1Value, col2Value, col3Value));
        }
        songColCombview.setCellValueFactory(new PropertyValueFactory<>("column1"));
        artistColCombview.setCellValueFactory(new PropertyValueFactory<>("column2"));
        dateColCombview.setCellValueFactory(new PropertyValueFactory<>("column3"));
        // Set the data to the table view
        combviewTable.setItems(dataTablecombview);
        conn.close();
        pstmt.close();
        rs.close();
    }

    public void clickAdd(MouseEvent mouseEvent) {
        hideWindows();
        addWindow.setVisible(true);
        combviewButton.getStyleClass().remove("filterclicked");
        brainzButton.getStyleClass().remove("filterclicked");
        beatportButton.getStyleClass().remove("filterclicked");
        junodownloadButton.getStyleClass().remove("filterclicked");
    }
    public void clickAddConfirm(MouseEvent mouseEvent) throws SQLException {
        //add new artist typed by user
        String userInput = artistInputField.getText();
        artistInputField.clear();
        if (userInput.isEmpty() || userInput.isBlank())
            return;
        hideWindows();
        Connection conn = DriverManager.getConnection(DBtools.path);
        String sql = "insert into artists(artistname) values(?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, userInput);
        pstmt.executeUpdate();
        conn.close();
        pstmt.close();
        loadList();
        dataTable.clear();
        mainTable.setItems(dataTable);
        lastClickedArtist = null;
    }
    public void clickAddCancel(MouseEvent mouseEvent) {
        hideWindows();
        artistInputField.clear();
    }
    public void clickDelete(MouseEvent mouseEvent) throws SQLException {
        //delete last selected artist and all entries from artist
        if (lastClickedArtist != null) {
            Connection conn = DriverManager.getConnection(DBtools.path);
            String sql = "DELETE FROM artists WHERE artistname = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, lastClickedArtist);
            pstmt.executeUpdate();
            sql = "DELETE FROM musicbrainz WHERE artist = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, lastClickedArtist);
            pstmt.executeUpdate();
            sql = "DELETE FROM beatport WHERE artist = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, lastClickedArtist);
            pstmt.executeUpdate();
            sql = "DELETE FROM junodownload WHERE artist = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, lastClickedArtist);
            pstmt.executeUpdate();
            conn.close();
            pstmt.close();
            loadList();
            hideWindows();
            fillCombviewTable();
            loadcombviewTable();
            combviewButton.getStyleClass().add("filterclicked");
            brainzButton.getStyleClass().remove("filterclicked");
            beatportButton.getStyleClass().remove("filterclicked");
            junodownloadButton.getStyleClass().remove("filterclicked");
        }
    }

    private void artistClick(String cellcontent) throws SQLException {
        combviewButton.getStyleClass().remove("filterclicked");
        lastClickedArtist = cellcontent;
        if (selectedSource != null)
            loadTable();
    }

    public void clickComb(MouseEvent mouseEvent) throws SQLException {
        combviewButton.getStyleClass().remove("filterclicked");
        brainzButton.getStyleClass().remove("filterclicked");
        beatportButton.getStyleClass().remove("filterclicked");
        junodownloadButton.getStyleClass().remove("filterclicked");
        combviewButton.getStyleClass().add("filterclicked");
        if (lastClickedCell != null) {
            lastClickedCell.getStyleClass().remove("selected-row");
            lastClickedArtist = null;
            selectedSource = null;
            hideWindows();
        }
        loadcombviewTable();
    }

    public void clickBrainz(MouseEvent mouseEvent) throws SQLException {
        brainzButton.getStyleClass().remove("filterclicked");
        combviewButton.getStyleClass().remove("filterclicked");
        beatportButton.getStyleClass().remove("filterclicked");
        junodownloadButton.getStyleClass().remove("filterclicked");
        brainzButton.getStyleClass().add("filterclicked");
        selectedSource = "musicbrainz";
        if (lastClickedArtist != null) {
            loadTable();
        }
    }
    public void clickPort(MouseEvent mouseEvent) throws SQLException {
        beatportButton.getStyleClass().remove("filterclicked");
        combviewButton.getStyleClass().remove("filterclicked");
        brainzButton.getStyleClass().remove("filterclicked");
        junodownloadButton.getStyleClass().remove("filterclicked");
        beatportButton.getStyleClass().add("filterclicked");
        selectedSource = "beatport";
        if (lastClickedArtist != null) {
            loadTable();
        }
    }
    public void clickJunodownload(MouseEvent mouseEvent) throws SQLException {
        junodownloadButton.getStyleClass().remove("filterclicked");
        combviewButton.getStyleClass().remove("filterclicked");
        brainzButton.getStyleClass().remove("filterclicked");
        beatportButton.getStyleClass().remove("filterclicked");
        junodownloadButton.getStyleClass().add("filterclicked");
        selectedSource = "junodownload";
        if (lastClickedArtist != null) {
            loadTable();
        }
    }
    public void clickBrainzUrlButton(MouseEvent mouseEvent) throws SQLException {
        String sql = "UPDATE artists SET urlbrainz = ? WHERE artistname = ?";
        String userInput = brainzUrlBar.getText();
        if (userInput.isEmpty() || userInput.isBlank())
            return;
        //reduce to base form then modify
        int artistIndex = userInput.indexOf("/artist/");
        if (artistIndex != -1 && userInput.contains("musicbrainz.org")) {
            int artistIdIndex = userInput.indexOf('/', artistIndex + "/artist/".length());
            if (artistIdIndex != -1)
                userInput = userInput.substring(0, artistIdIndex);
        }
        else
            return;
        //modify link to latest releases
        if(!userInput.contains("page="))
            userInput += "/releases/?page=20";
        brainzUrlBar.clear();
        saveUrl(sql, userInput);
    }
    public void clickBeatportUrlButton(MouseEvent mouseEvent) throws SQLException {
        String sql = "UPDATE artists SET urlbeatport = ? WHERE artistname = ?";
        String userInput = beatportUrlBar.getText();
        if (userInput.isEmpty() || userInput.isBlank())
            return;
        //reduce to base form then modify
        int artistIndex = userInput.indexOf("/artist/");
        if (artistIndex != -1 && userInput.contains("beatport.com")) {
            int artistIdIndex = userInput.indexOf('/', artistIndex + "/artist/".length());
            if (artistIdIndex != -1) {
                artistIdIndex = userInput.indexOf('/', artistIdIndex + 1); // skip one '/' and find the next '/'
                if (artistIdIndex != -1) {
                    userInput = userInput.substring(0, artistIdIndex); // remove the trailing '/'
                }
            }
        }
        else
            return;
        userInput += "/tracks/?per-page=50";
        beatportUrlBar.clear();
        saveUrl(sql, userInput);
    }
    public void clickJunodownloadUrlButton(MouseEvent mouseEvent) throws SQLException {
        String sql = "UPDATE artists SET urljunodownload = ? WHERE artistname = ?";
        String userInput = junodownloadUrlbar.getText();
        if (userInput.isEmpty() || userInput.isBlank())
            return;
        //reduce to base form then modify
        int artistIndex = userInput.indexOf("/artists/");
        if (artistIndex != -1 && userInput.contains("junodownload.com")) {
            int artistIdIndex = userInput.indexOf('/', artistIndex + "/artists/".length());
            if (artistIdIndex != -1)
                userInput = userInput.substring(0, artistIdIndex + 1);
        }
        else
            return;
        userInput += "releases/?music_product_type=single&laorder=date_down";
        junodownloadUrlbar.clear();
        hideWindows();
        saveUrl(sql, userInput);
    }
    public void saveUrl(String sql, String userInput) throws SQLException {
        //validation of links
        Document doc = null;
        try {
            doc = Jsoup.connect(userInput).timeout(40000).get();
        } catch (SocketTimeoutException e) {
            System.out.println("Task timed out");
            return;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (userInput.contains("musicbrainz.org"))
        {
            Elements songs = doc.select("[href*=/release/]");
            String[] songsArray = songs.eachText().toArray(new String[0]);
            if (songsArray == null)
                return;
            hideWindows();
        }
        else if (userInput.contains("beatport.com"))
        {
            Elements songs = doc.select("span.buk-track-primary-title");
            String[] songsArray = songs.eachText().toArray(new String[0]);
            if (songsArray == null)
                return;
            hideWindows();
        }
        else if (userInput.contains("junodownload.com"))
        {
            Elements songs = doc.select("a.juno-title");
            String[] songsArray = songs.eachText().toArray(new String[0]);
            if (songsArray == null)
                return;
            hideWindows();
        }
        else
            return;
        //after passing check - save input
        Connection conn = DriverManager.getConnection(DBtools.path);
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, userInput);
        pstmt.setString(2, lastClickedArtist);
        pstmt.executeUpdate();
        conn.close();
        pstmt.close();
    }

    public void clickSettings(MouseEvent mouseEvent) {
        settingsWindow.setVisible(true);
    }
    public void clickSettingsClose(MouseEvent mouseEvent) {
        settingsWindow.setVisible(false);
    }

    public void clickScrapeButton(MouseEvent mouseEvent) {
        //amogus threading
        hideWindows();
        addButton.setVisible(false);
        deleteButton.setVisible(false);
        refreshButton.setVisible(false);
        settingsButton.setVisible(false);
        progressbar.setVisible(true);
        progressbar.setProgress(0);
        Task<Void> scrapeTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                scrapeData();
                return null;
            }
        };

        scrapeTask.setOnSucceeded(event -> {
            refreshButton.setVisible(true);
            settingsButton.setVisible(true);
            deleteButton.setVisible(true);
            addButton.setVisible(true);
            progressbar.setVisible(false);
            try {
                fillCombviewTable();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (lastClickedArtist != null && selectedSource != null) {
                try {
                    loadTable();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                try {
                    loadcombviewTable();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        // create an ExecutorService to run tasks on separate threads
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(scrapeTask);
        executor.shutdown();
    }

    public void hideWindows() {
        brainzUrlDiag.setVisible(false);
        beatportUrlDiag.setVisible(false);
        junodownloadUrlDiag.setVisible(false);
        addWindow.setVisible(false);
    }

    public void hyperlinkClick(MouseEvent mouseEvent) {
        Hyperlink clickedHyperlink = (Hyperlink) mouseEvent.getSource();
        String url;
        switch (clickedHyperlink.getId()) {
            case "linkMusicbrainz" -> url = "https://musicbrainz.org";
            case "linkBeatport" -> url = "https://beatport.com";
            case "linkJunodownload" -> url = "https://junodownload.com";
            default -> {
                return;
            }
        }
        ProcessBuilder processBuilder;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("nix") || os.contains("nux") || os.contains("bsd")) {
            //Linux
            processBuilder = new ProcessBuilder("xdg-open", url);
        } else {
            //Windows
            processBuilder = new ProcessBuilder("cmd.exe", "/c", "start", url);
        }
        try {
            processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}