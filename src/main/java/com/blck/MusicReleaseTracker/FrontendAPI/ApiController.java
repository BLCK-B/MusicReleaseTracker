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

/**
 * API controller receives REST requests from vue and passes them to ServiceLayer
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final ServiceLayer sendRequest;

    @Autowired
    public ApiController(ServiceLayer serviceLayer) {
        this.sendRequest = serviceLayer;
    }

    @GetMapping("/isBackendReady")
    public boolean isBackendReady() {
        return sendRequest.isBackendReady();
    }

    @GetMapping("/loadList")
    public List<String> loadList() {
        return sendRequest.loadList();
    }

    @GetMapping("/tableData")
    public List<MediaItem> tableData(@RequestParam String source, @RequestParam String artist) {
        return sendRequest.getTableData(TablesEnum.valueOf(source), artist);
    }

    @PostMapping("/thumbnailUrls")
    public List<String> thumbnailUrls(@RequestBody List<String> keys) {
        return sendRequest.getThumbnailUrls(keys);
    }

    @PostMapping("/artist/{artistId}")
    public void addArtist(@PathVariable String artistId) {
        sendRequest.addNewArtist(artistId);
    }

    @DeleteMapping("/artist/{artistId}")
    public void deleteArtist(@PathVariable String artistId) {
        sendRequest.deleteArtist(artistId);
    }

    @GetMapping("/urlExists")
    public boolean urlExists(@RequestParam String source, @RequestParam String artist) {
        return sendRequest.checkExistURL(TablesEnum.valueOf(source), artist);
    }

    @PostMapping("/confirmSaveUrl")
    public void confirmSaveUrl(@RequestParam String source, @RequestParam String artist) {
        sendRequest.saveUrl(TablesEnum.valueOf(source), artist);
    }

    @DeleteMapping("/url")
    public void deleteUrl(@RequestParam String source, @RequestParam String artist) {
        sendRequest.deleteSourceID(TablesEnum.valueOf(source), artist);
    }

    @PostMapping("/scrapePreview")
    public void scrapePreview(@RequestParam String source, @RequestParam String artist, @RequestParam String url) {
        String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
        sendRequest.scrapePreview(TablesEnum.valueOf(source), artist, decodedUrl);
    }

    @PostMapping("/cleanArtistSource")
    public void cleanArtistSource(@RequestParam String source, @RequestParam String artist) {
        sendRequest.cleanArtistSource(TablesEnum.valueOf(source), artist);
    }

    @PostMapping("/scrape")
    public void scrape() {
        sendRequest.clickScrape();
    }

    @PostMapping("/cancelScrape")
    public void cancelScrape() {
        sendRequest.cancelScrape();
    }

    @PutMapping("/setting")
    public void setting(@RequestParam String name, @RequestParam String value) {
        sendRequest.setSetting(name, value);
    }

    @GetMapping("/themeConfig")
    public Map<String, String> themeConfig() {
        return sendRequest.getThemeConfig();
    }

    @GetMapping("/settingsData")
    public Map<String, String> settingsData() {
        return sendRequest.settingsOpened();
    }

    @PostMapping("/fillCombview")
    public void fillCombview() {
        sendRequest.fillCombview();
    }

    @GetMapping("/scrapeDate")
    public String scrapeDate() {
        return sendRequest.getScrapeDate();
    }

    @PostMapping("/resetSettings")
    public void resetSettings() {
        sendRequest.resetSettings();
    }

    @PostMapping("/resetDB")
    public void resetDB() {
        sendRequest.resetDB();
    }

    @GetMapping("/appVersion")
    public String appVersion() {
        return sendRequest.getAppVersion();
    }

//    TODO
//    @GetMapping("/songDetails/{source}/{song}")
//    public SongDetails songDetails(@PathVariable String source, @PathVariable Song song) {
//        return sendRequest.getSongDetails(TablesEnum.valueOf(source), song);
//    }

}