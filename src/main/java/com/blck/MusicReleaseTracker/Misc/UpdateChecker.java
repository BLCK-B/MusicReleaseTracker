
package com.blck.MusicReleaseTracker.Misc;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
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
