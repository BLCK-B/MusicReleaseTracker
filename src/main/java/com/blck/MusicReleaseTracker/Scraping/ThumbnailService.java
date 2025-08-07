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
import org.jsoup.HttpStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ThumbnailService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static HttpClient httpClient = HttpClient.newHttpClient();

    private final ValueStore valueStore;

    private final ErrorLogging log;

    private final String slash = File.separator;

    public boolean scrapeCancel = false;

    @Autowired
    public ThumbnailService(ValueStore valueStore, ErrorLogging log) {
        this.valueStore = valueStore;
        this.log = log;
    }

    public void loadThumbnails(List<MediaItem> mediaItems, int downloadDelay) {
        scrapeCancel = false;
        try {
            Path thumbnailsDir = Paths.get(valueStore.getAppDataPath(), "thumbnails");

            for (MediaItem item : mediaItems) {
                if (scrapeCancel) {
                    return;
                }
                String key = (item.getName() + item.getDate()).toLowerCase().replaceAll("[^a-z0-9]", "");

                String url = item.getThumbnailUrl().orElse(null);
                if (!isValidUrl(url)) {
                    continue;
                }

                if (doesThumbnailExist(thumbnailsDir, key)) {
                    continue;
                }

                String fileName = key + "_" + LocalDateTime.now().format(FORMATTER) + ".jpg";
                Path imagePath = thumbnailsDir.resolve(fileName);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(imagePath));

                if (response.statusCode() != 200) {
                    log.error(new HttpStatusException("", response.statusCode(), url), ErrorLogging.Severity.WARNING, "Thumbnail download failed.");
                }
                Thread.sleep(downloadDelay);
            }
        } catch (Exception e) {
            log.error(e, ErrorLogging.Severity.WARNING, "Failed to load thumbnails.");
        }
    }

    public boolean isValidUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(url.trim());
            String scheme = uri.getScheme();
            return scheme != null && scheme.equalsIgnoreCase("https");
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public List<String> getAllThumbnailUrlsMatchingKeys(List<String> keys) {
        String thumbnailsDirPath = valueStore.getAppDataPath() + "thumbnails" + slash;
        File thumbnailsDir = new File(thumbnailsDirPath);
        if (!thumbnailsDir.exists() || !thumbnailsDir.isDirectory()) {
            return Collections.emptyList();
        }

        try (Stream<Path> paths = Files.list(thumbnailsDir.toPath())) {
            return paths
                    .filter(path -> startsWithKey(path, keys))
                    .map(path -> "/thumbnails/" + path.getFileName()) // forward slashes intentional
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean doesThumbnailExist(Path dir, String key) {
        return !getAllThumbnailUrlsMatchingKeys(List.of(key)).isEmpty();
    }

    public void removeThumbnailsOlderThan(LocalDate localDate) {
        String thumbnailsDirPath = valueStore.getAppDataPath() + "thumbnails" + slash;
        File thumbnailsDir = new File(thumbnailsDirPath);

        try (Stream<Path> paths = Files.list(thumbnailsDir.toPath())) {
            paths.forEach(path -> {
                String fileName = path.getFileName().toString();
                String[] parts = fileName.split("_");
                String fileDate = parts[1].split("\\.")[0];
                try {
                    LocalDate localFileDate = LocalDate.parse(fileDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
                    if (localFileDate.isBefore(localDate)) {
                        Files.delete(path);
                    }
                } catch (Exception e) {
                    log.error(e, ErrorLogging.Severity.WARNING, "Failed to remove thumbnail");
                }
            });
        } catch (IOException e) {
            System.out.println(e.getMessage());
            log.error(e, ErrorLogging.Severity.WARNING, "Remove thumbnails fail");
        }
    }

    private boolean isValidFormat(Path path) {
        if (!Files.isRegularFile(path)) {
            return false;
        }
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
    }

    private boolean startsWithKey(Path path, List<String> keys) {
        String filename = path.getFileName().toString().toLowerCase();
        return keys.stream()
                .anyMatch(filename::startsWith);
    }
}
