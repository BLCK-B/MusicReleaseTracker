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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *  Create, write, read, migrate JSON settings file. <br/>
 *  Only one level of nesting is expected, like a map.
 */
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

    /**
     *
     * @return {@code String, String} map of filter name and true/false value
     */
    public HashMap<String, String> getFilterValues() {
        File jsonFile = new File(String.valueOf(store.getConfigPath()));
        var fileContents = readJsonFile(jsonFile);
        HashMap<String, String> filterWords = new HashMap<>();
        fileContents.fieldNames().forEachRemaining(fieldName -> {
            if (fieldName.contains("filter"))
                filterWords.put(fieldName, fileContents.get(fieldName).asText());
        });
        return filterWords;
    }

    /**
     *  facilitates the creation and updating of the settings file
     */
    public void updateSettings() {
        File jsonFile = new File(String.valueOf(store.getConfigPath()));
        try {
            jsonFile.createNewFile();
            JsonNode settingsJson = readJsonFile(jsonFile);
            JsonNode reference = SettingsModel.getSettingsModel();

            migrateDataToReference(reference, settingsJson);

            writeJsonFile(jsonFile, serializeJsonNode(reference));
        } catch (IOException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error updating settings file");
        }
    }

    /**
     *  resets settings to the template with defaults
     */
    public void defaultSettings() {
        new File(String.valueOf(store.getConfigPath())).delete();
        updateSettings();
    }

    /**
     *
     *  @param reference the expected newest structure
     *  @param current to be updated
     */
    public void migrateDataToReference(JsonNode reference, JsonNode current) {
        if (current == null)
            return;
        current.fieldNames().forEachRemaining(fieldName -> {
            if (reference.has(fieldName))
                ((ObjectNode) reference).set(fieldName, current.get(fieldName));
        });
    }

    /**
     *
     *  @param jsonNode settings object representation
     *  @return prettified json, in case of error {@code null}
     */
    public String serializeJsonNode(JsonNode jsonNode) {
        try {
            return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(jsonNode);
        } catch (Exception e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "error serialising JsonNode");
        }
        return null;
    }

    /**
     *
     *  @param file json settings file
     *  @return JsonNode, in case of error {@code null}
     */
    public JsonNode readJsonFile(File file) {
        try {
            return objectMapper.readTree(file);
        } catch (IOException e) {
            log.error(e, ErrorLogging.Severity.WARNING, "error reading settings file - defaulting");
            defaultSettings();
        }
        return null;
    }

    /**
     *
     *  @param file json settings file
     *  @param json json as String
     */
    private void writeJsonFile(File file, String json) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
        } catch (IOException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "settings file could not be written");
        }
    }

    /**
     *
     *  @param setting exact key in the json file
     *  @return value of the entry, else {@code null}
     */
    public String readSetting(String setting) {
        File jsonFile = new File(String.valueOf(store.getConfigPath()));
        try {
            return readJsonFile(jsonFile).get(setting).asText();
        } catch (NullPointerException e) {
            log.error(e, ErrorLogging.Severity.WARNING, "setting " + setting  + " does not exist");
        }
        return null;
    }

    /**
     *  Write any setting in config, {@code setting} must match the defined name
     *
     *  @param setting exact key in the json file
     *  @param value of the entry as String
     */
    public void writeSetting(String setting, String value) {
        File jsonFile = new File(String.valueOf(store.getConfigPath()));
        JsonNode jsonNode = readJsonFile(jsonFile);
        if (!jsonNode.has(setting))
            log.error(new IllegalArgumentException(), ErrorLogging.Severity.WARNING, "setting " + setting  + " does not exist");
        ((ObjectNode) jsonNode).put(setting, value);
        writeJsonFile(jsonFile, serializeJsonNode(jsonNode));
    }

    /**
     *
     *  @return map of all settings in the json file and their values
     */
    public Map<String, String> readAllSettings() {
        File jsonFile = new File(String.valueOf(store.getConfigPath()));
        var allSettings = readJsonFile(jsonFile);
        return Arrays.stream(SettingsModel.values())
                .collect(Collectors.toMap(
                        Enum::name, setting -> allSettings.get(setting.name()).asText()
                ));
    }

}
