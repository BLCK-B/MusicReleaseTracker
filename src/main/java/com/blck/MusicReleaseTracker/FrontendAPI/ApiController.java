package com.blck.MusicReleaseTracker.FrontendAPI;

import com.blck.MusicReleaseTracker.Core.TablesEnum;
import com.blck.MusicReleaseTracker.GUIController;
import com.blck.MusicReleaseTracker.DataObjects.TableModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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

    @PostMapping ("/getTableArtistClick")
    public List<TableModel> getTableArtistClick(@RequestBody Map<String, String> requestData) {
        return sendRequest.getTableData(requestData.get("item"));
    }

    @PostMapping ("/getTableSourceClick")
    public List<TableModel> getTableSourceClick(@RequestBody Map<String, String> requestData) {
        return sendRequest.getTableData(TablesEnum.valueOf(requestData.get("item")));
    }

    @PostMapping("/clickArtistAdd")
    public void clickArtistAdd(@RequestBody String artistname) {
        artistname = URLDecoder.decode(artistname, StandardCharsets.UTF_8).replace("=" , "").trim();
        sendRequest.addNewArtist(artistname);
    }

    @RequestMapping ("/clickArtistDelete")
    public void clickArtistDelete() {
        sendRequest.removeArtist();
    }

    @PostMapping("/deleteUrl")
    public void deleteUrl() {
        sendRequest.deleteSourceID();
    }

    @RequestMapping ("/cleanArtistSource")
    public void cleanArtistSource() {
        sendRequest.cleanArtistSource();
    }
    @RequestMapping ("/saveUrl")
    public void saveUrl() {
        sendRequest.saveUrl();
    }

    @PostMapping ("/clickAddURL")
    public void clickAddURL(@RequestBody String url) {
        url = URLDecoder.decode(url, StandardCharsets.UTF_8).replace("=" , "").trim();
        sendRequest.scrapePreview(url);
    }

    @RequestMapping ("/clickScrape")
    public void clickScrape() {
        sendRequest.clickScrape();
    }

    @PostMapping("/setSetting")
    public void setSetting(@RequestBody Map<String, String> params) {
        sendRequest.setSetting(params.get("name"), params.get("value"));
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

    @GetMapping("/checkExistURL")
    public boolean checkExistURL() {
        return sendRequest.checkExistURL();
    }

    @GetMapping("/getScrapeDate")
    public String getScrapeDate() {
        return sendRequest.getScrapeDate();
    }

    @GetMapping("/getLastArtist")
    public String getLastArtist() {
        return sendRequest.getLastArtist();
    }

    @PostMapping("/resetSettings")
    public void resetSettings() {
        sendRequest.resetSettings();
    }

    @PostMapping("/resetDB")
    public void resetDB() {
        sendRequest.resetDB();
    }

}