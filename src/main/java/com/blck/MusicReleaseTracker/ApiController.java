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

    @RequestMapping("/artistListClick")
    public List<TableModel> artistListClick(@RequestBody Map<String, String> requestData) {
        String artist = requestData.get("artist");
        return sendRequest.artistListClick(artist);
    }

    @RequestMapping ("/sourceTabClick")
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

    @RequestMapping ("/cleanArtistSource")
    public void cleanArtistSource() {
        sendRequest.cleanArtistSource();
    }
    @RequestMapping("/saveUrl")
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

    @PostMapping("/toggleFilter")
    public void toggleFilter(@RequestParam String filter, @RequestParam Boolean value) {
        sendRequest.toggleFilter(filter, value);
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

}