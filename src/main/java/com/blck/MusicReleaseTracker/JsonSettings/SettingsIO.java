package com.blck.MusicReleaseTracker.JsonSettings;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

@Component
public class SettingsIO {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ValueStore store;
    private final ErrorLogging log;

    @Autowired
    public SettingsIO(ValueStore valueStore, ErrorLogging errorLogging) {
        this.store = valueStore;
        this.log = errorLogging;
    }

    public String serializeJsonNode(JsonNode jsonNode) {
        try {
            return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(jsonNode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public JsonNode readJsonFile(File file) {
        try {
            return objectMapper.readTree(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeJsonFile(File file, String json) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateSettings() {
        File jsonFile = new File(String.valueOf(store.getConfigPath()));
        try {
            jsonFile.createNewFile();
            JsonNode settingsJson = readJsonFile(jsonFile);
            JsonNode reference = SettingsModel.getSettingsModel();

            migrateDataToReference(reference, settingsJson);

            writeJsonFile(jsonFile, serializeJsonNode(reference));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void migrateDataToReference(JsonNode reference, JsonNode current) {
        current.fieldNames().forEachRemaining(fieldName -> {
            if (reference.has(fieldName))
                ((ObjectNode) reference).set(fieldName, current.get(fieldName));
        });
    }

}
