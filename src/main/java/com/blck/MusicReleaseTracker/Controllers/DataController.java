
package com.blck.MusicReleaseTracker.Controllers;

import com.blck.MusicReleaseTracker.Core.TablesEnum;
import com.blck.MusicReleaseTracker.DTO.MediaItemDTO;
import com.blck.MusicReleaseTracker.ServiceLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DataController {

    private final ServiceLayer serviceLayer;

    @Autowired
    public DataController(ServiceLayer serviceLayer) {
        this.serviceLayer = serviceLayer;
    }

    @GetMapping("/loadList")
    public List<String> loadList() {
        return serviceLayer.loadList();
    }

    @GetMapping("/tableData")
    public List<MediaItemDTO> tableData(@RequestParam String source, @RequestParam String artist) {
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
        return serviceLayer.doesUrlExist(TablesEnum.valueOf(source), artist);
    }

    @GetMapping("/sourcesWithUrl")
    public List<TablesEnum> sourcesWithUrl(@RequestParam String artist) {
        return serviceLayer.sourcesWithUrl(artist);
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

    @PostMapping("/fillCombview")
    public void fillCombview() {
        serviceLayer.fillCombview();
    }

    @PostMapping("/resetDB")
    public void resetDB() {
        serviceLayer.resetDB();
    }

    @GetMapping("/getDBfile")
    public Resource getDBfile() {
        return serviceLayer.getDBfile();
    }

    @PostMapping("/uploadDBfile")
    public void uploadDBfile(@RequestParam("file") MultipartFile file) {
        serviceLayer.uploadDBfile(file);
    }
}