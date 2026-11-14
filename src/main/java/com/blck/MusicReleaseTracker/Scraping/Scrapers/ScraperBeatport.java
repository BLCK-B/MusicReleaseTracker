
package com.blck.MusicReleaseTracker.Scraping.Scrapers;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.TablesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.Scraping.ScraperGenericException;
import com.blck.MusicReleaseTracker.Scraping.ScraperTimeoutException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ScraperBeatport extends Scraper {

    private final String songArtist;

    private final boolean isIDnull;

    private String id;

    public ScraperBeatport(ValueStore store, ErrorLogging log, DBqueries DB, String songArtist, String id) {
        super(store, log, DB);
        this.songArtist = songArtist;
        this.id = id;

        isIDnull = (id == null);
        reduceToID();
    }

    @Override
    public String getUrl() {
        return "https://www.beatport.com/artist/" + id + "/tracks";
    }

    @Override
    public void scrape(String url, int timeout) throws ScraperTimeoutException, ScraperGenericException {
        if (isIDnull) return;

        Document doc = null;
        try {
            if (url.toLowerCase().startsWith("http")) {
                doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/")
                        .timeout(timeout).get();
            } else { // local file for tests
                File localFile = new File(url);
                doc = Jsoup.parse(localFile, "UTF-8");
            }
        } catch (SocketTimeoutException e) {
            throw new ScraperTimeoutException(url);
        } catch (Exception e) {
            throw new ScraperGenericException(url, e.getMessage());
        }

        Elements script = doc.select("script#__NEXT_DATA__[type=application/json]");
        String jsonContent = script.first().data();

        ArrayList<String> songsArrayList = new ArrayList<>();
        ArrayList<String> datesArrayList = new ArrayList<>();
        ArrayList<String> typesArrayList = new ArrayList<>();
        ArrayList<String> thumbnailUrlList = new ArrayList<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonContent);
            JsonNode resultsNode = rootNode.findPath("results");
            for (JsonNode track : resultsNode) {
                songsArrayList.add(track.path("name").asString("").replace("\\u0026", "&"));
                datesArrayList.add(track.path("new_release_date").asString(""));
                typesArrayList.add(track.path("mix_name").asString(""));
                thumbnailUrlList.add(track.path("release").path("image").path("uri").asString("").replace("{w}x{h}", "300x300"));
            }
        } catch (Exception e) {
            throw new ScraperGenericException(url, e.getMessage());
        }

        super.source = TablesEnum.beatport;
        super.insertSet(
                processInfo(
                        artistToSongList(
                                songsArrayList,
                                songArtist,
                                datesArrayList,
                                typesArrayList,
                                thumbnailUrlList
                        )
                ));
    }

    @Override
    public void reduceToID() {
        if (isIDnull) return;

        int idStartIndex;
        int idEndIndex;
        // https://beatport.com/artist/koven/245904/charts
        int artistIndex = id.indexOf("/artist/");
        if (artistIndex != -1 && id.contains("beatport.com")) {
            idStartIndex = artistIndex + "/artist/".length();
            int firstSlash = id.indexOf('/', idStartIndex) + 1;
            // the second '/' after /artist/
            idEndIndex = id.indexOf('/', firstSlash);
            if (idEndIndex != -1)
                id = id.substring(idStartIndex, idEndIndex);
            else // if no other '/'
                id = id.substring(idStartIndex);
            // koven/245904
        }
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String toString() {
        return "beatport";
    }

}
