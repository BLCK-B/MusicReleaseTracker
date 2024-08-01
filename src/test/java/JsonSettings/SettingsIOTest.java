package JsonSettings;

import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.JsonSettings.SettingsIO;
import com.blck.MusicReleaseTracker.JsonSettings.SettingsModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SettingsIOTest {

    final static Path testSettingsPath = Paths.get("src", "test", "testresources", "testSettings.json");
    final static File settingsFile = new File(testSettingsPath.toString());

    static String testingBlbost = "";

    @Mock
    ValueStore valueStore;
    @InjectMocks
    SettingsIO settingsIO;

    @BeforeEach
    void setUp() {
        try (FileWriter writer = new FileWriter(settingsFile)) {
            settingsFile.createNewFile();
            writer.write("""
                    {
                      "theme" : "black",
                      "filterRemix" : false
                    }
                    """);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
    void currentSettingsFileEmpty() {
        ObjectMapper objectMapper = new ObjectMapper();
        var current = objectMapper.createObjectNode();
        var reference = ModelFactory.getModelV1();

        settingsIO.migrateDataToReference(reference, current);
        assertFalse(reference.isEmpty());
    }

    @Test
    void readSetting() {
        // TODO: inject enum - not production
        when(valueStore.getConfigPath()).thenReturn(testSettingsPath);

        assertEquals("false", settingsIO.readSetting(SettingsModel.filterRemix));
    }

    @Test
    void readSettingWhenEmptyFile() throws IOException {
        when(valueStore.getConfigPath()).thenReturn(testSettingsPath);
        try (FileWriter writer = new FileWriter(settingsFile)) {
            writer.write("");
        }

        // TODO: mock errorlog
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> settingsIO.readSetting(SettingsModel.filterRemix));
    }

}
