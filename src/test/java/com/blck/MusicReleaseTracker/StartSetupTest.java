package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Core.ValueStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class StartSetupTest {

    final static String testDir = Paths.get("src", "test", "testresources") + File.separator;
    final static String testDirAppData = testDir + "appDataDir" + File.separator;

    @Mock
    ValueStore store;
    @InjectMocks
    StartSetup startSetup;

    @Captor
    ArgumentCaptor<Path> pathCaptor;

    @Test
    void DBpathIntegrity() {
        String DBpath = File.separator + "MusicReleaseTracker" + File.separator + "musicdata.db";

        startSetup.createPaths();

        verify(store).setDBpath(pathCaptor.capture());
        assertTrue(pathCaptor.getValue().toString().contains(DBpath));
    }

    @Test
    void errorLogPathIntegrity() {
        startSetup.createPaths();

        verify(store).setErrorLogsPath(pathCaptor.capture());
        assertTrue(pathCaptor.getValue().toString().contains(File.separator + "errorlogs.txt"));
    }

}
