package com.blck.MusicReleaseTracker.JsonSettings;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.ValueStore;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SettingsIOTest {

    final static Path testResources = Paths.get("src", "test", "testresources");
    final static Path testSettingsPath = Paths.get("src", "test", "testresources", "testSettings.json");
    final static File settingsFile = new File(testSettingsPath.toString());

    @Mock
    ValueStore valueStore;
    @Mock
    ErrorLogging log;
    @InjectMocks
    SettingsIO settingsIO;

    @BeforeAll
    static void init() throws IOException {
        if (!Files.exists(testResources))
            Files.createDirectory(testResources);
    }

    @BeforeEach
    void setUp() {
        try (FileWriter writer = new FileWriter(settingsFile)) {
            settingsFile.createNewFile();
            writer.write("""
                    {
                      "theme" : "dark",
                      "filterRemix" : false
                    }
                    """);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        lenient().when(valueStore.getConfigPath()).thenReturn(testSettingsPath);
    }

    @Test
    void readJsonFile() {
        assertTrue(settingsIO.readJsonFile(settingsFile).size() > 1);
    }


    @Test
    void readJsonFileParseExceptionTriggersReset() throws IOException {
        try (FileWriter writer = new FileWriter(settingsFile)) {
            writer.write("broken json ^%gb#%.#*-*");
        }

        var json = settingsIO.readJsonFile(settingsFile);

        assertNull(json);
        verify(log, times(1)).error(any(), eq(ErrorLogging.Severity.WARNING), contains("error reading"));
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

        assertEquals("dark", settingsIO.readSetting("theme"));
    }

}
