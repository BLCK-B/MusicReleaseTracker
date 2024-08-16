/*
 *         MusicReleaseTracker
 *         Copyright (C) 2023 - 2024 BLCK
 *         This program is free software: you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation, either version 3 of the License, or
 *         (at your option) any later version.
 *         This program is distributed in the hope that it will be useful,
 *         but WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *         GNU General Public License for more details.
 *         You should have received a copy of the GNU General Public License
 *         along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
    ArgumentCaptor<String> stringCaptor;
    @Captor
    ArgumentCaptor<Path> pathCaptor;

    @Test
    void DBpathIntegrity() {
        String DBpath = File.separator + "MusicReleaseTracker" + File.separator + "musicdata.db";

        startSetup.createPaths();

        verify(store).setDBpath(stringCaptor.capture());
        assertTrue(stringCaptor.getValue().contains(DBpath));
        assertTrue(stringCaptor.getValue().contains("jdbc:sqlite:"));
    }

    @Test
    void errorLogPathIntegrity() {
        startSetup.createPaths();

        verify(store).setErrorLogsPath(pathCaptor.capture());
        assertTrue(pathCaptor.getValue().toString().contains(File.separator + "errorlogs.txt"));
    }

}
