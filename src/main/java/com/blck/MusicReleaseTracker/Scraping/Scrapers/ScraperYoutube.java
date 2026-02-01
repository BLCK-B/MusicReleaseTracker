
package com.blck.MusicReleaseTracker.Scraping.Scrapers;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.TablesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.Scraping.ScraperGenericException;
import com.blck.MusicReleaseTracker.Scraping.ScraperTimeoutException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public final class ScraperYoutube extends Scraper {

    private final String songArtist;

    private final boolean isIDnull;

    private String id;

    public ScraperYoutube(ValueStore store, ErrorLogging log, DBqueries DB, String songArtist, String id) {
        super(store, log, DB);
        this.songArtist = songArtist;
        this.id = id;

        isIDnull = (id == null);
        reduceToID();
    }

    @Override
    public void scrape(int timeout) throws ScraperTimeoutException, ScraperGenericException {
        if (isIDnull) return;

        String url = "https://www.youtube.com/feeds/videos.xml?channel_id=" + id;

        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/")
                    .timeout(timeout).get();
        } catch (SocketTimeoutException e) {
            throw new ScraperTimeoutException(url);
        } catch (Exception e) {
            throw new ScraperGenericException(url);
        }

        String[] songsArray = doc.select("title").eachText().toArray(new String[0]);
        String[] publishedDatesArray = doc.select("published").eachText().toArray(new String[0]);
        String[] mediaDescriptionDatesArray = doc.select("media\\:description").eachText().toArray(new String[0]);
        String[] thumbnailUrlArray = doc.select("media\\:thumbnail").eachAttr("url").toArray(new String[0]);

        // cut date to yyyy-MM-dd
        String[] publishedDates = Arrays.stream(publishedDatesArray)
                .map(date -> date.substring(0, 10))
                .toArray(String[]::new);

        // remove first index, which is the channel name
        songsArray = Arrays.copyOfRange(songsArray, 1, songsArray.length);
        publishedDates = Arrays.copyOfRange(publishedDates, 1, publishedDates.length);

        // topic channels contain a "published on" date in description, which is the real release date
        String[] mediaDescriptionDates = extractDateFromDescription(mediaDescriptionDatesArray);

        // date in the description seems to exist only when the video is uploaded later than release
        String[] datesArray = mergeDescriptionAndPublishedDates(mediaDescriptionDates, publishedDates);

        super.source = TablesEnum.youtube;
        super.insertSet(
                processInfo(
                        artistToSongList(
                                List.of(songsArray),
                                songArtist,
                                List.of(datesArray),
                                null,
                                List.of(thumbnailUrlArray)
                        )));
    }

    public String[] extractDateFromDescription(String[] descriptionArray) {
        Pattern releasedOnPattern = Pattern.compile("Released on:\\s*(\\d{4}-\\d{2}-\\d{2})");
        return Arrays.stream(descriptionArray)
                .map(desc -> {
                    Matcher matcher = releasedOnPattern.matcher(desc);
                    return matcher.find() ? matcher.group(1) : "";
                })
                .toArray(String[]::new);
    }

    public String[] mergeDescriptionAndPublishedDates(String[] preferredDates, String[] defaultDates) {
        if (preferredDates.length != defaultDates.length) return defaultDates;

        return IntStream.range(0, preferredDates.length)
                .mapToObj(i -> {
                    if (!preferredDates[i].isEmpty()) {
                        return preferredDates[i];
                    }
                    return defaultDates[i];
                })
                .toArray(String[]::new);
    }

    @Override
    public void reduceToID() {
        if (isIDnull)
            return;
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
