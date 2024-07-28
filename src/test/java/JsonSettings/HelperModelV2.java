package JsonSettings;

import com.blck.MusicReleaseTracker.JsonSettings.SettingsModel;

import java.util.HashMap;
import java.util.Map;

public class HelperModelV2 {
    private String theme = "black";
    private boolean isoDates = false;
    private boolean V2exclusive = true;

    private Map<String, Boolean> filters = new HashMap<>();

    public HelperModelV2() {
        filters.put("acoustic", false);
        filters.put("remix", false);
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

    public boolean getIsoDates() {
        return isoDates;
    }

    public void setIsoDates(boolean isoDates) {
        this.isoDates = isoDates;
    }

    public boolean getV2exclusive() {
        return V2exclusive;
    }

    public void setV2exclusive(boolean autoTheme) {
        this.V2exclusive = autoTheme;
    }
}
