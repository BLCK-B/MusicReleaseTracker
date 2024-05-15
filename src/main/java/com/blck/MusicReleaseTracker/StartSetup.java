package com.blck.MusicReleaseTracker;


import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

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
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win"))
            appData = System.getenv("APPDATA");
        else if (os.contains("nix") || os.contains("nux") || os.contains("mac"))
            appData = System.getProperty("user.home");
        else
            throw new UnsupportedOperationException("unsupported OS");

        String appDataPath = appData + slash + "MusicReleaseTracker" + slash;
        String DBpath = "jdbc:sqlite:" + appDataPath + "musicdata.db";
        String configPath = appDataPath + "MRTsettings.hocon";
        String errorLogsPath = appDataPath + "errorlogs.txt";

        store.setAppDataPath(appDataPath);
        store.setConfigPath(configPath);
        store.setDBpath(DBpath);
        store.setErrorLogsPath(errorLogsPath);
    }

    private void createDirs() {
        String appDataPath = store.getAppDataPath();
        try {
            File folder = new File(appDataPath);
            if (!folder.exists())
                folder.mkdirs();
            // junk folder because sqlite did not delete temp files in "temp"
            File tempfolder = new File(appDataPath + "temp");
            if (!tempfolder.exists())
                tempfolder.mkdirs();
            for (File file : tempfolder.listFiles())
                file.delete();
            System.setProperty("org.sqlite.tmpdir", appDataPath + "temp");
        } catch (Exception e) {
            log.error(e, ErrorLogging.Severity.WARNING, "something went wrong in directory setup");
        }
    }

}
