package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.TablesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.DB.ManageMigrateDB;
import com.blck.MusicReleaseTracker.DataObjects.TableModel;
import com.blck.MusicReleaseTracker.Scraping.ScrapeProcess;
import com.blck.MusicReleaseTracker.Scraping.ScraperManager;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/*      MusicReleaseTracker
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

/**
 * class with methods called from ApiController
 */
public class GUIController {

    private final ValueStore store;
    private final ErrorLogging log;
    private final ScrapeProcess scrapeProcess;
    private final ConfigTools config;
    private final DBqueries DB;
    private final ManageMigrateDB manageDB;

    private TablesEnum selectedSource = TablesEnum.combview;
    private String selectedArtist;
    private String tempID;

    @Autowired
    public GUIController(ValueStore valueStore, ErrorLogging errorLogging, ScrapeProcess scrapeProcess,
                         ConfigTools config, DBqueries dBqueries, ManageMigrateDB manageDB) {
        this.store = valueStore;
        this.log = errorLogging;
        this.scrapeProcess = scrapeProcess;
        this.config = config;
        this.DB = dBqueries;
        this.manageDB = manageDB;
    }

    public List<String> loadList() {
        return DB.getArtistList();
    }

    public void addNewArtist(String name) {
        if (name.isEmpty() || name.isBlank())
            return;
        DB.insertIntoArtistList(name);
        selectedArtist = name;
    }

    public void removeArtist() {
        if (selectedArtist == null)
            return;
        DB.removeArtistFromAllTables(selectedArtist);
        selectedArtist = null;
    }

    public void deleteSourceID() {
        if (selectedArtist != null && selectedSource != TablesEnum.combview) {
            DB.updateArtistSourceID(selectedArtist, selectedSource, null);
            DB.clearArtistDataFrom(selectedArtist, selectedSource);
        }
    }

    public void cleanArtistSource() {
        DB.clearArtistDataFrom(selectedArtist, selectedSource);
    }

    public List<TableModel> getTableData(String artist) {
        selectedArtist = artist;
        return selectedSource == TablesEnum.combview ? DB.loadCombviewTable() : DB.loadTable(selectedSource, selectedArtist);
    }

    public List<TableModel> getTableData(TablesEnum source) {
        selectedSource = source;
        if (selectedSource == TablesEnum.combview)
            return DB.loadCombviewTable();
        else if (selectedArtist != null)
            return DB.loadTable(selectedSource, selectedArtist);
        return null;
    }

    public void fillCombview() {
        scrapeProcess.fillCombviewTable();
    }

    public void scrapePreview(String url) {
        if (url.isBlank())
            return;

        tempID = null;
        String id = null;
        try {
            Scraper scraper = null;
            switch (selectedSource) {
                case musicbrainz -> scraper = new ScraperMusicbrainz(log, DB, selectedArtist, url);
                case beatport -> scraper = new ScraperBeatport(log, DB, selectedArtist, url);
                case junodownload -> scraper = new ScraperJunodownload(log, DB, selectedArtist, url);
                case youtube -> scraper = new ScraperYoutube(log, DB, selectedArtist, url);
            }
            id = scraper.getID();
            scraper.scrape(25000);
        } catch (Exception e) {
            log.error(e, ErrorLogging.Severity.WARNING, "error scraping " + selectedSource + ", perhaps an incorrect link");
        }

        tempID = id;
    }

    public void saveUrl() {
       DB.updateArtistSourceID(selectedArtist, selectedSource, tempID);
    }

    public boolean checkExistURL() {
        if (selectedSource == TablesEnum.combview)
            return false;
        return DB.getArtistSourceID(selectedArtist, selectedSource).isPresent();
    }

    public void clickScrape() {
        scrapeProcess.scrapeData(new ScraperManager(log, DB));
        scrapeProcess.fillCombviewTable();
        DB.vacuum();
    }

    public void cancelScrape() {
        scrapeProcess.scrapeCancel = true;
    }

    public HashMap<String, Boolean> settingsOpened() {
        // gather all settings states and return them to frontend when settings are opened
        HashMap<String, Boolean> configData = new HashMap<>();

        config.readConfig(ConfigTools.configOptions.filters);
        ArrayList<String> filterWords = store.getFilterWords();

        String[] allFilters = new String[]{"Acoustic", "Extended", "Instrumental", "Remaster", "Remix", "VIP"};
        Arrays.stream(allFilters)
                .forEach(filter -> configData.put(filter, filterWords.contains(filter)));

        config.readConfig(ConfigTools.configOptions.isoDates);
        configData.put("isoDates", store.getIsoDates());
        config.readConfig(ConfigTools.configOptions.autoTheme);
        configData.put("autoTheme", store.getAutoTheme());

        return configData;
    }

    public void setSetting(String name, String value) {
        // write any setting in config, note: "name" = config name
        config.writeSingleConfig(name, value);
    }

    public Map<String, String> getThemeConfig() {
        config.readConfig(ConfigTools.configOptions.themes);
        return store.getThemes();
    }

    public String getScrapeDate() {
        config.readConfig(ConfigTools.configOptions.lastScrape);
        return store.getScrapeDate();
    }

    public String getLastArtist() {
        return selectedArtist;
    }

    public void resetSettings() {
        config.resetSettings();
    }

    public void resetDB() {
        manageDB.resetDB();
    }
}