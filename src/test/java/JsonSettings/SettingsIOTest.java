package JsonSettings;

import com.blck.MusicReleaseTracker.JsonSettings.SettingsIO;
import com.blck.MusicReleaseTracker.JsonSettings.SettingsModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SettingsIOTest {

    SettingsIO settingsIO = new SettingsIO();

    @Test
    void previouslyNonexistentOptionsAdded() {
        SettingsModel old = new HelperModelV1();

        HelperModelV2 result = (HelperModelV2) settingsIO.migrateSettings(old, new HelperModelV2());

        assertTrue(result.getIsoDates());
        assertFalse(result.getFilterState("Acoustic"));
    }

    @Test
    void retainsSharedBool() {
        SettingsModel old = new HelperModelV1();
        old.setIsoDates(true);
        HelperModelV2 reference = new HelperModelV2();

        HelperModelV2 result = (HelperModelV2) settingsIO.migrateSettings(old, reference);

        assertFalse(reference.getIsoDates());
        assertTrue(result.getIsoDates());
    }

    @Test
    void retainsSharedString() {
        SettingsModel old = new HelperModelV1();
        old.setTheme("Light");
        HelperModelV2 reference = new HelperModelV2();

        HelperModelV2 result = (HelperModelV2) settingsIO.migrateSettings(old, reference);

        assertNotEquals("Light", reference.getTheme());
        assertEquals("Light", result.getTheme());
    }

    @Test
    void retainsSharedFilter() {
        SettingsModel old = new HelperModelV1();
        old.setFilterState("Remix", true);
        HelperModelV2 reference = new HelperModelV2();

        HelperModelV2 result = (HelperModelV2) settingsIO.migrateSettings(old, reference);

        assertFalse(reference.getFilterState("Remix"));
        assertTrue(result.getFilterState("Remix"));
    }

}
