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

public final class BeatportScraper extends ScraperParent implements ScraperInterface {

    private final String songArtist;
    private String id;
    public BeatportScraper(ValueStore valueStore, DBtools DB, String songArtist, String id) {
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
        String url = "https://www.beatport.com/artist/" + id + "/tracks";

        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/")
                    .timeout(store.getTimeout()).get();
        } catch (SocketTimeoutException e) {
            DB.logError(e, "INFO", "scrapeBeatport timed out " + url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // pattern matching to make sense of the JSON extracted from <script>
        Elements script = doc.select("script#__NEXT_DATA__[type=application/json]");
        String JSON = script.first().data();
        Pattern pattern = Pattern.compile(
                "\"mix_name\"\\s*:\\s*\"([^\"]+)\",\\s*" +
                        "\"name\"\\s*:\\s*\"([^\"]+)\",\\s*" +
                        "\"new_release_date\"\\s*:\\s*\"([^\"]+)\""
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
        doc.empty();

        // create arraylist of song objects
        ArrayList<SongClass> songList = new ArrayList<SongClass>();
        for (int i = 0; i < Math.min(songsArrayList.size(), datesArrayList.size()); i++) {
            if (songsArrayList.get(i) != null && datesArrayList.get(i) != null && typesArrayList.get(i) != null)
                songList.add(new SongClass(songsArrayList.get(i), songArtist, datesArrayList.get(i), typesArrayList.get(i)));
        }

        script.clear();
        JSON = null;
        songsArrayList.clear();
        typesArrayList.clear();
        datesArrayList.clear();

        super.processInfo(songList, "beatport");
    }

    private void reduceToID() {
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

    public String reduceToID(String id) {
        this.id = id;
        reduceToID();
        return this.id;
    }

    @Override
    public String toString() {
        return "beatport";
    }

}
