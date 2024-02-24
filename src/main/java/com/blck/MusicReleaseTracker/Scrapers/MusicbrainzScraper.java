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

public final class MusicbrainzScraper extends ScraperParent implements ScraperInterface {

    private final String songArtist;
    private String id;
    public MusicbrainzScraper(ValueStore valueStore, DBtools DB, String songArtist, String id) {
        super(valueStore, DB);
        this.songArtist = songArtist;
        this.id = id;
    }
    @Override
    public void scrape() {
        if (id == null)
            return;
        // creating link for API
        reduceToID();
        String url = "https://musicbrainz.org/ws/2/release-group?artist=" + id + "&type=single&limit=400";

        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent("MusicReleaseTracker ( https://github.com/BLCK-B/MusicReleaseTracker )")
                    .timeout(store.getTimeout()).get();
        } catch (SocketTimeoutException e) {
            DB.logError(e, "INFO", "scrapeBrainz timed out " + url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Elements songs = doc.select("title");
        Elements dates = doc.select("first-release-date");
        String[] songsArray = songs.eachText().toArray(new String[0]);
        String[] datesArray = dates.eachText().toArray(new String[0]);

        // create arraylist of song objects
        ArrayList<SongClass> songList = new ArrayList<SongClass>();
        for (int i = 0; i < Math.min(songsArray.length, datesArray.length); i++) {
            if (songsArray[i] != null && datesArray[i] != null)
                songList.add(new SongClass(songsArray[i], songArtist, datesArray[i]));
        }
        doc.empty();
        songs.clear();
        dates.clear();
        datesArray = null;
        songsArray = null;

        super.processInfo(songList, "musicbrainz");
    }

    private void reduceToID() {
        // reduce url to only the identifier
        // this method is not meant to discard wrong input, it reduces to id when possible
        int idStartIndex;
        int idEndIndex;
        // https://musicbrainz.org/artist/ad110705-cbe6-4c47-9b99-8526e6db0f41/recordings
        int artistIndex = id.indexOf("/artist/");
        if (artistIndex != -1 && id.contains("musicbrainz.org")) {
            idStartIndex = artistIndex + "/artist/".length();
            // the next '/' after /artist/
            idEndIndex = id.indexOf('/', idStartIndex);
            if (idEndIndex != -1)
                id = id.substring(idStartIndex, idEndIndex);
            else // if no other '/'
                id = id.substring(idStartIndex);
        // ad110705-cbe6-4c47-9b99-8526e6db0f41
        }
    }

    public String reduceToID(String id) {
        this.id = id;
        reduceToID();
        return this.id;
    }

    @Override
    public String toString() {
        return "musicbrainz";
    }

}
