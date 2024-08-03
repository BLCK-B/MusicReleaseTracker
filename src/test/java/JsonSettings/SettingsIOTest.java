package JsonSettings;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.JsonSettings.SettingsIO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
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
        lenient().when(valueStore.getConfigPath()).thenReturn(testSettingsPath);
    }

    @Test
    void readSetting() {
        assertEquals("false", settingsIO.readSetting("filterRemix"));
    }

    @Test
    void readNonexistentSetting() {
        settingsIO.readSetting("doesNotExist");

        verify(log, times(1)).error(any(), eq(ErrorLogging.Severity.WARNING), contains("does not exist"));
    }

    @Test
    void readSettingWhenEmptyFile() throws IOException {
        try (FileWriter writer = new FileWriter(settingsFile)) {
            writer.write("");
        }

        settingsIO.readSetting("filterRemix");

        verify(log, times(1)).error(any(), eq(ErrorLogging.Severity.WARNING), contains("does not exist"));
    }

    @Test
    void writeSetting() {
        settingsIO.writeSetting("theme", "light");

        assertEquals("light", settingsIO.readSetting("theme"));
    }

    @Test
    void writingSettingThatDoesNotExist() {
        settingsIO.writeSetting("doesNotExist", "true");

        verify(log, times(1)).error(any(), eq(ErrorLogging.Severity.WARNING), contains("does not exist"));
    }

    @Test
    void defaultSettings() {
        settingsIO.writeSetting("theme", "light");

        settingsIO.defaultSettings();

        assertEquals("black", settingsIO.readSetting("theme"));
    }

}
