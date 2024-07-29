package com.blck.MusicReleaseTracker.JsonSettings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SettingsModel {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public final String theme = "black";
    public final String accent = "cactus";
    public final String lastScrape = "-";
    public final boolean isoDates = false;
    public final boolean autoTheme = true;
    public final boolean filterAcoustic = false;
    public final boolean filterExtended = false;
    public final boolean filterInstrumental = false;
    public final boolean filterRemaster = false;
    public final boolean filterRemix = false;
    public final boolean filterVIP = false;

    public static JsonNode getSettingsModel() {
        return objectMapper.valueToTree(new SettingsModel());
    }
}