package JsonSettings;

import com.blck.MusicReleaseTracker.JsonSettings.SettingsIO;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SettingsIOTest {

    SettingsIO settingsIO = new SettingsIO();

    @Test
    void previouslyNonexistentOptionsAdded() {
        Object old = new HelperModelV1();

        JsonNode result = settingsIO.migrateSettings(old, new HelperModelV2());

        assertTrue(result.has("v2exclusive"));
        // TODO: assert filter
    }

    @Test
    void noLongerSupportedOptionsOmitted() {
        Object old = new HelperModelV2();

        JsonNode result = settingsIO.migrateSettings(old, new HelperModelV1());

        assertFalse(result.has("v2exclusive"));
        // TODO: assert filter
    }

    @Test
    void retainsSharedBool() {
        HelperModelV1 old = new HelperModelV1();
        old.setIsoDates(true);
        HelperModelV2 reference = new HelperModelV2();

        JsonNode result = settingsIO.migrateSettings(old, reference);

        assertFalse(reference.getIsoDates());
        assertTrue(result.get("isoDates").asBoolean());
    }

    @Test
    void retainsSharedString() {
        HelperModelV1 old = new HelperModelV1();
        old.setTheme("light");
        HelperModelV2 reference = new HelperModelV2();

        JsonNode result = settingsIO.migrateSettings(old, reference);

        assertNotEquals("light", reference.getTheme());
        assertEquals("light", result.get("theme").textValue());
    }

    @Test
    void retainsSharedFilter() {
        HelperModelV1 old = new HelperModelV1();
        old.setFilterState("remix", true);
        HelperModelV2 reference = new HelperModelV2();

        JsonNode result = settingsIO.migrateSettings(old, reference);

        assertFalse(reference.getFilterState("remix"));
        // TODO: assert filter
    }

}
