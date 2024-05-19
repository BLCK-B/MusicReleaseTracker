package com.blck.MusicReleaseTracker.Scraping;

import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.DB.DBqueriesClass;
import com.blck.MusicReleaseTracker.DataObjects.Song;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.webservices.server.AutoConfigureMockWebServiceClient;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.event.annotation.BeforeTestClass;

import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

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
@ExtendWith(MockitoExtension.class)
public class ScrapeProcessTest {

    private ValueStore store = new ValueStore();
    private ArrayList<Song> expectedList;
//    private final String DBpath = "jdbc:sqlite:" + Paths.get("src", "test", "testresources", "testdb.db");

    @Mock
    DBqueries DB;

    @Mock
    ScraperManager scraperManager;

    @InjectMocks
    ScrapeProcess scrapeProcess;

    @BeforeEach
    public void setUp() {

    }

    @Test
    @Disabled
    public void testScrapeData() {
//        doNothing().when(DB).truncateScrapeData(anyBoolean());
        when(scraperManager.loadWithScrapers()).thenReturn(0);
        scrapeProcess.scrapeData();
    }



}
