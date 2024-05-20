package com.blck.MusicReleaseTracker.Scraping;

import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.FrontendAPI.SSEController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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

    int scrapers;

    @Mock
    DBqueries DB;
    @Mock
    ScraperManager scraperManager;
    @Mock
    SSEController sseController;
    @InjectMocks
    ScrapeProcess scrapeProcess;
    @Captor
    private ArgumentCaptor<Double> progressCaptor;

    @BeforeEach
    void setUp() {
        scrapers = 4;
        when(scraperManager.loadWithScrapers()).thenReturn(4);
        when(scraperManager.scrapeNext()).thenAnswer(invocation -> {
                    scrapers--;
                    return scrapers;
                });
    }

    @Test
    void scrapeDataCallsAllScrapers() {
        scrapeProcess.scrapeData(scraperManager);

        verify(scraperManager, times(4)).scrapeNext();
    }

    @Test
    void scrapeCancelBreaksLoop() {
        when(scraperManager.scrapeNext()).thenAnswer(invocation -> {
            scrapeProcess.scrapeCancel = true;
            return scrapers;
        });

        scrapeProcess.scrapeData(scraperManager);

        verify(scraperManager, times(1)).scrapeNext();
    }

    @Test
    void calculatesCorrectProgress() {
        scrapeProcess.scrapeData(scraperManager);

        verify(sseController, atLeastOnce()).sendProgress(progressCaptor.capture());
        List<Double> values = progressCaptor.getAllValues();
        assertEquals(0.0, values.get(0));
        assertEquals(0.25, values.get(1));
        assertEquals(0.5, values.get(2));
        assertEquals(0.75, values.get(3));
        assertEquals(1, values.get(4));
    }


}
