package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.SourcesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DataObjects.TableModel;
import com.blck.MusicReleaseTracker.Scraping.ScrapeProcess;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final DBtools DB;
    private final ManageMigrateDB manageDB;
    private SourcesEnum selectedSource;
    private String lastClickedArtist;
    private String tempID;

    @Autowired
    public GUIController(ValueStore valueStore, ErrorLogging errorLogging, ScrapeProcess scrapeProcess,
                         ConfigTools config, DBtools DB, ManageMigrateDB manageDB) {
        this.store = valueStore;
        this.log = errorLogging;
        this.scrapeProcess = scrapeProcess;
        this.config = config;
        this.DB = DB;
        this.manageDB = manageDB;
    }

    public void setTestData(String lastClickedArtist, SourcesEnum selectedSource) {
        this.lastClickedArtist = lastClickedArtist;
        this.selectedSource = selectedSource;
        tempID = "testingUrl";
    }

    public List<String> loadList() {
        return DB.getArtistList();
    }

    public void addNewArtist(String name) {
        if (name.isEmpty() || name.isBlank())
            return;
        DB.insertIntoArtistList(name);
        lastClickedArtist = null;
    }

    public void deleteArtist() {
        if (lastClickedArtist == null)
            return;
        DB.removeArtist(lastClickedArtist);
        lastClickedArtist = null;
    }

    public void deleteSourceID() {
        if (lastClickedArtist != null && selectedSource != null) {
            DB.updateArtistSourceID(lastClickedArtist, selectedSource, null);
            DB.clearArtistDataFrom(lastClickedArtist, selectedSource.toString());
        }
    }

    public void cleanArtistSource() {
        DB.clearArtistDataFrom(selectedSource.toString(), lastClickedArtist);
    }

    public List<TableModel> getTableData(String item, String origin) {
        if (origin.equals("list"))
            lastClickedArtist = item;
        else if (origin.equals("tab"))
            selectedSource = item.equals("combview") ? null : SourcesEnum.valueOf(item);

        if (selectedSource == null)
            return DB.loadCombviewTable();
        else if (lastClickedArtist != null)
            return DB.loadTable(selectedSource, lastClickedArtist);

        return null;
    }

    public void fillCombview() {
        scrapeProcess.fillCombviewTable();
    }

    public void scrapePreview(String url) {
        if (lastClickedArtist == null || selectedSource == null || url.isBlank())
            return;

        tempID = null;
        String id = null;
        try {
            Scraper scraper = null;
            switch (selectedSource) {
                case musicbrainz -> scraper = new ScraperMusicbrainz(store, log, lastClickedArtist, url);
                case beatport -> scraper = new ScraperBeatport(store, log, lastClickedArtist, url);
                case junodownload -> scraper = new ScraperJunodownload(store, log, lastClickedArtist, url);
                case youtube -> scraper = new ScraperYoutube(store, log, lastClickedArtist, url);
            }
            id = scraper.getID();
            scraper.scrape();
        } catch (Exception e) {
            log.error(e, ErrorLogging.Severity.WARNING, "error scraping " + selectedSource + ", perhaps an incorrect link");
        }

        tempID = id;
    }

    public void saveUrl() {
       DB.updateArtistSourceID(lastClickedArtist, selectedSource, tempID);
    }

    public boolean checkExistURL() {
        try {
            SourcesEnum.valueOf(String.valueOf(selectedSource));
        } catch (IllegalArgumentException e) {
            return false;
        }
        return DB.getArtistSourceID(lastClickedArtist, selectedSource) != null;
    }

    public void clickScrape() {
        scrapeProcess.scrapeData();
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
        for (String filter : allFilters) {
            if (filterWords.contains(filter))
                configData.put(filter, true);
            else
                configData.put(filter, false);
        }

        config.readConfig(ConfigTools.configOptions.longTimeout);
        if (store.getTimeout() > 25000)
            configData.put("longTimeout", true);
        else
            configData.put("longTimeout", false);

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
        return lastClickedArtist;
    }

    public void resetSettings() {
        config.resetSettings();
    }

    public void resetDB() {
        manageDB.resetDB();
    }
}