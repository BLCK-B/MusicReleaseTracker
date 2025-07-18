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
import com.blck.MusicReleaseTracker.DTO.SongDetails;
import com.blck.MusicReleaseTracker.DataObjects.MediaItem;
import com.blck.MusicReleaseTracker.DataObjects.Song;
import com.blck.MusicReleaseTracker.JsonSettings.SettingsIO;
import com.blck.MusicReleaseTracker.Scraping.ScrapeProcess;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Class with methods called from ApiController
 */
@Component
public class GUIController {

    private final ValueStore store;
    private final ErrorLogging log;
    private final ScrapeProcess scrapeProcess;
    private final SettingsIO settingsIO;
    private final DBqueries DB;
    private final MigrateDB manageDB;
    private String tempID;

    @Autowired
    public GUIController(ValueStore valueStore, ErrorLogging errorLogging, ScrapeProcess scrapeProcess,
                         SettingsIO settingsIO, DBqueries dBqueries, MigrateDB manageDB) {
        this.store = valueStore;
        this.log = errorLogging;
        this.scrapeProcess = scrapeProcess;
        this.settingsIO = settingsIO;
        this.DB = dBqueries;
        this.manageDB = manageDB;
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
        if (artist == null)
            return DB.loadCombviewTable();
        else if (source == TablesEnum.combview || artist.isBlank())
            return DB.loadCombviewTable();
        else
            return DB.loadTable(source, artist);
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
                case junodownload -> scraper = new ScraperJunodownload(store, log, DB, artist, url);
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

    public SongDetails getSongDetails(TablesEnum source, Song song) {
        String ID = String.valueOf(DB.getArtistSourceID(song.getArtists(), source));
//        SongDetails songDetails = new SongDetails(DB.);
        return null;
    }

    public void clickScrape() {
//        scrapeProcess.scrapeData(new ScraperManager(log, DB));
        scrapeProcess.downloadThumbnails();
        scrapeProcess.fillCombviewTable();
        DB.vacuum();
    }

    public void cancelScrape() {
        scrapeProcess.scrapeCancel = true;
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