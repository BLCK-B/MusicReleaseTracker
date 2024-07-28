package com.blck.MusicReleaseTracker.JsonSettings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SettingsIO {

    private ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode migrateSettings(Object settingsModel, Object reference) {
        System.out.println();
        JsonNode currentTree = objectMapper.valueToTree(settingsModel);
        JsonNode referenceTree = objectMapper.valueToTree(reference);
        referenceTree.fieldNames().forEachRemaining(fieldName -> {
            JsonNode valueNode = referenceTree.get(fieldName);
            if (currentTree.has(fieldName)) {
                valueNode = currentTree.get(fieldName);
                System.out.println(fieldName + " set to " + valueNode);
            }
            else {
                ((ObjectNode) currentTree).set(fieldName, valueNode);
                System.out.println(fieldName + " added with value " + valueNode);
            }
        });
        currentTree.fieldNames().forEachRemaining(fieldName -> {
            JsonNode valueNode = currentTree.get(fieldName);
            if (!referenceTree.has(fieldName)) {
                ((ObjectNode) currentTree).remove(fieldName);
                System.out.println(fieldName + " removed");
            }
        });

        return currentTree;
    }


}
