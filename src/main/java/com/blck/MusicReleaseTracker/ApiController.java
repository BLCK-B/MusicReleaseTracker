package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.ModelsEnums.TableModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    /*
    If you are retrieving a list and not sending any data in the request body, use @GetMapping
    If you are sending data, use @PostMapping
    For universal, @RequestMapping
    */

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
    public void clickArtistAdd(@RequestBody String input) {
        sendRequest.artistAddConfirm(input);
    }
    @RequestMapping ("/clickArtistDelete")
    public void clickArtistDelete() {
        sendRequest.artistClickDelete();
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
}