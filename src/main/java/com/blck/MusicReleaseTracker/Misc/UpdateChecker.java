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

package com.blck.MusicReleaseTracker.Misc;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
public class UpdateChecker {

    private final ErrorLogging log;

    public UpdateChecker(ErrorLogging log) {
        this.log = log;
    }

    public boolean isUpdateAfter(String currentVersion) {
        try {
            Document doc = Jsoup.connect("https://api.github.com/repos/BLCK-B/MusicReleaseTracker/releases/latest")
                    .ignoreContentType(true)
                    .header("Accept", "application/vnd.github+json")
                    .get();
            String json = doc.body().text();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(json);

            StringBuilder latestVersion = new StringBuilder(node.path("tag_name").asText());
            if (latestVersion == null) {
                log.error(new Exception(), ErrorLogging.Severity.INFO, "Missing version tag in GitHub API response.");
                return false;
            }

            latestVersion = new StringBuilder(latestVersion.substring(1));
            // since I like shortening version tags
            while (latestVersion.length() < currentVersion.length()) {
                latestVersion.append(".0");
            }
            return !latestVersion.toString().equals(currentVersion);
        } catch (Exception e) {
            log.error(new Exception(), ErrorLogging.Severity.WARNING, "Error parsing GitHub API response.");
            return false;
        }
    }
}
