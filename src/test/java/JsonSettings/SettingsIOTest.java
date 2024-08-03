package JsonSettings;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.JsonSettings.SettingsIO;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SettingsIOTest {

    final static Path testSettingsPath = Paths.get("src", "test", "testresources", "testSettings.json");
    final static File settingsFile = new File(testSettingsPath.toString());

    static String testingBlbost = "";

    @Mock
    ValueStore valueStore;
    @Mock
    ErrorLogging log;
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
        when(valueStore.getConfigPath()).thenReturn(testSettingsPath);

        assertEquals("false", settingsIO.readSetting("filterRemix"));
    }

    @Test
    void readNonexistentSetting() {
        when(valueStore.getConfigPath()).thenReturn(testSettingsPath);

        settingsIO.readSetting("doesNotExist");

        verify(log, times(1)).error(any(), eq(ErrorLogging.Severity.WARNING), contains("does not exist"));
    }

    @Test
    void readSettingWhenEmptyFile() throws IOException {
        when(valueStore.getConfigPath()).thenReturn(testSettingsPath);
        try (FileWriter writer = new FileWriter(settingsFile)) {
            writer.write("");
        }

        settingsIO.readSetting("filterRemix");

        verify(log, times(1)).error(any(), eq(ErrorLogging.Severity.WARNING), contains("does not exist"));
    }

    @Test
    void writeSetting() {
        when(valueStore.getConfigPath()).thenReturn(testSettingsPath);

        settingsIO.writeSetting("theme", "light");

        assertEquals("light", settingsIO.readSetting("theme"));
    }

    @Test
    void writingSettingThatDoesNotExist() {
        when(valueStore.getConfigPath()).thenReturn(testSettingsPath);

        settingsIO.writeSetting("doesNotExist", "true");

        verify(log, times(1)).error(any(), eq(ErrorLogging.Severity.WARNING), contains("does not exist"));
    }

    @Test
    void defaultSettings() {
        when(valueStore.getConfigPath()).thenReturn(testSettingsPath);
        settingsIO.writeSetting("theme", "light");

        settingsIO.defaultSettings();

        assertEquals("black", settingsIO.readSetting("theme"));
    }


}
