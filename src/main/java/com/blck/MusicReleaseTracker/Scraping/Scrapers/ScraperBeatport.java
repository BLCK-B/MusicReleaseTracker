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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public final class ScraperBeatport extends Scraper implements ScraperInterface {

    private final String songArtist;
    private String id;
    private final boolean isIDnull;
    public ScraperBeatport(ErrorLogging log, DBqueries DB, String songArtist, String id) {
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

        String url = "https://www.beatport.com/artist/" + id + "/tracks";

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
        // pattern matching to make sense of the JSON extracted from <script>
        Elements script = doc.select("script#__NEXT_DATA__[type=application/json]");
        String JSON = script.first().data();
        //   "mix_name": "Song name",
        //   "name": "Joe Smith",
        //   "new_release_date": "2024-02-01"
        Pattern pattern = Pattern.compile(
        "\"mix_name\":\"([^\"]+)\"," +
              "\"name\":\"([^\"]+)\"," +
              "\"new_release_date\":\"([^\"]+)\""
        );
        Matcher matcher = pattern.matcher(JSON);
        ArrayList<String> typesArrayList = new ArrayList<>();
        ArrayList<String> songsArrayList = new ArrayList<>();
        ArrayList<String> datesArrayList = new ArrayList<>();

        while (matcher.find()) {
            typesArrayList.add(matcher.group(1));
            songsArrayList.add(matcher.group(2).replace("\\u0026", "&"));
            datesArrayList.add(matcher.group(3));
        }

        // create arraylist of song objects
        ArrayList<Song> songList = new ArrayList<>();
        for (int i = 0; i < Math.min(songsArrayList.size(), datesArrayList.size()); i++) {
            if (songsArrayList.get(i) != null && datesArrayList.get(i) != null && typesArrayList.get(i) != null)
                songList.add(new Song(songsArrayList.get(i), songArtist, datesArrayList.get(i), typesArrayList.get(i)));
        }

        doc = null;
        script = null;
        JSON = null;
        songsArrayList = null;
        typesArrayList = null;
        datesArrayList = null;

        super.songList = songList;
        super.source = SourcesEnum.beatport;
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
