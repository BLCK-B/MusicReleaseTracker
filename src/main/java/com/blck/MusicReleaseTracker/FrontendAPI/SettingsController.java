package com.blck.MusicReleaseTracker.FrontendAPI;

import com.blck.MusicReleaseTracker.ServiceLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SettingsController {

    private final ServiceLayer serviceLayer;

    @Autowired
    public SettingsController(ServiceLayer serviceLayer) {
        this.serviceLayer = serviceLayer;
    }

    @PutMapping("/setting")
    public void setting(@RequestParam String name, @RequestParam String value) {
        serviceLayer.setSetting(name, value);
    }

    @GetMapping("/themeConfig")
    public Map<String, String> themeConfig() {
        return serviceLayer.getThemeConfig();
    }

    @GetMapping("/settingsData")
    public Map<String, String> settingsData() {
        return serviceLayer.settingsOpened();
    }

    @PostMapping("/resetSettings")
    public void resetSettings() {
        serviceLayer.resetSettings();
    }
}
