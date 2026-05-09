
package com.blck.MusicReleaseTracker.Scraping.Scrapers;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.TablesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.Scraping.ScraperGenericException;
import com.blck.MusicReleaseTracker.Scraping.ScraperTimeoutException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public final class ScraperBandcamp extends Scraper {

    private final String songArtist;

    private final boolean isIDnull;

    private String id;

    public ScraperBandcamp(ValueStore store, ErrorLogging log, DBqueries DB, String songArtist, String id) {
        super(store, log, DB);
        this.songArtist = songArtist;
        this.id = id;

        isIDnull = (id == null);
        reduceToID();
    }

    @Override
    public String getUrl() {
        return "https://bandcamp.k47.cz/?art=" + id;
    }

    @Override
    public void scrape(int timeout) throws ScraperTimeoutException, ScraperGenericException {
        if (isIDnull) return;

        String url = getUrl();

        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent(
                            "MusicReleaseTracker/v" + store.getAppVersion() + " ( https://github.com/BLCK-B/MusicReleaseTracker )")
                    .timeout(timeout).get();
        } catch (SocketTimeoutException e) {
            throw new ScraperTimeoutException(url);
        } catch (Exception e) {
            throw new ScraperGenericException(url);
        }

        Elements albums = doc.select("div.album");

        List<String> songs = new ArrayList<>();
        List<String> dates = new ArrayList<>();

        for (Element album : albums) {
            Element titleEl = album.selectFirst("a.out");
            Element dateEl = album.selectFirst("span[title]");

            if (titleEl == null || dateEl == null) continue;

            songs.add(titleEl.text());
            dates.add(dateEl.attr("title"));
        }

        super.source = TablesEnum.bandcamp;
        super.insertSet(
                processInfo(
                        artistToSongList(
                                songs,
                                songArtist,
                                dates,
                                null,
                                null
                        )));
    }

    @Override
    public void reduceToID() {
        if (isIDnull) return;

        int idStartIndex;
        int idEndIndex;
        // https://bandcamp.k47.cz/?art=123-id-123
        int queryIndex = id.indexOf("?art=");
        if (queryIndex != -1) {
            idStartIndex = queryIndex + "?art=".length();
            id = id.substring(idStartIndex);
        }
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String toString() {
        return "bandcamp";
    }

}
