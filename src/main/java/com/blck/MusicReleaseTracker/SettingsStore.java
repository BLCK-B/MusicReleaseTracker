package com.blck.MusicReleaseTracker;

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

import java.util.ArrayList;

public class SettingsStore {

    private String DBpath;
    private String configPath;
    private String configFolder;
    private ArrayList<String> filterWords;
    private int entriesLimit;

    public SettingsStore() {
    }

    public void setDBpath(String DBpath) {
        this.DBpath = DBpath;
    }
    public String getDBpath() {
        return DBpath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }
    public String getConfigPath() {
        return configPath;
    }

    public void setConfigFolder(String configFolder) {
        this.configFolder = configFolder;
    }
    public String getConfigFolder() {
        return configFolder;
    }

    public void setFilterWords(ArrayList<String> filterWords) {
        this.filterWords = filterWords;
    }
    public ArrayList<String> getFilterWords() {
        return filterWords;
    }

    public void setEntriesLimit(int entriesLimit) {
        this.entriesLimit = entriesLimit;
    }
    public int getEntriesLimit() {
        return entriesLimit;
    }

    @Override
    public String toString() {
        return null;
    }


}
