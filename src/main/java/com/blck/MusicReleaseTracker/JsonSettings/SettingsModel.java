
package com.blck.MusicReleaseTracker.JsonSettings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The default settings file definition. The enum specifies options which are then converted to JSON representation.
 */
public enum SettingsModel {
    theme("dark"),
    accent("cactus"),
    lastScrape("-"),
    isoDates(false),
    autoTheme(true),
    filterAcoustic(false),
    filterExtended(false),
    filterInstrumental(false),
    filterRemaster(false),
    filterRemix(false),
    filterVIP(false),
    loadThumbnails(true),
    flippedThumbSwitch(false); // TODO: next release

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Object value;

    SettingsModel(Object value) {
        this.value = value;
    }

    public static JsonNode getSettingsModel() {
        var model = objectMapper.createObjectNode();
        for (SettingsModel setting : SettingsModel.values()) {
            model.put(setting.name(), setting.getValue().toString());
        }
        return model;
    }

    public Object getValue() {
        return value;
    }
}