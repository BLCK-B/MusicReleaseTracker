
package com.blck.MusicReleaseTracker.Scraping.Scrapers;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.TablesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.DataObjects.Song;
import com.blck.MusicReleaseTracker.Scraping.ScraperGenericException;
import com.blck.MusicReleaseTracker.Scraping.ScraperTimeoutException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The common denominator of specific web source scrapers with super methods and other to be overriden. </br>
 * It is not abstract because we need instantiation.
 */
public abstract class Scraper {

    protected final ErrorLogging log;

    protected final ValueStore store;

    private final DBqueries DB;

    public TablesEnum source;

    public Scraper(ValueStore store, ErrorLogging errorLogging, DBqueries DB) {
        this.log = errorLogging;
        this.DB = DB;
        this.store = store;
    }

    /**
     * Scrapes data from given source and inserts them into the respective source table.
     *
     * @param timeout Jsoup timeout
     */
    public abstract void scrape(int timeout) throws ScraperTimeoutException, ScraperGenericException;

    /**
     *
     * @return base artist identifier for a given source
     */
    public abstract String getID();

    /**
     * Reduces to base id when possible. Does not discard otherwise.
     */
    public abstract void reduceToID();

    /**
     * Creates a {@code List<Song>} from song names, dates, types of one artist.
     *
     * @param names  list of song names
     * @param artist songs from this artist
     * @param dates  release dates of songs
     * @param types  may be null
     * @return {@code List<Song>}
     */
    public List<Song> artistToSongList(List<String> names,
                                       String artist,
                                       List<String> dates,
                                       List<String> types,
                                       List<String> thumbnails) {
        return IntStream.range(0, Math.min(names.size(), dates.size()))
                .filter(i -> names.get(i) != null && artist != null && dates.get(i) != null)
                .mapToObj(i -> new Song(
                        names.get(i),
                        artist,
                        dates.get(i),
                        types == null ? null : types.get(i),
                        thumbnails == null ? null : thumbnails.get(i))
                )
                .collect(Collectors.toList());
    }

    /**
     * Ensures valid date format, unifies symbols in names, removes song name duplicates, sorts by date newest->oldest.
     *
     * @param songList list of songs
     * @return a better formatted list of songs
     */
    public List<Song> processInfo(List<Song> songList) {
        if (songList.isEmpty()) {
            log.error(new Exception(), ErrorLogging.Severity.WARNING, "song list produced by scraper is empty");
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return songList.stream()
                .filter(song -> isValidDate(song.getDate(), formatter))
                .map(song -> new Song(unifyAphostrophes(song.getName()),
                        song.getArtists(),
                        song.getDate(),
                        song.getType().orElse(""),
                        song.getThumbnailUrl().orElse("")
                ))
                .sorted((song1, song2) -> song1.compareDates(song2, formatter))
                .distinct() // remove all name duplicates but the oldest by date
                .toList().reversed(); // newest by date
    }

    public String unifyAphostrophes(String input) {
        return input.replace("’", "'")
                .replace("`", "'")
                .replace("´", "'");
    }

    /**
     *
     * @param date      a date to check
     * @param formatter formatter object with specified date format
     * @return if the date is valid in the requested format
     */
    public boolean isValidDate(String date, DateTimeFormatter formatter) {
        try {
            LocalDate.parse(date, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public void insertSet(List<Song> songList) {
        DB.batchInsertSongs(songList, source, 15);
    }
}
