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

import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DataObjects.Song;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ThumbnailService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private final ValueStore valueStore;

    @Autowired
    public ThumbnailService(ValueStore valueStore) {
        this.valueStore = valueStore;
    }

    public void loadThumbnails(List<Song> songs) {
        try {
            Path thumbnailsDir = Paths.get(valueStore.getAppDataPath(), "thumbnails");

            for (Song song : songs) {
                String key = (song.getName() + song.getDate()).toLowerCase().replaceAll("[^a-z0-9]", "");

                String url = song.getThumbnailUrl().get();
                if (!isValidUrl(url))
                    continue;

                Optional<Path> existing = findExistingThumbnail(thumbnailsDir, key);
                if (existing.isPresent()) {
                    System.out.println("Thumbnail exists: " + existing.get());
                    continue;
                }

                String timestamp = LocalDateTime.now().format(FORMATTER);
                String filename = key + "_" + timestamp + ".jpg";
                Path imagePath = thumbnailsDir.resolve(filename);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(song.getThumbnailUrl().get()))
                        .GET()
                        .build();

                HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(imagePath));

                if (response.statusCode() == 200) {
                    System.out.println("Downloaded thumbnail: " + imagePath);
                } else {
                    throw new IOException("Failed to download image, status: " + response.statusCode());
                }
                Thread.sleep(250);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load thumbnail: ", e);
        }
    }

    private boolean isValidUrl(String url) {
        if (url == null || url.isBlank())
            return false;
        try {
            URI uri = URI.create(url.trim());
            String scheme = uri.getScheme();
            return scheme != null && scheme.equalsIgnoreCase("https");
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private Optional<Path> findExistingThumbnail(Path dir, String key) throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().startsWith(key + "_"))
                    .findFirst();
        }
    }

    public List<String> getAllThumbnailUrls() {
        String thumbnailsDirPath = valueStore.getAppDataPath() + "thumbnails/";
        File thumbnailsDir = new File(thumbnailsDirPath);
        if (!thumbnailsDir.exists() || !thumbnailsDir.isDirectory()) {
            return Collections.emptyList();
        }

        File[] files = thumbnailsDir.listFiles((dir, name) -> {
            String lowerCase = name.toLowerCase();
            return lowerCase.endsWith(".jpg") || lowerCase.endsWith(".jpeg") || lowerCase.endsWith(".png");
        });

        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }

        return Arrays.stream(files)
                .map(file -> "/thumbnails/" + file.getName())
                .collect(Collectors.toList());
    }
}
