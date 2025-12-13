package com.blck.MusicReleaseTracker.Controllers;

import com.blck.MusicReleaseTracker.ServiceLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MiscController {

    private final ServiceLayer serviceLayer;

    @Autowired
    public MiscController(ServiceLayer serviceLayer) {
        this.serviceLayer = serviceLayer;
    }

    @GetMapping("/isBackendReady")
    public boolean isBackendReady() {
        return serviceLayer.isBackendReady();
    }

    @GetMapping("/appVersion")
    public String appVersion() {
        return serviceLayer.getAppVersion();
    }

    @GetMapping("/isNewUpdate")
    public boolean isNewUpdate() {
        return serviceLayer.isNewUpdate();
    }
}
