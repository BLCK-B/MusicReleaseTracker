package com.blck.MusicReleaseTracker.Scraping;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.Scraper;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.ScraperBeatport;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.ScraperMusicbrainz;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScraperManagerTest {

    @Mock
    DBqueries DB;
    @Mock
    ScraperMusicbrainz scraperMB;
    @Mock
    ScraperBeatport scraperBP;
    @Mock
    ErrorLogging log;

    ScraperManager scraperManager;
    LinkedList<Scraper> scrapers;

    @BeforeEach
    void setUp() {
        scrapers = new LinkedList<Scraper>();
        scraperManager = new ScraperManager(log, DB, 0);
        lenient().when(DB.getAllScrapers()).thenReturn(scrapers);
        lenient().when(scraperMB.toString()).thenReturn("musicbrainz");
        lenient().when(scraperBP.toString()).thenReturn("beatport");
    }

    void insertScrapers() {
        scrapers.add(scraperMB);
        scrapers.add(scraperMB);
        scrapers.add(scraperBP);
        scrapers.add(scraperMB);
    }

    @Test
    void verifyScrapersLoaded() {
        insertScrapers();
        assertEquals(4, scraperManager.loadWithScrapers());
    }

    @Test
    void scrapingQueueNormalTest() {
        insertScrapers();

        assertEquals(4, scraperManager.loadWithScrapers());
        assertEquals(3, scraperManager.scrapeNext());
        assertEquals(2, scraperManager.scrapeNext());
        assertEquals(1, scraperManager.scrapeNext());
        assertEquals(0, scraperManager.scrapeNext());
    }

    @Test
    void removeSourceScraperOnTimeoutExceptions() throws ScraperTimeoutException, ScraperGenericException {
        doThrow(ScraperTimeoutException.class).when(scraperMB).scrape(anyInt());
        insertScrapers();

        assertEquals(4, scraperManager.loadWithScrapers());
        assertEquals(1, scraperManager.scrapeNext());
        verify(log, times(3)).error(any(), eq(ErrorLogging.Severity.INFO), contains("time out"));
    }

    @Test
    void removingLastSourceScraperOnTimeoutExceptionsReturnsEmpty() throws ScraperTimeoutException, ScraperGenericException {
        doThrow(ScraperTimeoutException.class).when(scraperMB).scrape(anyInt());
        scrapers.add(scraperMB);

        assertEquals(1, scraperManager.loadWithScrapers());
        assertEquals(0, scraperManager.scrapeNext());
        verify(log, times(3)).error(any(), eq(ErrorLogging.Severity.INFO), contains("time out"));
    }

    @Test
    void erroringSourceLogsWarningsAndIsRemoved() throws ScraperTimeoutException, ScraperGenericException {
        doThrow(ScraperGenericException.class).when(scraperMB).scrape(anyInt());
        doThrow(ScraperGenericException.class).when(scraperBP).scrape(anyInt());
        insertScrapers();

        assertEquals(4, scraperManager.loadWithScrapers());
        assertEquals(1, scraperManager.scrapeNext());
        assertEquals(0, scraperManager.scrapeNext());
        verify(log, times(6)).error(any(), eq(ErrorLogging.Severity.WARNING), contains("generic"));
    }

    @Test
    void delaysReturnsZeroWhenWaitedLongerThatMinDelay() {
        assertEquals(0, scraperManager.delays("musicbrainz"));
    }

    @Test
    void delaysReturnsDifferenceWhenWaitedLessThanMinDelay() {
        scraperManager = new ScraperManager(log, DB, 500);
        assertEquals(500, scraperManager.delays("musicbrainz"));
    }
}