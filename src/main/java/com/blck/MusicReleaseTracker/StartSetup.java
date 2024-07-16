package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

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

public class StartSetup {
    private final ValueStore store;
    private final ErrorLogging log;
    final String slash = File.separator;

    @Autowired
    public StartSetup(ValueStore valueStore, ErrorLogging errorLogging) {
        this.store = valueStore;
        this.log = errorLogging;
    }

    public void initializeSystem() {
        createPaths();
        createDirs();
    }

    private void createPaths() {
        String appData = null;
        final String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win"))
            appData = System.getenv("APPDATA");
        else if (os.contains("nix") || os.contains("nux") || os.contains("mac"))
            appData = System.getProperty("user.home");
        else
            throw new UnsupportedOperationException("unsupported OS");

        Path appDataPath = Paths.get(appData, "MusicReleaseTracker", "");
        String DBpath = "jdbc:sqlite:" + Paths.get(appDataPath.toString(), "musicdata.db");
        Path configPath = Paths.get(appDataPath.toString(), "MRTsettings.hocon");
        Path errorLogsPath = Paths.get(appDataPath.toString(), "errorlogs.txt");

        store.setAppDataPath(appDataPath);
        store.setConfigPath(configPath);
        store.setDBpath(DBpath);
        store.setErrorLogsPath(errorLogsPath);
    }

    private void createDirs() {
        Path appDataPath = store.getAppDataPath();
        try {
            appDataPath.toFile().mkdirs();

            // junk folder because sqlite did not delete temp files in "temp"
            File tempfolder = new File(appDataPath + "temp");
            tempfolder.mkdirs();
            Arrays.stream(tempfolder.listFiles()).forEach(File::delete);
            System.setProperty("org.sqlite.tmpdir", appDataPath + "temp");
        } catch (Exception e) {
            log.error(e, ErrorLogging.Severity.WARNING, "something went wrong in directory setup");
        }
    }

}
