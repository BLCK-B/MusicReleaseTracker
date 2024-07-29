package JsonSettings;

import com.blck.MusicReleaseTracker.JsonSettings.SettingsIO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SettingsIOTest {

    SettingsIO settingsIO = new SettingsIO();

    @Test
    void previouslyNonexistentOptionsAdded() {
        JsonNode current = ModelFactory.getModelV1();
        JsonNode reference = ModelFactory.getModelV2();

        assertFalse(current.has("V2exclusive"));
        settingsIO.migrateDataToReference(reference, current);
        assertTrue(reference.has("V2exclusive"));
    }

    @Test
    void noLongerSupportedOptionsOmitted() {
        JsonNode current = ModelFactory.getModelV2();
        JsonNode reference = ModelFactory.getModelV1();

        assertTrue(current.has("V2exclusive"));
        settingsIO.migrateDataToReference(reference, current);
        assertFalse(reference.has("V2exclusive"));
    }

    @Test
    void retainsSharedBool() {
        JsonNode current = ModelFactory.getModelV1();
        ((ObjectNode) current).put("autoTheme", true);
        JsonNode reference = ModelFactory.getModelV2();

        assertFalse(reference.get("autoTheme").asBoolean());
        settingsIO.migrateDataToReference(reference, current);
        assertTrue(reference.get("autoTheme").asBoolean());
    }

    @Test
    void retainsSharedString() {
        JsonNode current = ModelFactory.getModelV1();
        ((ObjectNode) current).put("theme", "light");
        JsonNode reference = ModelFactory.getModelV2();

        assertNotEquals("light", reference.get("theme").textValue());
        settingsIO.migrateDataToReference(reference, current);
        assertEquals("light", reference.get("theme").textValue());
    }

}
