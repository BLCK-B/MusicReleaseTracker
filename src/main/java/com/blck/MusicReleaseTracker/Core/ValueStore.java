
package com.blck.MusicReleaseTracker.Core;

import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * a central value store of variables shared among classes
 */
@Component
public class ValueStore {

    private String appDataPath;

    private Path DBpath;

    private Path DBpathTemplate;

    private Path configPath;

    private Path errorLogsPath;

    private String appVersion;

    private boolean backendReady = false;

    public boolean isBackendReady() {
        return backendReady;
    }

    public void setBackendReady() {
        backendReady = true;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getAppDataPath() {
        return appDataPath;
    }

    public void setAppDataPath(String appDataPath) {
        this.appDataPath = appDataPath;
    }

    public Path getDBpath() {
        return DBpath;
    }

    public void setDBpath(Path DBpath) {
        this.DBpath = DBpath;
    }

    public Path getDBpathTemplate() {
        return DBpathTemplate;
    }

    public void setDBpathTemplate(Path DBpathTemplate) {
        this.DBpathTemplate = DBpathTemplate;
    }

    public Path getConfigPath() {
        return configPath;
    }

    public void setConfigPath(Path configPath) {
        this.configPath = configPath;
    }

    public Path getErrorLogsPath() {
        return errorLogsPath;
    }

    public void setErrorLogsPath(Path errorLogsPath) {
        this.errorLogsPath = errorLogsPath;
    }

    @Override
    public String toString() {
        return "value store";
    }

}
