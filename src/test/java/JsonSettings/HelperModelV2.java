package JsonSettings;

import com.blck.MusicReleaseTracker.JsonSettings.SettingsModel;

import java.util.HashMap;
import java.util.Map;

public class HelperModelV2 extends SettingsModel {
    private String theme = "Black";
    private boolean isoDates = false;
    private boolean autoTheme = true;

    private Map<String, Boolean> filters = new HashMap();

    public HelperModelV2() {
        filters.put("Acoustic", false);
        filters.put("Remix", false);
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

    public boolean getAutoTheme() {
        return autoTheme;
    }

    public void setAutoTheme(boolean autoTheme) {
        this.autoTheme = autoTheme;
    }
}
