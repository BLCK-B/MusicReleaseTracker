package JsonSettings;

import com.blck.MusicReleaseTracker.JsonSettings.SettingsModel;

import java.util.HashMap;
import java.util.Map;

public class HelperModelV1 {
    private String theme = "black";
    private boolean isoDates = false;

    private Map<String, Boolean> filters = new HashMap<>();

    public HelperModelV1() {
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

    public boolean getIsoDates() {
        return isoDates;
    }

    public void setIsoDates(boolean isoDates) {
        this.isoDates = isoDates;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}
