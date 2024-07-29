package com.blck.MusicReleaseTracker.JsonSettings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SettingsModel {
    final String theme = "black";
    final String accent = "cactus";
    final String lastScrape = "-";
    final boolean isoDates = false;
    final boolean autoTheme = true;
    final boolean filterAcoustic = false;
    final boolean filterExtended = false;
    final boolean filterInstrumental = false;
    final boolean filterRemaster = false;
    final boolean filterRemix = false;
    final boolean filterVIP = false;

    public JsonNode getSettingsModel() {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.valueToTree(this);
    }
}