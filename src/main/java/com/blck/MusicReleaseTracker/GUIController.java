package com.blck.MusicReleaseTracker;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValueFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public StackPane fadeTables;
    @FXML
    public CheckBox FilterRemix;
    @FXML
    public CheckBox FilterExtended;
    @FXML
    public CheckBox FilterAcoustic;
    @FXML
    public CheckBox FilterVIP;
    @FXML
    public CheckBox FilterRemaster;
    @FXML
    public CheckBox FilterInstrumental;
    @FXML
    public ImageView refreshButtonActive;
    @FXML
    public Hyperlink linkGithub;
    @FXML
    public CheckBox CVlengthShort;
    @FXML
    public CheckBox CVlengthMedium;
    @FXML
    public CheckBox CVlengthLong;
    @FXML
    public AnchorPane settingsAnchorPane;
    @FXML
    public ScrollPane settingsScrollPane;
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
    private ListCell<?> lastClickedCell = null;
    private String lastClickedArtist = null;
    private String selectedSource = null;

    public void updateProgressBar(double state) {
        Platform.runLater(() -> progressbar.setProgress(state));
    }
    public ProgressBar getProgressBar() {
        return progressbar;
    }

    @FXML
    public void initialize() {
        combviewButton.getStyleClass().add("filterclicked");
        try {
            loadList();
            loadcombviewTable();
        } catch (Exception e) {
            System.out.println("could not load artist list or combiew table");
            e.printStackTrace();
        }
        try {
            loadConfigGUI();
        } catch (Exception e) {
            System.out.println("failed reflecting filters in GUI");
            e.printStackTrace();
        }
    }

    public void loadList() throws SQLException {
        dataList.clear();
        //populating artist list - from table "artists"
        Connection conn = DriverManager.getConnection(DBtools.DBpath);
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
                            e.printStackTrace();
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
    public void currentlyScrapedArtist(String artistnamerow) {
        artistList.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String cellContent, boolean empty) {
                super.updateItem(cellContent, empty);
                if (empty || cellContent == null) {
                    setText(null);
                } else {
                    setText(cellContent);
                    getStyleClass().remove("currentlyScraped");
                    if (cellContent.equals(artistnamerow)) {
                        getStyleClass().add("currentlyScraped");
                    }
                }
            }
        });
        artistList.setOnMousePressed(event -> {
            String selectedArtist = artistList.getSelectionModel().getSelectedItem();
            if (selectedArtist != null) {
                try {
                    artistClick(selectedArtist);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                if (lastClickedCell != null) {
                    lastClickedCell.getStyleClass().remove("selected-row");
                }
                ListCell<?> targetCell = (ListCell<?>) event.getTarget();
                targetCell.getStyleClass().add("selected-row");
                lastClickedCell = targetCell;
                lastClickedArtist = selectedArtist;
            }
        });
    }
    public void removeScrapedCss() {
        artistList.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String cellContent, boolean empty) {
                super.updateItem(cellContent, empty);
                    setText(cellContent);
                    getStyleClass().remove("currentlyScraped");
                }
        });
    }

    public void loadTable() throws SQLException {
        dataTable.clear();
        hideWindows();
        combviewTable.setVisible(false);
        //url existence check
        Connection conn = DriverManager.getConnection(DBtools.DBpath);
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
                    case "musicbrainz" -> {
                        brainzUrlDiag.setVisible(true) ;
                        fadeTables.setVisible(true);
                        Platform.runLater(() -> brainzUrlBar.requestFocus());
                    }
                    case "beatport" -> {
                        beatportUrlDiag.setVisible(true);
                        fadeTables.setVisible(true);
                        Platform.runLater(() -> beatportUrlBar.requestFocus());
                    }
                    case "junodownload" -> {
                        junodownloadUrlDiag.setVisible(true);
                        fadeTables.setVisible(true);
                        Platform.runLater(() -> junodownloadUrlbar.requestFocus());
                    }
                }
                return;
            }
        }

        //populating table - for given artist and given source - launched every list and source click
        conn = DriverManager.getConnection(DBtools.DBpath);
        switch (selectedSource) {
            case "musicbrainz" -> sql = "SELECT song, date FROM musicbrainz WHERE artist = ? ORDER BY date DESC";
            case "beatport" -> sql = "SELECT song, date FROM beatport WHERE artist = ? ORDER BY date DESC";
            case "junodownload" -> sql = "SELECT song, date FROM junodownload WHERE artist = ? ORDER BY date DESC";
        }
        pstmt =conn.prepareStatement(sql);
        pstmt.setString(1, lastClickedArtist);
        rs = pstmt.executeQuery();
        //loop through the result set and add each row to the data list
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
        Connection conn = DriverManager.getConnection(DBtools.DBpath);
        //populating combview table
        String sql = "SELECT * FROM combview ORDER BY date DESC";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        //loop through the result set and add each row to the data list
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
        fadeTables.setVisible(true);
        artistInputField.requestFocus();
    }
    public void clickAddConfirm(MouseEvent mouseEvent) throws SQLException {
        //add new artist typed by user
        String userInput = artistInputField.getText();
        if (userInput.isEmpty() || userInput.isBlank() || userInput.length() > 30)
            return;
        artistInputField.clear();
        hideWindows();
        Connection conn = DriverManager.getConnection(DBtools.DBpath);
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
            Connection conn = DriverManager.getConnection(DBtools.DBpath);
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
            RealMain.fillCombviewTable();
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
    public void clickBrainzUrlButton(MouseEvent mouseEvent) {
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
    public void clickBeatportUrlButton(MouseEvent mouseEvent) {
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
        userInput += "/tracks";
        beatportUrlBar.clear();
        saveUrl(sql, userInput);
    }
    public void clickJunodownloadUrlButton(MouseEvent mouseEvent) {
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
    public void saveUrl(String sql, String userInput) {
        //validation of links
        Thread checkingThread = new Thread(() -> {
            Document doc = null;
            try {
                doc = Jsoup.connect(userInput).timeout(40000).get();
            } catch (IOException e) {
                System.out.println("link verification: task timed out");
                return;
            }
            if (userInput.contains("musicbrainz.org"))
            {
                Elements songs = doc.select("[href*=/release/]");
                String[] songsArray = songs.eachText().toArray(new String[0]);
                songs.clear();
                doc.empty();
                if (songsArray.length == 0 || songsArray == null)
                    return;
                hideWindows();
            }
            else if (userInput.contains("beatport.com"))
            {
                Elements script = doc.select("script#__NEXT_DATA__[type=application/json]");
                String JSON = script.first().data();
                Pattern pattern = Pattern.compile(
                        "\"mix_name\"\\s*:\\s*\"([^\"]+)\",\\s*" +
                                "\"name\"\\s*:\\s*\"([^\"]+)\",\\s*" +
                                "\"new_release_date\"\\s*:\\s*\"([^\"]+)\""
                );
                Matcher matcher = pattern.matcher(JSON);
                List<String> songsArray = new ArrayList<>();
                while (matcher.find()) {
                    songsArray.add(matcher.group(2));
                }
                doc.empty();
                script.clear();
                if (songsArray.size() == 0 || songsArray == null)
                    return;
                hideWindows();
            }
            else if (userInput.contains("junodownload.com"))
            {
                Elements songs = doc.select("a.juno-title");
                String[] songsArray = songs.eachText().toArray(new String[0]);
                songs.clear();
                doc.empty();
                if (songsArray.length == 0 || songsArray == null)
                    return;
                hideWindows();
            }
            else
                return;

            Platform.runLater(() -> {
                try (Connection conn = DriverManager.getConnection(DBtools.DBpath);
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, userInput);
                    pstmt.setString(2, lastClickedArtist);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        });
        checkingThread.start();
    }

    public void clickSettings(MouseEvent mouseEvent) {
        settingsWindow.setVisible(true);
    }
    public void clickSettingsClose(MouseEvent mouseEvent) throws SQLException {
        settingsWindow.setVisible(false);
        try {
            RealMain.fillCombviewTable();
        } catch (Exception e) {
            System.out.println("could not fill combiew table");
            e.printStackTrace();
        }
        if (selectedSource == null)
            loadcombviewTable();
    }
    public void toggleFilter(MouseEvent event) {
        //change config filter state on click
        Config config = ConfigFactory.parseFile(new File(DBtools.ConfigPath));
        CheckBox clickedCheckbox = (CheckBox) event.getSource();
        String fxid = clickedCheckbox.getId();
        boolean newState = clickedCheckbox.isSelected();
        switch (fxid) {
            case "FilterRemix" -> fxid = "Remix";
            case "FilterVIP" -> fxid = "VIP";
            case "FilterExtended" -> fxid = "Extended";
            case "FilterRemaster" -> fxid = "Remaster";
            case "FilterAcoustic" -> fxid = "Acoustic";
            case "FilterInstrumental" -> fxid = "Instrumental";
        }
        config = config.withValue("filters." + fxid, ConfigValueFactory.fromAnyRef(newState));
        ConfigRenderOptions renderOptions = ConfigRenderOptions.defaults().setOriginComments(false).setJson(false).setFormatted(true);
        try (PrintWriter writer = new PrintWriter(new FileWriter(DBtools.ConfigPath))) {
            writer.write(config.root().render(renderOptions));
        } catch (IOException e) {
            System.out.println("could not save filter change");
            e.printStackTrace();
        }
    }

    public void toggleCVlength(MouseEvent event) {
        //change config combviewlength state on click
        Config config = ConfigFactory.parseFile(new File(DBtools.ConfigPath));
        CheckBox clickedCheckbox = (CheckBox) event.getSource();
        String fxid = clickedCheckbox.getId();
        String newValue = switch (fxid) {
            case "CVlengthShort" -> {
                CVlengthShort.setMouseTransparent(true);
                CVlengthMedium.setMouseTransparent(false);
                CVlengthLong.setMouseTransparent(false);
                CVlengthMedium.setSelected(false);
                CVlengthLong.setSelected(false);
                yield "short";
            }
            case "CVlengthMedium" -> {
                CVlengthMedium.setMouseTransparent(true);
                CVlengthShort.setMouseTransparent(false);
                CVlengthLong.setMouseTransparent(false);
                CVlengthShort.setSelected(false);
                CVlengthLong.setSelected(false);
                yield "medium";
            }
            case "CVlengthLong" -> {
                CVlengthLong.setMouseTransparent(true);
                CVlengthMedium.setMouseTransparent(false);
                CVlengthShort.setMouseTransparent(false);
                CVlengthShort.setSelected(false);
                CVlengthMedium.setSelected(false);
                yield "long";
            }
            default -> throw new IllegalStateException("unexpected value");
        };
        config = config.withValue("combviewlength", ConfigValueFactory.fromAnyRef(newValue));
        ConfigRenderOptions renderOptions = ConfigRenderOptions.defaults().setOriginComments(false).setJson(false).setFormatted(true);
        try (PrintWriter writer = new PrintWriter(new FileWriter(DBtools.ConfigPath))) {
            writer.write(config.root().render(renderOptions));
        } catch (IOException e) {
            System.out.println("could not save combviewlength change");
            e.printStackTrace();
        }
    }

    public void loadConfigGUI() {
        //reflect the states of config in GUI
        Config config = ConfigFactory.parseFile(new File(DBtools.ConfigPath));
        Config filtersConfig = config.getConfig("filters");
        FilterRemix.setSelected(filtersConfig.getBoolean("Remix"));
        FilterVIP.setSelected(filtersConfig.getBoolean("VIP"));
        FilterExtended.setSelected(filtersConfig.getBoolean("Extended"));
        FilterRemaster.setSelected(filtersConfig.getBoolean("Remaster"));
        FilterAcoustic.setSelected(filtersConfig.getBoolean("Acoustic"));
        FilterInstrumental.setSelected(filtersConfig.getBoolean("Instrumental"));

        String combviewlength = config.getString("combviewlength");
        CVlengthShort.setSelected(combviewlength.equals("short"));
        CVlengthMedium.setSelected(combviewlength.equals("medium"));
        CVlengthLong.setSelected(combviewlength.equals("long"));
        CVlengthShort.setMouseTransparent(CVlengthShort.isSelected());
        CVlengthMedium.setMouseTransparent(CVlengthMedium.isSelected());
        CVlengthLong.setMouseTransparent(CVlengthLong.isSelected());
    }

    public void clickScrapeButton(MouseEvent mouseEvent) {
        //amogus threading
        hideWindows();
        addButton.setVisible(false);
        deleteButton.setVisible(false);
        refreshButton.setMouseTransparent(true);
        refreshButtonActive.setVisible(true);
        settingsButton.setMouseTransparent(true);
        progressbar.setVisible(true);
        progressbar.setProgress(0);
        Task<Void> scrapeTask = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    RealMain.scrapeData();
                } catch (Exception e) {
                    System.out.println("catastrophic error during scraping");
                    e.printStackTrace();
                }
                return null;
            }
        };

        scrapeTask.setOnSucceeded(event -> {
            refreshButton.setMouseTransparent(false);
            refreshButtonActive.setVisible(false);
            settingsButton.setMouseTransparent(false);
            deleteButton.setVisible(true);
            addButton.setVisible(true);
            progressbar.setVisible(false);
            try {
                RealMain.fillCombviewTable();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (lastClickedArtist != null && selectedSource != null) {
                try {
                    loadTable();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    loadcombviewTable();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        //create an ExecutorService to run tasks on separate threads
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(scrapeTask);
        executor.shutdown();
    }
    public void clickScrapeCancel(MouseEvent mouseEvent) {
        RealMain.scrapeCancel = true;
    }

    public void hideWindows() {
        brainzUrlDiag.setVisible(false);
        beatportUrlDiag.setVisible(false);
        junodownloadUrlDiag.setVisible(false);
        addWindow.setVisible(false);
        fadeTables.setVisible(false);
    }

    public void hyperlinkClick(MouseEvent mouseEvent) {
        Hyperlink clickedHyperlink = (Hyperlink) mouseEvent.getSource();
        String url;
        switch (clickedHyperlink.getId()) {
            case "linkMusicbrainz" -> {
                url = "https://musicbrainz.org";
                brainzUrlBar.requestFocus();
            }
            case "linkBeatport" -> {
                url = "https://beatport.com";
                beatportUrlBar.requestFocus();
            }
            case "linkJunodownload" -> {
                url = "https://junodownload.com";
                junodownloadUrlbar.requestFocus();
            }
            case "linkGithub" -> url = "https://github.com/BLCK-B/MusicReleaseTracker";
            default -> {
                return;
            }
        }
        ProcessBuilder processBuilder;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("nix") || os.contains("nux") || os.contains("bsd")) { //linux
            processBuilder = new ProcessBuilder("xdg-open", url);
        } else {  //windows
            processBuilder = new ProcessBuilder("cmd.exe", "/c", "start", url);
        }
        try {
            processBuilder.start();
        } catch (IOException e) {
            System.out.println("hyperlink error");
            e.printStackTrace();
        }
    }

}