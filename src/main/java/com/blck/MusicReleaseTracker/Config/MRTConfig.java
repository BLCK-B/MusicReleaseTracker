package com.blck.MusicReleaseTracker.Config;

import com.blck.MusicReleaseTracker.*;
import com.blck.MusicReleaseTracker.Simple.SSEController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// config class for spring boot to manage lifecycle of the beans, this helps avoid static classes
// each bean has a single instance, beans defined here can be used by other classes as dependencies
@Configuration
public class MRTConfig {

    @Bean
    public ValueStore valueStore() {
        return new ValueStore();
    }

    @Bean
    public ScrapeProcess scrapeProcess(ValueStore valueStore, ConfigTools configTools, DBtools dBtools, SSEController sseController) {
        return new ScrapeProcess(valueStore, configTools, dBtools, sseController);
    }

    @Bean
    public GUIController guiController(ValueStore valueStore, ScrapeProcess scrapeProcess, ConfigTools config, DBtools dBtools) {
        return new GUIController(valueStore, scrapeProcess, config, dBtools);
    }

    @Bean
    public ConfigTools configTools(ValueStore valueStore, DBtools dBtools) {
        return new ConfigTools(valueStore, dBtools);
    }

    @Bean
    public DBtools dBtools(ValueStore valueStore) {
        return new DBtools(valueStore);
    }

}
