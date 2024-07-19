package com.blck.MusicReleaseTracker.Scraping;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.FrontendAPI.SSEController;
import com.blck.MusicReleaseTracker.DataObjects.Song;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;

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

/**
 * class handling scraping and processing logic
 */
@Component
public class ScrapeProcess {

    private final ErrorLogging log;
    private final DBqueries DB;
    private final SSEController SSE;

    @Autowired
    public ScrapeProcess(ErrorLogging errorLogging, DBqueries dBqueries, SSEController sseController) {
        this.log = errorLogging;
        this.DB = dBqueries;
        this.SSE = sseController;
    }

    public boolean scrapeCancel = false;

    public void scrapeData(ScraperManager scraperManager) {
        scrapeCancel = false;
        DB.truncateAllTables();
        final int initSize = scraperManager.loadWithScrapers();
        if (initSize == 0)
            return;
        int remaining = 1;
        double progress = 0.0;
        while (remaining != 0 && !scrapeCancel) {
            remaining = scraperManager.scrapeNext();

            progress = ((double) initSize - (double) remaining) / (double) initSize;
            if (progress != 1.0)
                if (SSE.sendProgress(progress))
                    scrapeCancel = true;
        }
        SSE.sendProgress(1.0);
        System.gc();
    }

    public void fillCombviewTable() {
        DB.truncateCombview();
        ArrayList<Song> songObjectList = DB.getSourceTablesDataForCombview();
        if (songObjectList.isEmpty())
            return;
        ArrayList<Song> finalSortedList = processSongs(songObjectList);
        DB.batchInsertSongs(finalSortedList, null, 115);
        System.gc();
    }

    public ArrayList<Song> processSongs(List<Song> songObjectList) {
        // name-artist duplicates
        Map<String, Song> nameArtistMap =
                songObjectList.stream()
                .collect(Collectors.toUnmodifiableMap(
                        song -> noSpacesLowerCase(song.getName() + song.getArtists()),
                        song -> song, this::getOlderDate
                ));
        // name-date duplicates
        Map<String, Song> nameDateMap =
                nameArtistMap.values().stream()
                .collect(Collectors.toUnmodifiableMap(
                        song -> noSpacesLowerCase(noSpacesLowerCase(song.getName() + song.getDate())),
                        song -> song, (existingValue, newValue) -> {
                            existingValue.appendArtist(newValue.getArtists());
                            return existingValue;
                        }
                ));
        // sort by newest
        return nameDateMap.values().stream()
                .sorted(Comparator.comparing(Song::getDate).reversed())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private String noSpacesLowerCase(String s) {
        return s.replaceAll("\\s+", "").toLowerCase();
    }

    private Song getOlderDate(Song song1, Song song2) {
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

}