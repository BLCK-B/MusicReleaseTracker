/*
 *         MusicReleaseTracker
 *         Copyright (C) 2023 - 2025 BLCK
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

package com.blck.MusicReleaseTracker.Scraping;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.DataObjects.Song;
import com.blck.MusicReleaseTracker.FrontendAPI.SSEController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * class handling scraping and processing logic
 */
@Component
public class ScrapeProcess {

	private final ErrorLogging log;

	private final DBqueries DB;

	private final SSEController SSE;

	private final ThumbnailService thumbnailService;

	public boolean scrapeCancel = false;

	@Autowired
	public ScrapeProcess(ErrorLogging errorLogging, DBqueries dBqueries,
						 SSEController sseController, ThumbnailService thumbnailService) {
		this.log = errorLogging;
		this.DB = dBqueries;
		this.SSE = sseController;
		this.thumbnailService = thumbnailService;
	}

	/**
	 * Clears tables, loads scraperManager and calls scraping until done or cancelled.
	 *
	 * @param scraperManager new instance - no setup necessary
	 */
	public void scrapeData(ScraperManager scraperManager) {
		scrapeCancel = false;
		DB.truncateAllTables();
		final int initSize = scraperManager.loadWithScrapers();
		if (initSize == 0)
			return;
		int remaining = 1;
		double progress = 0.0;
		while (!scrapeCancel) {
			remaining = scraperManager.scrapeNext();
			progress = ((double) initSize - (double) remaining) / (double) initSize;
			if (progress == 1.0) {
				SSE.sendProgress(progress);
				break;
			}
			else {
				SSE.sendProgress(progress);
			}
		}
	}

	/**
	 * Downloads thumbnails for combview songs.
	 */
	public void downloadThumbnails() {
		thumbnailService.loadThumbnails(DB.loadCombviewTable(), 300);
	}

	/**
	 * Closes SSE emitter for frontend progress bar.
	 */
	public void closeSSE() {
		SSE.complete();
	}

	/**
	 * Clears and fills combview with data from source tables using the processing chain.
	 */
	public void fillCombviewTable() {
		DB.truncateCombview();
		List<Song> songList = DB.getSourceTablesDataForCombview();
		if (songList.isEmpty())
			return;
// TODO: chaining - return streams
		songList = mergeNameArtistDuplicates(songList);
		songList = mergeNameDateDuplicates(songList);
		songList = mergeSongsWithinDaysApart(songList, 7);
		songList = groupSameDateArtistSongs(songList, 4);
		songList = sortByNewestAndByName(songList);

		DB.batchInsertCombview(songList);
	}

	/**
	 * Merges songs with the same artist and song name (retains older by date):
	 * <pre>
	 * input:
	 *   songName Joe 2024-10-10
	 *   songName Joe 2024-10-15
	 * output:
	 *   songName Joe 2024-10-10
	 * </pre>
	 *
	 * @param songList list of songs
	 * @return list of songs with merged duplicates
	 */
	public List<Song> mergeNameArtistDuplicates(List<Song> songList) {
		Map<String, Song> nameArtistMap =
			songList.stream()
				.collect(Collectors.toUnmodifiableMap(
					key -> noSpacesLowerCase(key.getName() + key.getArtists()),
					key -> key, this::getOlderDateSong
				));
		return new ArrayList<>(nameArtistMap.values());
	}

	/**
	 * Merges songs with the same song name and date (appends artists):
	 * <pre>
	 * input:
	 *   songName Joe 2024-10-10
	 *   songName Bob 2024-10-10
	 * output:
	 *   songName Bob, Joe 2024-10-10
	 * </pre>
	 *
	 * @param songList list of songs
	 * @return list of songs with merged duplicates
	 */
	public List<Song> mergeNameDateDuplicates(List<Song> songList) {
		Map<String, Song> nameDateMap =
			songList.stream()
				.collect(Collectors.toUnmodifiableMap(
					key -> noSpacesLowerCase(key.getName() + key.getDate()),
					key -> key, (existingValue, newValue) -> {
						existingValue.appendArtist(newValue.getArtists());
						return existingValue;
					}
				));
		return new ArrayList<>(nameDateMap.values());
	}

	/**
	 * Merges all songs with the same song name if they are {@code maxDays} or less apart by date. Retains older by
	 * date. <STRONG>Does not merge same-date songs.</STRONG>
	 * <pre>
	 * input:
	 *   songName Joe 2020-01-05
	 *   songName Bob 2020-01-03
	 *   songName Zilch 2020-01-04
	 * output:
	 *   songName Bob, Joe, Zilch 2020-01-03
	 * </pre>
	 *
	 * @param songList list of songs
	 * @param maxDays max days apart
	 * @return list of songs with merged duplicates
	 */
	public List<Song> mergeSongsWithinDaysApart(List<Song> songList, int maxDays) {
		List<Song> tempList = new ArrayList<>();
		Map<String, Song> nameArtistMap =
			songList.stream()
			.collect(Collectors.toMap(
				key -> noSpacesLowerCase(key.getName()),
				key -> key, (existing, replacement) -> {
					Song older = getOlderDateSong(existing, replacement);
					Song newer = getNewerDateSong(existing, replacement);
					if (!existing.getArtists().equalsIgnoreCase(replacement.getArtists()))
						older.appendArtist(newer.getArtists());
					if (getDayDifference(existing, replacement) > maxDays)
						tempList.add(newer);
					return older;
				},
				LinkedHashMap::new
			));
		tempList.forEach(s -> nameArtistMap.put(s.getDate() + s.getArtists(), s));
		return new ArrayList<>(
				nameArtistMap.values()).stream()
				.sorted(Comparator.comparing(Song::getDate))
				.toList();
	}

	/**
	 * Groups songs of the same artist released on the same day using an album identifier.
	 *
	 * @param songList list of songs
	 * @param atLeast minimum number of songs of album
	 * @return list of songs with and without album identifiers
	 */
	public List<Song> groupSameDateArtistSongs(List<Song> songList, int atLeast) {
		Map<String, List<Song>> artistSameDayCounts =
				songList.stream()
				.collect(Collectors.groupingBy(
						song -> noSpacesLowerCase(song.getArtists() + song.getDate())
				));
		for (List<Song> group : artistSameDayCounts.values()) {
			if (group.size() >= atLeast)
				group.forEach(song -> song.setAlbumID("[" + group.size() + "] songs by " + group.getFirst().getArtists()));
		}
		return artistSameDayCounts.values().stream()
				.flatMap(Collection::stream)
				.toList();
	}

	/**
	 * Sorts a list of songs by date (newest -> oldest) then by song name (A -> Z).
	 *
	 * @param songObjectList list of songs
	 * @return sorted list of songs
	 */
	public List<Song> sortByNewestAndByName(List<Song> songObjectList) {
		return songObjectList.stream()
				.sorted(Comparator.comparing(Song::getDate).reversed()
						.thenComparing(Song::getName))
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private String noSpacesLowerCase(String s) {
		return s.replaceAll("\\s+", "").toLowerCase();
	}

	/**
	 *
	 * @return older song
	 */
	private Song getOlderDateSong(Song song1, Song song2) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date existingDate = dateFormat.parse(song1.getDate());
			Date newDate = dateFormat.parse(song2.getDate());
			return existingDate.before(newDate) ? song1 : song2;
		} catch (ParseException e) {
			log.error(e, ErrorLogging.Severity.SEVERE, "incorrect date format");
		}
		return song1;
	}

	/**
	 *
	 * @return newer song
	 */
	private Song getNewerDateSong(Song song1, Song song2) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date existingDate = dateFormat.parse(song1.getDate());
			Date newDate = dateFormat.parse(song2.getDate());
			return existingDate.after(newDate) ? song1 : song2;
		} catch (ParseException e) {
			log.error(e, ErrorLogging.Severity.SEVERE, "incorrect date format");
		}
		return song1;
	}

	/**
	 *
	 * @return absolute floored difference in days between song dates
	 */
	public int getDayDifference(Song song1, Song song2) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date date1 = dateFormat.parse(song1.getDate());
			Date date2 = dateFormat.parse(song2.getDate());
			long diffMillisec = date2.getTime() - date1.getTime();
			long diffDays = diffMillisec / (1000 * 3600 * 24);
			return (int) Math.abs(Math.floor(diffDays));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

}