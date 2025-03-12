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

package com.blck.MusicReleaseTracker.Core;

import org.springframework.stereotype.Component;

import java.nio.file.Path;

/** a central value store of variables shared among classes */
@Component
public class ValueStore {

    private String appDataPath;
    private Path DBpath;
    private Path DBpathTemplate;
    private Path configPath;
    private Path errorLogsPath;
    private String scrapeDate;
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

    public String getScrapeDate() {
        return scrapeDate;
    }

    public void setScrapeDate(String scrapeDate) {
        this.scrapeDate = scrapeDate;
    }

    @Override
    public String toString() {
        return "value store";
    }

}
