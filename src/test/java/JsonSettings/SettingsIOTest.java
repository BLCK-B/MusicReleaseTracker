package JsonSettings;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.JsonSettings.SettingsIO;
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
    void readNonexistentSettingLogsWarning() {
        settingsIO.readSetting("doesNotExist");

        verify(log, times(1)).error(any(), eq(ErrorLogging.Severity.WARNING), contains("does not exist"));
    }

    @Test
    void readSettingWhenEmptyFileLogsWarning() throws IOException {
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
    void writingSettingThatDoesNotExistLogsWarning() {
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
