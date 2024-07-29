package com.blck.MusicReleaseTracker.JsonSettings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SettingsIO {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void migrateDataToReference(JsonNode reference, JsonNode current) {
        current.fieldNames().forEachRemaining(fieldName -> {
            if (reference.has(fieldName))
                ((ObjectNode) reference).set(fieldName, current.get(fieldName));
        });
    }

}
