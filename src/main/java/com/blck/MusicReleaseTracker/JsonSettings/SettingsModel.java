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

package com.blck.MusicReleaseTracker.JsonSettings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *  The default settings file definition. The enum specifies options which are then converted to JSON representation.
 */
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
    filterVIP(false),
    loadThumbnails(false);

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