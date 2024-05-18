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

import static java.lang.String.valueOf;

public final class ScraperJunodownload extends Scraper implements ScraperInterface {

    private enum MonthNumbers {
        JAN("01"), FEB("02"), MAR("03"), APR("04"),
        MAY("05"), JUN("06"), JUL("07"), AUG("08"),
        SEP("09"), OCT("10"), NOV("11"), DEC("12");
        public final String abbr;
        MonthNumbers(String abbr) {
            this.abbr = abbr;
        }
    }
    private final String songArtist;
    private String id;
    private final boolean isIDnull;
    public ScraperJunodownload(ErrorLogging log, DBqueries DB, String songArtist, String id) {
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

        String url = "https://www.junodownload.com/artists/" + id + "/releases/?music_product_type=single&laorder=date_down";

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
        Elements songs = doc.select("a.juno-title");
        Elements dates = doc.select("div.text-sm.text-muted.mt-3");
        String[] songsArray = songs.eachText().toArray(new String[0]);

        String[] datesArray = new String[dates.size()];
        doc = null;
        // processing dates into correct format
        /* example:
            <div class="text-sm mb-3 mb-lg-3">
             LIQ 202
             <br>
             28 Jun 23
             <br>
             Drum &amp; Bass / Jungle
            </div>
        */
        for (int i = 0; i < dates.size(); i++) {
            try {
                String cleanWhitespace = valueOf(dates.get(i))
                        .replaceAll("<br>", " ")
                        .replaceAll("\\s+", " ")
                        .trim();
                // cleanWhitespace: <div class="text-sm mb-3 mb-lg-3"> LIQ 202 28 Jun 23 Drum &amp; Bass / Jungle </div>

                Pattern pattern = Pattern.compile("\\b (\\d{1,2} [A-Za-z]{3} \\d{2}) \\b");
                Matcher matcher = pattern.matcher(cleanWhitespace);
                String extractedDate = null;
                if (matcher.find())
                    extractedDate = matcher.group(1);
                // extractedDate: 28 Jun 23
                String[] parts = extractedDate.split(" ");
                String monthNumber = MonthNumbers.valueOf(parts[1].toUpperCase()).abbr;
                // only assuming songs from 21st century
                datesArray[i] = "20" + parts[2] + "-" + monthNumber + "-" + parts[0];
                // datesArray[i]: 2023-06-28
            } catch (Exception e) {
                log.error(e, ErrorLogging.Severity.WARNING, "error processing junodownload date");
            }
        }

        // create arraylist of song objects
        ArrayList<Song> songList = new ArrayList<>();
        for (int i = 0; i < Math.min(songsArray.length, datesArray.length); i++) {
            if (songsArray[i] != null && datesArray[i] != null)
                songList.add(new Song(songsArray[i], songArtist, datesArray[i]));
        }

        songs = null;
        dates = null;
        songsArray = null;
        datesArray = null;

        super.songList = songList;
        super.source = SourcesEnum.junodownload;
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
        // https://www.junodownload.com/artists/Koven/releases/
        int artistsIndex = id.indexOf("/artists/");
        if (artistsIndex != -1 && id.contains("junodownload.com")) {
            idStartIndex = artistsIndex + "/artists/".length();
            // the next '/' after /artists/
            idEndIndex = id.indexOf('/', idStartIndex);
            if (idEndIndex != -1)
                id = id.substring(idStartIndex, idEndIndex);
            else // if no other '/'
                id = id.substring(idStartIndex);
            // Koven
        }
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String toString() {
        return "junodownload";
    }

}
