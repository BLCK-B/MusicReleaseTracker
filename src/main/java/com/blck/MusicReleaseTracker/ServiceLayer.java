/*
 *         MusicReleaseTracker
 *         Copyright (C) 2023 - 2025 BLCK
 *         This program is free software: you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation, either version 3 of the License, or
 *         (at your option) any later version.
 *         This program is distributed in the hope that it will be useful,
 *         but WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *         GNU General Public License for more details.
 *         You should have received a copy of the GNU General Public License
 *         along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.TablesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.DB.MigrateDB;
import com.blck.MusicReleaseTracker.DataObjects.MediaItem;
import com.blck.MusicReleaseTracker.JsonSettings.SettingsIO;
import com.blck.MusicReleaseTracker.Scraping.ScrapeProcess;
import com.blck.MusicReleaseTracker.Scraping.ScraperManager;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.Scraper;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.ScraperBeatport;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.ScraperMusicbrainz;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.ScraperYoutube;
import com.blck.MusicReleaseTracker.Scraping.Thumbnails.ThumbnailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * First layer to be called from ApiController
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

    private String tempID;

    @Autowired
    public ServiceLayer(ValueStore valueStore, ErrorLogging errorLogging, ScrapeProcess scrapeProcess,
                        SettingsIO settingsIO, DBqueries dBqueries, MigrateDB manageDB, ThumbnailService thumbnailService) {
        this.store = valueStore;
        this.log = errorLogging;
        this.scrapeProcess = scrapeProcess;
        this.settingsIO = settingsIO;
        this.DB = dBqueries;
        this.manageDB = manageDB;
        this.thumbnailService = thumbnailService;
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

    public List<MediaItem> getTableData(TablesEnum source, String artist) {
        if (artist == null || artist.isBlank() || source == TablesEnum.combview) {
            return DB.loadCombviewTable();
        } else {
            return DB.loadTable(source, artist);
        }
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
            log.error(e, ErrorLogging.Severity.WARNING, "error scraping " + source + ", perhaps an incorrect link");
        }
    }

    public void saveUrl(TablesEnum source, String artist) {
        DB.updateArtistSourceID(artist, source, tempID);
    }

    public boolean checkExistURL(TablesEnum source, String artist) {
        if (source == TablesEnum.combview)
            return false;
        return DB.getArtistSourceID(artist, source).isPresent();
    }

//    TODO
//    public SongDetails getSongDetails(TablesEnum source, Song song) {
//        String ID = String.valueOf(DB.getArtistSourceID(song.getArtists(), source));

    /// /        SongDetails songDetails = new SongDetails(DB.);
//        return null;
//    }
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

    public String getScrapeDate() {
        store.setScrapeDate(settingsIO.readSetting("lastScrape"));
        return store.getScrapeDate();
    }

    public void resetSettings() {
        settingsIO.defaultSettings();
    }

    public void resetDB() {
        manageDB.resetDB();
    }

    public String getAppVersion() {
        return store.getAppVersion();
    }
}