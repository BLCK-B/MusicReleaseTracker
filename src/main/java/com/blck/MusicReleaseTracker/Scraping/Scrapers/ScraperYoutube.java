package com.blck.MusicReleaseTracker.Scraping.Scrapers;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.SourcesEnum;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.Scraping.ScraperGenericException;
import com.blck.MusicReleaseTracker.Scraping.ScraperTimeoutException;
import com.blck.MusicReleaseTracker.DataObjects.Song;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;

public final class ScraperYoutube extends Scraper implements ScraperInterface {

    private final String songArtist;
    private String id;
    private final boolean isIDnull;
    public ScraperYoutube(ErrorLogging log, DBqueries DB, String songArtist, String id) {
        super(log, DB);
        this.songArtist = songArtist;
        this.id = id;

        isIDnull = (id == null);
        reduceToID();
    }
    @Override
    public void scrape(int timeout) throws ScraperTimeoutException, ScraperGenericException {
        if (isIDnull)
            return;

        String url = "https://www.youtube.com/feeds/videos.xml?channel_id=" + id;

        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/")
                    .timeout(timeout).get();
        }
        catch (SocketTimeoutException e) {
            throw new ScraperTimeoutException(url);
        }
        catch (Exception e) {
            throw new ScraperGenericException(url);
        }
        Elements songs = doc.select("title");
        Elements dates = doc.select("published");
        String[] songsArray = songs.eachText().toArray(new String[0]);
        String[] datesDirtyArray = dates.eachText().toArray(new String[0]);

        // cut date to yyyy-MM-dd
        String[] datesArray = Arrays.stream(datesDirtyArray)
                .map(date -> date.substring(0, 10))
                .toArray(String[]::new);
        // first index is channel name
        songsArray = Arrays.copyOfRange(songsArray, 1, songsArray.length);
        datesArray = Arrays.copyOfRange(datesArray, 1, datesArray.length);

        // create arraylist of song objects
        ArrayList<Song> songList = new ArrayList<Song>();
        for (int i = 0; i < Math.min(songsArray.length, datesArray.length); i++) {
            if (songsArray[i] != null && datesArray[i] != null)
                songList.add(new Song(songsArray[i], songArtist, datesArray[i]));
        }
        doc = null;
        songs = null;
        dates = null;
        songsArray = null;
        datesArray = null;

        super.songList = songList;
        super.source = SourcesEnum.youtube;
        super.processInfo();
        super.insertSet();
    }

    private void reduceToID() {
        if (isIDnull)
            return;
        // reduce url to only the identifier
        // this method is not meant to discard wrong input, it reduces to id when possible
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
