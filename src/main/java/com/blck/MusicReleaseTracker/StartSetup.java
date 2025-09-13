
package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

@Component
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
     * Prepare system file paths:
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
                Paths.get(appDataPath, "musicdata.db")
        );
        store.setDBpathTemplate(
                Paths.get(appDataPath, "DBTemplate.db")
        );
        store.setErrorLogsPath(
                Paths.get(appDataPath, "errorlogs.txt")
        );
    }

    /**
     * Create AppData dirs, handle sqlite temporary files.
     */
    public void createDirs() {
        String appDataPath = store.getAppDataPath();
        try {
            new File(appDataPath).mkdirs();
            // junk folder because sqlite did not delete temp files in "temp"
            File tempfolder = new File(appDataPath + "temp");
            tempfolder.mkdirs();
            File thumbFolder = new File(appDataPath + "thumbnails");
            thumbFolder.mkdirs();
            Arrays.stream(tempfolder.listFiles()).forEach(File::delete);
            System.setProperty("org.sqlite.tmpdir", appDataPath + "temp");
        } catch (Exception e) {
            log.error(e, ErrorLogging.Severity.WARNING, "something went wrong in directory setup");
        }
    }

}
