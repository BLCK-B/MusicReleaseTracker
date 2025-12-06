package com.blck.MusicReleaseTracker.JsonSettings;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SettingsMigrationTest {

    SettingsIO settingsIO = new SettingsIO(null, null);

    @Test
    void previouslyNonexistentOptionsAdded() {
        var current = ModelFactory.getModelV1();
        var reference = ModelFactory.getModelV2();

        assertFalse(current.has("V2exclusive"));
        settingsIO.migrateDataToReference(reference, current);
        assertTrue(reference.has("V2exclusive"));
    }

    @Test
    void noLongerSupportedOptionsOmitted() {
        var current = ModelFactory.getModelV2();
        var reference = ModelFactory.getModelV1();

        assertTrue(current.has("V2exclusive"));
        settingsIO.migrateDataToReference(reference, current);
        assertFalse(reference.has("V2exclusive"));
    }

    @Test
    void retainsOldStateOfSharedBool() {
        var current = ModelFactory.getModelV1();
        current.put("autoTheme", true);
        var reference = ModelFactory.getModelV2();

        assertFalse(reference.get("autoTheme").asBoolean());
        settingsIO.migrateDataToReference(reference, current);
        assertTrue(reference.get("autoTheme").asBoolean());
    }

    @Test
    void retainsOldStateOfSharedString() {
        var current = ModelFactory.getModelV1();
        current.put("theme", "light");
        var reference = ModelFactory.getModelV2();

        assertNotEquals("light", reference.get("theme").textValue());
        settingsIO.migrateDataToReference(reference, current);
        assertEquals("light", reference.get("theme").textValue());
    }

    @Test
    void sameSharedValueRemainsUnchanged() {
        var current = ModelFactory.getModelV1();
        var reference = ModelFactory.getModelV2();

        assertEquals("black", reference.get("theme").textValue());
        settingsIO.migrateDataToReference(reference, current);
        assertEquals("black", reference.get("theme").textValue());
    }

    @Test
    void currentSettingsFileEmptyIsDefaulted() {
        ObjectMapper objectMapper = new ObjectMapper();
        var current = objectMapper.createObjectNode();
        var reference = ModelFactory.getModelV1();

        settingsIO.migrateDataToReference(reference, current);
        assertFalse(reference.isEmpty());
    }

}
