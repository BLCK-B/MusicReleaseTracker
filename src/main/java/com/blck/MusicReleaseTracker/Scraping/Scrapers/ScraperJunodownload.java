/*
 *         MusicReleaseTracker
 *         Copyright (C) 2023 - 2024 BLCK
 *         This program is free software: you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation, either version 3 of the License, or
 *         (at your option) any later version.
 *         This program is distributed in the hope that it will be useful,
 *         but WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *         GNU General Public License for more details.
 *         You should have received a copy of the GNU General Public License
 *         along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.blck.MusicReleaseTracker.Scraping.Scrapers;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.TablesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.Scraping.ScraperGenericException;
import com.blck.MusicReleaseTracker.Scraping.ScraperTimeoutException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.valueOf;

public final class ScraperJunodownload extends Scraper implements ScraperInterface {

    private final String songArtist;
    private final boolean isIDnull;
    private String id;

    public ScraperJunodownload(ValueStore store, ErrorLogging log, DBqueries DB, String songArtist, String id) {
        super(store, log, DB);
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
        String[] songsArray = doc.select("a.juno-title").eachText().toArray(new String[0]);
        Elements dates = doc.select("div.text-sm.text-muted.mt-3");

        String[] datesArray = new String[dates.size()];
        // processing dates into correct format
        /*
            <div class="text-sm mb-3 mb-lg-3">
             LIQ 202
             <br>
             28 Jun 23
             <br>
             Drum &amp; Bass / Jungle
            </div>
        */
        Pattern pattern = Pattern.compile("\\b (\\d{1,2} [A-Za-z]{3} \\d{2}) \\b");
        for (int i = 0; i < dates.size(); i++) {
            try {
                // <div class="text-sm mb-3 mb-lg-3"> LIQ 202 28 Jun 23 Drum &amp; Bass / Jungle </div>
                String cleanWhitespace = valueOf(dates.get(i))
                        .replaceAll("<br>", " ")
                        .replaceAll("\\s+", " ")
                        .trim();
                Matcher matcher = pattern.matcher(cleanWhitespace);
                String[] extractedParts = matcher.find() ? matcher.group(1).split(" ") : null;
                // 28 Jun 23
                String monthNumber = MonthNumbers.valueOf(extractedParts[1].toUpperCase()).abbr;
                datesArray[i] = "20" + extractedParts[2] + "-" + monthNumber + "-" + extractedParts[0];
                // 2023-06-28
            } catch (Exception e) {
                log.error(e, ErrorLogging.Severity.WARNING, "error processing junodownload date");
            }
        }

        super.source = TablesEnum.junodownload;
        super.insertSet(
                processInfo(
                        artistToSongList(List.of(songsArray), songArtist, List.of(datesArray), null)));
    }

    public void reduceToID() {
        if (isIDnull)
            return;
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

    private enum MonthNumbers {
        JAN("01"), FEB("02"), MAR("03"), APR("04"),
        MAY("05"), JUN("06"), JUL("07"), AUG("08"),
        SEP("09"), OCT("10"), NOV("11"), DEC("12");
        public final String abbr;
        MonthNumbers(String abbr) {
            this.abbr = abbr;
        }
    }

}
