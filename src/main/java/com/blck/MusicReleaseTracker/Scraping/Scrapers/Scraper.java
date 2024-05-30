package com.blck.MusicReleaseTracker.Scraping.Scrapers;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.SourcesEnum;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.Scraping.ScraperGenericException;
import com.blck.MusicReleaseTracker.Scraping.ScraperTimeoutException;
import com.blck.MusicReleaseTracker.DataObjects.Song;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

public class Scraper {

    protected final ErrorLogging log;
    private final DBqueries DB;
    public ArrayList<Song> songList = new ArrayList<>();
    public SourcesEnum source;

    public Scraper(ErrorLogging errorLogging, DBqueries DB) {
        this.log = errorLogging;
        this.DB = DB;
    }

    public void scrape(int timeout) throws ScraperTimeoutException, ScraperGenericException {
        System.out.println("The method scrape() is to be overriden.");
    }

    public String getID() {
        return "The method getID() is to be overriden.";
    }

    public void processInfo() {
        if (songList.isEmpty()) {
            log.error(new Exception(), ErrorLogging.Severity.WARNING, "song list produced by scraper is empty");
            return;
        }
        unifyApostrophes();
        enforceDateFormat();
        sortAndRemoveNameDuplicates();
    }

    public void unifyApostrophes() {
        for (Song song : songList) {
            String songName = song.getName().replace("’", "'").replace("`", "'").replace("´", "'");
            song.setName(songName);
        }
    }

    public void enforceDateFormat() {
        songList.removeIf(obj -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            try {
                LocalDate.parse(obj.getDate(), formatter);
                return false;
            } catch (DateTimeParseException e) {
                return true;
            }
        });
    }

    public void sortAndRemoveNameDuplicates() {
        // oldest to newest
        songList.sort((obj1, obj2) -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date1 = LocalDate.parse(obj1.getDate(), formatter);
            LocalDate date2 = LocalDate.parse(obj2.getDate(), formatter);
            return date1.compareTo(date2);
        });
        Set<String> recordedNames = new HashSet<>();
        songList.removeIf(obj -> {
            String name = obj.getName().toLowerCase();
            if (recordedNames.contains(name))
                return true;
            else {
                recordedNames.add(name);
                return false;
            }
        });
        // newest to oldest
        Collections.reverse(songList);
    }

    public void insertSet() {
        DB.batchInsertSongs(songList, source, 15);
    }
}
