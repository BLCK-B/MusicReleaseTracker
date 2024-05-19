package com.blck.MusicReleaseTracker.Scraping;

import com.blck.MusicReleaseTracker.ConfigTools;
import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.FrontendAPI.SSEController;
import com.blck.MusicReleaseTracker.DataObjects.Song;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import java.util.stream.Collectors;

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
    private final ConfigTools config;
    private final DBqueries DB;
    private final SSEController SSE;

    @Autowired
    public ScrapeProcess(ErrorLogging errorLogging, ConfigTools configTools, DBqueries DB, SSEController sseController) {
        this.log = errorLogging;
        this.config = configTools;
        this.DB = DB;
        this.SSE = sseController;
    }

    public boolean scrapeCancel = false;

    public void scrapeData() {
        scrapeCancel = false;
        DB.truncateScrapeData(true);
        ScraperManager scrapers = new ScraperManager(log, DB);
        final int initSize = scrapers.loadWithScrapers();
        if (initSize == 0)
            return;
        int remaining = 0;
        double progress = 0.0;
        while (remaining != -1) {
            SSE.sendProgress(progress);
            if (scrapeCancel)
                break;
            remaining = scrapers.scrapeNext();
            progress = ((double) initSize - (double) remaining) / (double) initSize;
        }
        SSE.sendProgress(1.0);
        System.gc();
    }

    public void fillCombviewTable() {
        ArrayList<Song> songObjectList = prepareSongs();
        ArrayList<Song> finalSortedList = processSongs(songObjectList);
        DB.batchInsertSongs(finalSortedList, null, 115);
        System.gc();
    }

    public ArrayList<Song> prepareSongs() {
        config.readConfig(ConfigTools.configOptions.filters);
        DB.truncateScrapeData(false);
        return DB.getAllSourceTableData();
    }

    public ArrayList<Song> processSongs(List<Song> songObjectList) {
        // map songObjectList to get rid of name-artist duplicates, prefer older, example key: neverenoughbensley
        // eg: Never Enough - Bensley - 2023-05-12 : Never Enough - Bensley - 2022-12-16 = Never Enough - Bensley - 2022-12-16
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, Song> nameArtistMap = songObjectList.stream()
                .collect(Collectors.toMap(
                        song -> song.getName().replaceAll("\\s+", "").toLowerCase() + song.getArtist().replaceAll("\\s+", "").toLowerCase(),
                        song -> song, (existingValue, newValue) -> {
                            try {
                                Date existingDate = dateFormat.parse(existingValue.getDate());
                                Date newDate = dateFormat.parse(newValue.getDate());
                                if (existingDate.compareTo(newDate) < 0)
                                    return existingValue;
                                else
                                    return newValue;
                            } catch (ParseException e) {
                                log.error(e, ErrorLogging.Severity.WARNING, "error in parsing dates");
                                return existingValue;
                            }
                        }
                ));
        // map nameArtistMap.values to merge name-date duplicates, example key: theoutlines2023-06-23
        // eg: The Outlines - Koven - 2023-06-23 : The Outlines - Circadian - 2023-06-23 = The Outlines - Circadian, Koven - 2023-06-23
        Map<String, Song> nameDateMap = nameArtistMap.values().stream()
                .collect(Collectors.toMap(
                        song -> song.getName().replaceAll("\\s+", "").toLowerCase() + song.getDate(),
                        song -> song, (existingValue, newValue) -> {
                            // append artist from duplicate song to the already existing object in map
                            String newArtist = newValue.getArtist();
                            if (!existingValue.getArtist().contains(newArtist))
                                existingValue.appendArtist(newArtist);
                            return existingValue;
                        }
                ));
        // create a list of SongClass objects from map, sorted by date
        ArrayList<Song> finalSortedList = nameDateMap.values().stream()
                .sorted(Comparator.comparing(Song::getDate, Comparator.reverseOrder()))
                .collect(Collectors.toCollection(ArrayList::new));

        songObjectList = null;
        nameDateMap = null;
        nameArtistMap = null;

        return finalSortedList;
    }

}