package com.blck.MusicReleaseTracker.JsonSettings;

import java.util.HashMap;
import java.util.Map;

public class SettingsModel {
    private String theme = "black";
    private String accent = "cactus";
    private String lastScrape = "-";
    private boolean isoDates = false;
    private boolean autoTheme = true;
    private Map<String, Boolean> filters = new HashMap<>();

    public SettingsModel() {
        filters.put("acoustic", false);
        filters.put("extended", false);
        filters.put("instrumental", false);
        filters.put("remaster", false);
        filters.put("remix", false);
        filters.put("vip", false);
    }

    public void setFilterState(String filter, boolean state) {
        if (filters.containsKey(filter))
            filters.put(filter, state);
    }

    public boolean getFilterState(String filter) {
        if (!filters.containsKey(filter))
            System.out.println("log info");
        return filters.get(filter);
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getAccent() {
        return accent;
    }

    public void setAccent(String accent) {
        this.accent = accent;
    }

    public String getLastScrape() {
        return lastScrape;
    }

    public void setLastScrape(String lastScrape) {
        this.lastScrape = lastScrape;
    }

    public boolean getIsoDates() {
        return isoDates;
    }

    public void setIsoDates(boolean isoDates) {
        this.isoDates = isoDates;
    }

    public boolean getAutoTheme() {
        return autoTheme;
    }

    public void setAutoTheme(boolean autoTheme) {
        this.autoTheme = autoTheme;
    }
}
