package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.ModelsEnums.TableModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
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

@RestController
@RequestMapping("/api")
public class ApiController {

    private final GUIController sendRequest;

    @Autowired
    public ApiController(GUIController sendRequest) {
        this.sendRequest = sendRequest;
    }

    @GetMapping("/loadList")
    public List<String> loadList() throws SQLException {
        return sendRequest.loadList();
    }

    @PostMapping("/artistListClick")
    public List<TableModel> artistListClick(@RequestBody Map<String, String> requestData) {
        String artist = requestData.get("artist");
        return sendRequest.artistListClick(artist);
    }

    @PostMapping ("/sourceTabClick")
    public List<TableModel> sourceTabClick(@RequestBody Map<String, String> requestData) {
        String source = requestData.get("source");
        return sendRequest.sourceTabClick(source);
    }

    @PostMapping("/clickArtistAdd")
    public void clickArtistAdd(@RequestBody String artistname) {
        artistname = URLDecoder.decode(artistname, StandardCharsets.UTF_8);
        artistname = artistname.replace("=" , "").trim();
        sendRequest.artistAddConfirm(artistname);
    }

    @RequestMapping ("/clickArtistDelete")
    public void clickArtistDelete() {
        sendRequest.artistClickDelete();
    }
    @PostMapping("/deleteUrl")
    public void deleteUrl() {
        sendRequest.deleteUrl();
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
        url = URLDecoder.decode(url, StandardCharsets.UTF_8);
        sendRequest.clickAddURL(url);
    }

    @RequestMapping ("/clickScrape")
    public void clickScrape() {
        sendRequest.clickScrape();
    }

    @PostMapping("/setSetting")
    public void setSetting(@RequestBody Map<String, String> params) {
        String name = params.get("name");
        String value = params.get("value");
        sendRequest.setSetting(name, value);
    }
    @GetMapping("/getThemeConfig")
    public Map<String,String> getThemeConfig() {
        return sendRequest.getThemeConfig();
    }

    @GetMapping("/settingsOpened")
    public HashMap<String, Boolean> settingsOpened() {
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