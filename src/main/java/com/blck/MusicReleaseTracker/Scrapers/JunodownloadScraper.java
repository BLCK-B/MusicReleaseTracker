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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.valueOf;

public final class JunodownloadScraper extends ScraperParent implements ScraperInterface {

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
    public JunodownloadScraper(ValueStore valueStore, DBtools DB, String songArtist, String id) {
        super(valueStore, DB);
        this.songArtist = songArtist;
        this.id = id;

        isIDnull = (id == null);
        reduceToID();
    }
    @Override
    public void scrape() throws ScraperTimeoutException {
        if (isIDnull)
            return;

        String url = "https://www.junodownload.com/artists/" + id + "/releases/?music_product_type=single&laorder=date_down";

        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/")
                    .timeout(store.getTimeout()).get();
        }
        catch (SocketTimeoutException e) {
            throw new ScraperTimeoutException(url);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        Elements songs = doc.select("a.juno-title");
        Elements dates = doc.select("div.text-sm.text-muted.mt-3");
        String[] songsArray = songs.eachText().toArray(new String[0]);

        String[] datesArray = new String[dates.size()];
        doc.empty();
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
                DB.logError(e,"WARNING", "error processing junodownload date");
            }
        }

        // create arraylist of song objects
        ArrayList<SongClass> songList = new ArrayList<>();
        for (int i = 0; i < Math.min(songsArray.length, datesArray.length); i++) {
            if (songsArray[i] != null && datesArray[i] != null)
                songList.add(new SongClass(songsArray[i], songArtist, datesArray[i]));
        }

        songs.clear();
        dates.clear();
        songsArray = null;
        datesArray = null;

        super.processInfo(songList, "junodownload");
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
