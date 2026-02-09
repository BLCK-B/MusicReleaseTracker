
package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.TablesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.DB.MigrateDB;
import com.blck.MusicReleaseTracker.DTO.MediaItemDTO;
import com.blck.MusicReleaseTracker.DataObjects.MediaItem;
import com.blck.MusicReleaseTracker.JsonSettings.SettingsIO;
import com.blck.MusicReleaseTracker.Misc.UpdateChecker;
import com.blck.MusicReleaseTracker.Scraping.ScrapeProcess;
import com.blck.MusicReleaseTracker.Scraping.ScraperManager;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.Scraper;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.ScraperBeatport;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.ScraperMusicbrainz;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.ScraperYoutube;
import com.blck.MusicReleaseTracker.Scraping.Thumbnails.ThumbnailService;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * First layer to be called from DataController
 */
@Component
public class ServiceLayer {

    private final ValueStore store;

    private final ErrorLogging log;

    private final ScrapeProcess scrapeProcess;

    private final SettingsIO settingsIO;

    private final DBqueries DB;

    private final MigrateDB manageDB;

    private final ThumbnailService thumbnailService;

    private final UpdateChecker updateChecker;

    private String tempID;

    @Autowired
    public ServiceLayer(ValueStore valueStore, ErrorLogging errorLogging, ScrapeProcess scrapeProcess,
                        SettingsIO settingsIO, DBqueries dBqueries, MigrateDB manageDB, ThumbnailService thumbnailService, UpdateChecker updateChecker) {
        this.store = valueStore;
        this.log = errorLogging;
        this.scrapeProcess = scrapeProcess;
        this.settingsIO = settingsIO;
        this.DB = dBqueries;
        this.manageDB = manageDB;
        this.thumbnailService = thumbnailService;
        this.updateChecker = updateChecker;
    }

    public boolean isBackendReady() {
        return store.isBackendReady();
    }

    public List<String> loadList() {
        return DB.getArtistList();
    }

    public void addNewArtist(String name) {
        if (name.isBlank())
            return;
        DB.insertIntoArtistList(name);
    }

    public void deleteArtist(String artist) {
        if (artist == null)
            return;
        DB.removeArtistFromAllTables(artist);
    }

    public void deleteSourceID(TablesEnum source, String artist) {
        if (artist != null && source != TablesEnum.combview) {
            DB.updateArtistSourceID(artist, source, null);
            DB.clearArtistDataFrom(artist, source);
        }
    }

    public void cleanArtistSource(TablesEnum table, String artist) {
        DB.clearArtistDataFrom(artist, table);
    }

    public List<MediaItemDTO> getTableData(TablesEnum source, String artist) {
        if (artist == null || artist.isBlank() || source == TablesEnum.combview) {
            return mediaItemsToDTOs(DB.loadCombviewTable());
        } else {
            return mediaItemsToDTOs(DB.loadTable(source, artist));
        }
    }

    private List<MediaItemDTO> mediaItemsToDTOs(List<MediaItem> items) {
        return items.stream()
                .map(i -> new MediaItemDTO(i.getSongs(), i.getAlbum()))
                .toList();
    }

    public void fillCombview() {
        scrapeProcess.fillCombviewTable();
    }

    public void scrapePreview(TablesEnum source, String artist, String url) {
        if (url.isBlank())
            return;

        String id = null;
        try {
            Scraper scraper = null;
            switch (source) {
                case musicbrainz -> scraper = new ScraperMusicbrainz(store, log, DB, artist, url);
                case beatport -> scraper = new ScraperBeatport(store, log, DB, artist, url);
                case youtube -> scraper = new ScraperYoutube(store, log, DB, artist, url);
            }
            id = scraper.getID();
            scraper.scrape(25000);
            tempID = id;
        } catch (Exception e) {
            log.error(e, ErrorLogging.Severity.INFO, "error scraping " + source + ", perhaps an incorrect link: " + e.getMessage());
        }
    }

    public void saveUrl(TablesEnum source, String artist) {
        DB.updateArtistSourceID(artist, source, tempID);
    }

    public boolean doesUrlExist(TablesEnum source, String artist) {
        if (source == TablesEnum.combview)
            return false;
        return DB.getArtistSourceID(artist, source).isPresent();
    }

    public void clickScrape() {
        scrapeProcess.scrapeData(new ScraperManager(log, DB));
        scrapeProcess.fillCombviewTable();
        if (settingsIO.readSetting("loadThumbnails").equals("true")) {
            scrapeProcess.downloadThumbnails();
        }
        cleanupTasks();
    }

    private void cleanupTasks() {
        scrapeProcess.closeSSE();
        thumbnailService.removeThumbnailsOlderThan(LocalDate.now().minusMonths(6));
        DB.vacuum();
        System.gc();
    }

    public void cancelScrape() {
        scrapeProcess.scrapeCancel = true;
        thumbnailService.scrapeCancel = true;
    }

    public List<String> getThumbnailUrls(List<String> keys) {
        return thumbnailService.getAllThumbnailUrlsMatchingKeys(keys);
    }

    public Map<String, String> settingsOpened() {
        return settingsIO.readAllSettings();
    }

    public void setSetting(String name, String value) {
        settingsIO.writeSetting(name, value);
    }

    public Map<String, String> getThemeConfig() {
        Map<String, String> themesMap = new HashMap<>();
        themesMap.put("theme", settingsIO.readSetting("theme"));
        themesMap.put("accent", settingsIO.readSetting("accent"));
        return themesMap;
    }

    public void resetSettings() {
        settingsIO.defaultSettings();
    }

    public void resetDB() {
        manageDB.resetDB();
    }

    public Resource getDBfile() {
        try {
            Resource resource = new UrlResource(store.getDBpath().toUri());
            if (!resource.exists() || !resource.isReadable()) {
                log.error(new FileNotFoundException(), ErrorLogging.Severity.WARNING, "DB file not found or unreadable");
            }
            return resource;
        } catch (Exception e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error getting DB file: " + e.getMessage());
            return null;
        }
    }

    public void uploadDBfile(MultipartFile file) {
        if (file.isEmpty()) {
            log.error(new FileNotFoundException(), ErrorLogging.Severity.WARNING, "DB file is empty");
        }
        try {
            Files.copy(file.getInputStream(), store.getDBpath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "Error saving DB file: " + e.getMessage());
        }
    }

    public String getAppVersion() {
        return store.getAppVersion();
    }

    public boolean isNewUpdate() {
        return updateChecker.isUpdateAfter(store.getAppVersion());
    }
}