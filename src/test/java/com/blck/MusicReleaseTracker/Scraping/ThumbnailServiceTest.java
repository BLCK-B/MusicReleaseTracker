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
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DataObjects.MediaItem;
import com.blck.MusicReleaseTracker.DataObjects.Song;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ThumbnailServiceTest {

    public final static String testResources = Paths.get("src", "test", "testresources") + File.separator;

    public final Path thumbFolder = Path.of(testResources + "thumbnails");

    private final HttpResponse<Path> mockResponse = mock(HttpResponse.class);

    private final List<MediaItem> newMediaItems = List.of(
            new Song("calling", "artist", "2026-05-21", "", "https://example1.com"),
            new Song("change", "artist", "2024-11-15", "", "https://example2.com")
    );

    @Mock
    ValueStore store;

    @Mock
    ErrorLogging log;

    @Mock
    HttpClient httpClient;

    @InjectMocks
    ThumbnailService thumbnailService;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(store.getAppDataPath()).thenReturn(testResources);
        lenient().when(mockResponse.statusCode()).thenReturn(200);

        ThumbnailService.httpClient = spy(HttpClient.newHttpClient());
        HttpResponse<Path> mockResponse = mock(HttpResponse.class);
        lenient().doReturn(mockResponse).when(ThumbnailService.httpClient).send(any(), any());

        prepareTestThumbnails();
    }

    void prepareTestThumbnails() throws IOException {
        removeTestThumbnails();
        Files.createDirectories(thumbFolder);
        Files.createFile(thumbFolder.resolve("calling20260521_20250727.jpg"));
        Files.createFile(thumbFolder.resolve("louder20250423_20250727.jpeg"));
        Files.createFile(thumbFolder.resolve("partofme20250123_20250725.jpg"));
        Files.createFile(thumbFolder.resolve("change20241115_20250727.png"));
    }

    void removeTestThumbnails() {
        try (Stream<Path> paths = Files.list(thumbFolder)) {
            paths.forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getAllThumbnailUrlsMatchingKeys() {
        List<String> keys = List.of("calling20260521", "change20241115");

        List<String> urls = thumbnailService.getAllThumbnailUrlsMatchingKeys(keys);

        assertAll(
                () -> assertEquals(urls.size(), keys.size()),
                () -> assertEquals("/thumbnails/calling20260521_20250727.jpg", urls.getFirst()),
                () -> assertEquals("/thumbnails/change20241115_20250727.png", urls.getLast())
        );
    }

    @Test
    void getAllThumbnailUrlsMatchingKeysOneDoesntMatch() {
        List<String> keys = List.of("unknown", "calling20260521");

        List<String> urls = thumbnailService.getAllThumbnailUrlsMatchingKeys(keys);

        assertEquals("/thumbnails/calling20260521_20250727.jpg", urls.getFirst());
    }

    @Test
    void doesThumbnailExist() {
        assertAll(
                () -> assertFalse(thumbnailService.doesThumbnailExist(Path.of(testResources + "thumbnails"), "unknown")),
                () -> assertTrue(thumbnailService.doesThumbnailExist(Path.of(testResources + "thumbnails"), "calling20260521"))
        );
    }

    @Test
    void isValidUrl() {
        assertAll(
                () -> assertTrue(thumbnailService.isValidUrl("https://example.com")),
                () -> assertTrue(thumbnailService.isValidUrl("https://sub.domain.com/path?query=1")),
                () -> assertFalse(thumbnailService.isValidUrl(null)),
                () -> assertFalse(thumbnailService.isValidUrl("example.com"))
        );
    }

    @Test
    void loadThumbnails() throws Exception {
        removeTestThumbnails();

        thumbnailService.loadThumbnails(newMediaItems, 0);

        verify(ThumbnailService.httpClient, times(2)).send(any(), any());
    }

    @Test
    void doesNotDownloadExistingImages() throws Exception {
        thumbnailService.loadThumbnails(newMediaItems, 0);

        verify(ThumbnailService.httpClient, never()).send(any(), any());
    }

    @Test
    void stopsOnScrapeCancel() throws Exception {
        removeTestThumbnails();
        lenient().when(ThumbnailService.httpClient.send(any(), any())).thenAnswer(invocation -> {
            thumbnailService.scrapeCancel = true;
            return mockResponse;
        });

        thumbnailService.loadThumbnails(newMediaItems, 0);

        verify(ThumbnailService.httpClient, times(1)).send(any(), any());
    }

    @Test
    void removeThumbnailsOlderThan() {
        List<String> keys = List.of("calling20260521", "change20241115", "louder20250423", "change20241115");

        thumbnailService.removeThumbnailsOlderThan(LocalDate.parse("2025-07-26"));

        List<String> urls = thumbnailService.getAllThumbnailUrlsMatchingKeys(keys);
        assertAll(
                () -> assertEquals(3, urls.size()),
                () -> assertTrue(urls.contains("/thumbnails/calling20260521_20250727.jpg")),
                () -> assertTrue(urls.contains("/thumbnails/louder20250423_20250727.jpeg")),
                () -> assertTrue(urls.contains("/thumbnails/change20241115_20250727.png"))
        );
    }

}
