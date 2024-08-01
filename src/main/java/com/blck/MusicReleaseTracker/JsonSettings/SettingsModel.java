package com.blck.MusicReleaseTracker.JsonSettings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** the default settings file definition */
public enum SettingsModel {
    theme("black"),
    accent("cactus"),
    lastScrape("-"),
    isoDates(false),
    autoTheme(true),
    filterAcoustic(false),
    filterExtended(false),
    filterInstrumental(false),
    filterRemaster(false),
    filterRemix(false),
    filterVIP(false);

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Object value;
    SettingsModel(Object value) {
        this.value = value;
    }
    public Object getValue() {
        return value;
    }
    public static JsonNode getSettingsModel() {
        var model = objectMapper.createObjectNode();
        for (SettingsModel setting : SettingsModel.values())
            model.put(setting.name(), setting.getValue().toString());
        return model;
    }
}