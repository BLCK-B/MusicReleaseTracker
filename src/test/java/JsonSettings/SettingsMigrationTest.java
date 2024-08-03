package JsonSettings;

import com.blck.MusicReleaseTracker.JsonSettings.SettingsIO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/*      MusicReleaseTracker
    Copyright (C) 2023 BLCK
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.*/

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
