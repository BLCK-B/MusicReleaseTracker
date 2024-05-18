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

public final class ScraperMusicbrainz extends Scraper implements ScraperInterface {

    private final String songArtist;
    private String id;
    private final boolean isIDnull;

    public ScraperMusicbrainz(ErrorLogging log, DBqueries DB, String songArtist, String id) {
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

        String url = "https://musicbrainz.org/ws/2/release-group?artist=" + id + "&type=single&limit=400";

        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent("MusicReleaseTracker ( https://github.com/BLCK-B/MusicReleaseTracker )")
                    .timeout(timeout).get();
        }
        catch (SocketTimeoutException e) {
            throw new ScraperTimeoutException(url);
        }
        catch (Exception e) {
            throw new ScraperGenericException(url);
        }
        Elements songs = doc.select("title");
        Elements dates = doc.select("first-release-date");
        String[] songsArray = songs.eachText().toArray(new String[0]);
        String[] datesArray = dates.eachText().toArray(new String[0]);

        // create arraylist of song objects
        ArrayList<Song> songList = new ArrayList<Song>();
        for (int i = 0; i < Math.min(songsArray.length, datesArray.length); i++) {
            if (songsArray[i] != null && datesArray[i] != null)
                songList.add(new Song(songsArray[i], songArtist, datesArray[i]));
        }
        doc = null;
        songs = null;
        dates = null;
        datesArray = null;
        songsArray = null;

        super.songList = songList;
        super.source = SourcesEnum.musicbrainz;
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

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String toString() {
        return "musicbrainz";
    }

}
