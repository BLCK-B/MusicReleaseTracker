/*
 *         MusicReleaseTracker
 *         Copyright (C) 2023 - 2024 BLCK
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
import com.blck.MusicReleaseTracker.GUIController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/** API controller receives REST requests from vue and passes them to GUIController */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final GUIController sendRequest;

    @Autowired
    public ApiController(GUIController guiController) {
        this.sendRequest = guiController;
    }

    @GetMapping("/loadList")
    public List<String> loadList() {
        return sendRequest.loadList();
    }

    @PostMapping ("/getTableData")
    public List<MediaItem> getTableData(@RequestBody Map<String, String> requestData) {
        return sendRequest.getTableData(TablesEnum.valueOf(requestData.get("source")), requestData.get("artist"));
    }

    @PostMapping("/clickArtistAdd")
    public void clickArtistAdd(@RequestBody String artistname) {
        artistname = URLDecoder.decode(artistname, StandardCharsets.UTF_8).replace("=" , "").trim();
        sendRequest.addNewArtist(artistname);
    }

    @PostMapping ("/deleteArtist")
    public void deleteArtist(@RequestBody String artist) {
        artist = URLDecoder.decode(artist, StandardCharsets.UTF_8).replace("=" , "").trim();
        sendRequest.deleteArtist(artist);
    }

    @PostMapping("/deleteUrl")
    public void deleteUrl(@RequestBody Map<String, String> requestData) {
        sendRequest.deleteSourceID(TablesEnum.valueOf(requestData.get("source")), requestData.get("artist"));
    }

    @PostMapping ("/cleanArtistSource")
    public void cleanArtistSource(@RequestBody Map<String, String> requestData) {
        sendRequest.cleanArtistSource(TablesEnum.valueOf(requestData.get("source")), requestData.get("artist"));
    }

    @PostMapping ("/saveUrl")
    public void saveUrl(@RequestBody Map<String, String> requestData) {
        sendRequest.saveUrl(TablesEnum.valueOf(requestData.get("source")), requestData.get("artist"));
    }

    @PostMapping ("/clickAddURL")
    public void clickAddURL(@RequestBody Map<String, String> requestData) {
        String url = URLDecoder.decode( requestData.get("url"), StandardCharsets.UTF_8);
		sendRequest.scrapePreview(TablesEnum.valueOf(requestData.get("source")), requestData.get("artist"), url);
    }

    @RequestMapping ("/clickScrape")
    public void clickScrape() {
        sendRequest.clickScrape();
    }

    @PostMapping("/setSetting")
    public void setSetting(@RequestBody Map<String, String> requestData) {
        sendRequest.setSetting(requestData.get("name"), requestData.get("value"));
    }

    @GetMapping("/getThemeConfig")
    public Map<String,String> getThemeConfig() {
        return sendRequest.getThemeConfig();
    }

    @GetMapping("/settingsOpened")
    public Map<String, String> settingsOpened() {
        return sendRequest.settingsOpened();
    }

    @PostMapping("/cancelScrape")
    public void cancelScrape() {
        sendRequest.cancelScrape();
    }

    @PostMapping("/fillCombview")
    public void fillCombview() {
        sendRequest.fillCombview();
    }

    @PostMapping("/checkExistURL")
    public boolean checkExistURL(@RequestBody Map<String, String> requestData) {
        return sendRequest.checkExistURL(TablesEnum.valueOf(requestData.get("source")), requestData.get("artist"));
    }

    @GetMapping("/getScrapeDate")
    public String getScrapeDate() {
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
    @GetMapping("/getAppVersion")
    public String getAppVersion() {
        return sendRequest.getAppVersion();
    }

}