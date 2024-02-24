package com.blck.MusicReleaseTracker.Scrapers;

import com.blck.MusicReleaseTracker.DBtools;
import com.blck.MusicReleaseTracker.Simple.SongClass;
import com.blck.MusicReleaseTracker.ValueStore;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;

public final class YoutubeScraper extends ScraperParent implements ScraperInterface {

    private final String songArtist;
    private String id;
    public YoutubeScraper(ValueStore valueStore, DBtools DB, String songArtist, String id) {
        super(valueStore, DB);
        this.songArtist = songArtist;
        this.id = id;
    }
    @Override
    public void scrape() {
        if (id == null)
            return;
        // creating link
        reduceToID();
        String url = "https://www.youtube.com/feeds/videos.xml?channel_id=" + id;

        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/")
                    .timeout(store.getTimeout()).get();
        } catch (SocketTimeoutException e) {
            DB.logError(e, "INFO", "scrapeYoutube timed out " + url);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        ArrayList<SongClass> songList = new ArrayList<SongClass>();
        for (int i = 0; i < Math.min(songsArray.length, datesArray.length); i++) {
            if (songsArray[i] != null && datesArray[i] != null)
                songList.add(new SongClass(songsArray[i], songArtist, datesArray[i]));
        }

        super.processInfo(songList, "youtube");
    }

    private void reduceToID() {
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

    public String reduceToID(String id) {
        this.id = id;
        reduceToID();
        return this.id;
    }

    @Override
    public String toString() {
        return "youtube";
    }

}
