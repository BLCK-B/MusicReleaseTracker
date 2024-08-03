package com.blck.MusicReleaseTracker.Core;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

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

/** a central value store of variables shared among classes */
@Component
public class ValueStore {

    public ValueStore() {
    }

    private String appDataPath;
    private String DBpath;
    private Path configPath;
    private Path errorLogsPath;
    private String scrapeDate;

    public void setAppDataPath(String appDataPath) {
        this.appDataPath = appDataPath;
    }
    public String getAppDataPath() {
        return appDataPath;
    }
    public void setDBpath(String DBpath) {
        this.DBpath = DBpath;
    }
    public String getDBpathString() {
        return DBpath.toString();
    }
    public void setConfigPath(Path configPath) {
        this.configPath = configPath;
    }
    public Path getConfigPath() {
        return configPath;
    }
    public void setErrorLogsPath(Path errorLogsPath) {
        this.errorLogsPath = errorLogsPath;
    }
    public Path getErrorLogsPath() {
        return errorLogsPath;
    }
    public void setScrapeDate(String scrapeDate) {
        this.scrapeDate = scrapeDate;
    }
    public String getScrapeDate() {
        return scrapeDate;
    }

    @Override
    public String toString() {
        return "value store";
    }

}
