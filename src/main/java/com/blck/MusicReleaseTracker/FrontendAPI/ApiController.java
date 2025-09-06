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

package com.blck.MusicReleaseTracker.FrontendAPI;

import com.blck.MusicReleaseTracker.Core.TablesEnum;
import com.blck.MusicReleaseTracker.DataObjects.MediaItem;
import com.blck.MusicReleaseTracker.ServiceLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final ServiceLayer serviceLayer;

    @Autowired
    public ApiController(ServiceLayer serviceLayer) {
        this.serviceLayer = serviceLayer;
    }

    @GetMapping("/isBackendReady")
    public boolean isBackendReady() {
        return serviceLayer.isBackendReady();
    }

    @GetMapping("/loadList")
    public List<String> loadList() {
        return serviceLayer.loadList();
    }

    @GetMapping("/tableData")
    public List<MediaItem> tableData(@RequestParam String source, @RequestParam String artist) {
        return serviceLayer.getTableData(TablesEnum.valueOf(source), artist);
    }

    @PostMapping("/thumbnailUrls")
    public List<String> thumbnailUrls(@RequestBody List<String> keys) {
        return serviceLayer.getThumbnailUrls(keys);
    }

    @PostMapping("/artist/{artistId}")
    public void addArtist(@PathVariable String artistId) {
        serviceLayer.addNewArtist(artistId);
    }

    @DeleteMapping("/artist/{artistId}")
    public void deleteArtist(@PathVariable String artistId) {
        serviceLayer.deleteArtist(artistId);
    }

    @GetMapping("/urlExists")
    public boolean urlExists(@RequestParam String source, @RequestParam String artist) {
        return serviceLayer.checkExistURL(TablesEnum.valueOf(source), artist);
    }

    @PostMapping("/confirmSaveUrl")
    public void confirmSaveUrl(@RequestParam String source, @RequestParam String artist) {
        serviceLayer.saveUrl(TablesEnum.valueOf(source), artist);
    }

    @DeleteMapping("/url")
    public void deleteUrl(@RequestParam String source, @RequestParam String artist) {
        serviceLayer.deleteSourceID(TablesEnum.valueOf(source), artist);
    }

    @PostMapping("/scrapePreview")
    public void scrapePreview(@RequestParam String source, @RequestParam String artist, @RequestParam String url) {
        String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
        serviceLayer.scrapePreview(TablesEnum.valueOf(source), artist, decodedUrl);
    }

    @PostMapping("/cleanArtistSource")
    public void cleanArtistSource(@RequestParam String source, @RequestParam String artist) {
        serviceLayer.cleanArtistSource(TablesEnum.valueOf(source), artist);
    }

    @PostMapping("/scrape")
    public void scrape() {
        serviceLayer.clickScrape();
    }

    @PostMapping("/cancelScrape")
    public void cancelScrape() {
        serviceLayer.cancelScrape();
    }

    @PutMapping("/setting")
    public void setting(@RequestParam String name, @RequestParam String value) {
        serviceLayer.setSetting(name, value);
    }

    @GetMapping("/themeConfig")
    public Map<String, String> themeConfig() {
        return serviceLayer.getThemeConfig();
    }

    @GetMapping("/settingsData")
    public Map<String, String> settingsData() {
        return serviceLayer.settingsOpened();
    }

    @PostMapping("/fillCombview")
    public void fillCombview() {
        serviceLayer.fillCombview();
    }

    @GetMapping("/scrapeDate")
    public String scrapeDate() {
        return serviceLayer.getScrapeDate();
    }

    @PostMapping("/resetSettings")
    public void resetSettings() {
        serviceLayer.resetSettings();
    }

    @PostMapping("/resetDB")
    public void resetDB() {
        serviceLayer.resetDB();
    }

    @GetMapping("/appVersion")
    public String appVersion() {
        return serviceLayer.getAppVersion();
    }

    @GetMapping("/isNewUpdate")
    public boolean isNewUpdate() {
        return serviceLayer.isNewUpdate();
    }
}