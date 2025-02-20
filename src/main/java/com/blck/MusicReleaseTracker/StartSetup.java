/*
 *         MusicReleaseTracker
 *         Copyright (C) 2023 - 2025 BLCK
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

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

public class StartSetup {
    final String slash = File.separator;
    private final ValueStore store;
    private final ErrorLogging log;

    @Autowired
    public StartSetup(ValueStore valueStore, ErrorLogging errorLogging) {
        this.store = valueStore;
        this.log = errorLogging;
    }

    /**
     *  Prepare system file paths:
     *  <ul>
     *     <li>AppData / user.home</li>
     *     <li>DB</li>
     *     <li>settings</li>
     *     <li>logs</li>
     * </ul>
     */
    public void createPaths() {
        String appData = null;
        final String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win"))
            appData = System.getenv("APPDATA");
        else if (os.contains("nix") || os.contains("nux") || os.contains("mac"))
            appData = System.getProperty("user.home");
        else
            throw new UnsupportedOperationException("unsupported OS");

        String appDataPath = appData + slash + "MusicReleaseTracker" + slash;

        store.setAppDataPath(appDataPath);
        store.setConfigPath(
            Paths.get(appDataPath, "MRTsettings.json")
        );
        store.setDBpath(
            Paths.get(appDataPath,  "musicdata.db")
        );
        store.setDBpathTemplate(
            Paths.get(appDataPath, "DBTemplate.db")
        );
        store.setErrorLogsPath(
            Paths.get(appDataPath, "errorlogs.txt")
        );
    }

    /**
     *  Create AppData dir, handle sqlite temporary files.
     */
    public void createDirs() {
        String appDataPath = store.getAppDataPath();
        try {
            new File(appDataPath).mkdirs();
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
