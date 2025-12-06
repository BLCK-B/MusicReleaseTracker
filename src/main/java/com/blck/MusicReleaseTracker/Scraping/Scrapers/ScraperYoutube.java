
package com.blck.MusicReleaseTracker.Scraping.Scrapers;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.TablesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.Scraping.ScraperGenericException;
import com.blck.MusicReleaseTracker.Scraping.ScraperTimeoutException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

public final class ScraperYoutube extends Scraper {

    private final String songArtist;

    private final boolean isIDnull;

    private String id;

    public ScraperYoutube(ValueStore store, ErrorLogging log, DBqueries DB, String songArtist, String id) {
        super(store, log, DB);
        this.songArtist = songArtist;
        this.id = id;

        isIDnull = (id == null);
        reduceToID();
    }

    @Override
    public void scrape(int timeout) throws ScraperTimeoutException, ScraperGenericException {
        if (isIDnull) return;

        String url = "https://www.youtube.com/feeds/videos.xml?channel_id=" + id;

        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/")
                    .timeout(timeout).get();
        } catch (SocketTimeoutException e) {
            throw new ScraperTimeoutException(url);
        } catch (Exception e) {
            throw new ScraperGenericException(url);
        }

        String[] songsArray = doc.select("title").eachText().toArray(new String[0]);
        String[] datesDirtyArray = doc.select("published").eachText().toArray(new String[0]);
        String[] thumbnailUrlArray = doc.select("media\\:thumbnail").eachAttr("url").toArray(new String[0]);

        // cut date to yyyy-MM-dd
        String[] datesArray = Arrays.stream(datesDirtyArray)
                .map(date -> date.substring(0, 10))
                .toArray(String[]::new);
        // first index is channel name
        songsArray = Arrays.copyOfRange(songsArray, 1, songsArray.length);
        datesArray = Arrays.copyOfRange(datesArray, 1, datesArray.length);

        super.source = TablesEnum.youtube;
        super.insertSet(
                processInfo(
                        artistToSongList(
                                List.of(songsArray),
                                songArtist,
                                List.of(datesArray),
                                null,
                                List.of(thumbnailUrlArray)
                        )));
    }

    @Override
    public void reduceToID() {
        if (isIDnull)
            return;
        int idStartIndex;
        int idEndIndex;
        // https://www.youtube.com/channel/UCWaKvFOf-a7vENyuEsZkNqg
        int channelIndex = id.indexOf("/channel/");
        // url
        if (channelIndex != -1) {
            idStartIndex = channelIndex + "/channel/".length();
            // the next '/' after /artists/
            idEndIndex = id.indexOf('/', idStartIndex);
            if (idEndIndex != -1)
                id = id.substring(idStartIndex, idEndIndex);
            else // if no other '/'
                id = id.substring(idStartIndex);
            // UCWaKvFOf-a7vENyuEsZkNqg
        }
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String toString() {
        return "youtube";
    }

}
