package com.blck.MusicReleaseTracker.Scraping.Scrapers;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.SourcesEnum;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.Scraping.ScraperGenericException;
import com.blck.MusicReleaseTracker.Scraping.ScraperTimeoutException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

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
        String[] songsArray = doc.select("title").eachText().toArray(new String[0]);
        String[] datesArray = doc.select("first-release-date").eachText().toArray(new String[0]);

        ArrayList<String> songsArrayList = new ArrayList<>(List.of(songsArray));
        ArrayList<String> datesArrayList = new ArrayList<>(List.of(datesArray));

        super.source = SourcesEnum.musicbrainz;
        super.insertSet(
                processInfo(
                        artistToSongList(songsArrayList, songArtist, datesArrayList, null)));
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
