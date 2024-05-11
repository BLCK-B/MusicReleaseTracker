package com.blck.MusicReleaseTracker.Core;

import com.blck.MusicReleaseTracker.*;
import com.blck.MusicReleaseTracker.Scrapers.Scraper;
import com.blck.MusicReleaseTracker.Simple.SSEController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** config class for spring boot to manage lifecycle of the beans, this helps avoid static classes
    each bean has a single instance, beans defined here can be used by other classes as dependencies */
@Configuration
public class MRTConfig {

    @Bean
    public ValueStore valueStore() {
        return new ValueStore();
    }

    @Bean
    public ErrorLogging errorLogging(ValueStore store) {
        return new ErrorLogging(store);
    }

    @Bean
    public ScrapeProcess scrapeProcess(ValueStore valueStore, ErrorLogging errorLogging, ConfigTools configTools, DBtools dBtools, SSEController sseController) {
        return new ScrapeProcess(valueStore, errorLogging, configTools, dBtools, sseController);
    }

    @Bean
    public Scraper scraperParent(ValueStore valueStore, ErrorLogging errorLogging) {
        return new Scraper(valueStore, errorLogging);
    }

    @Bean
    public GUIController guiController(ValueStore valueStore, ErrorLogging errorLogging, ScrapeProcess scrapeProcess, ConfigTools config, DBtools dBtools) {
        return new GUIController(valueStore, errorLogging, scrapeProcess, config, dBtools);
    }

    @Bean
    public ConfigTools configTools(ValueStore valueStore, ErrorLogging errorLogging) {
        return new ConfigTools(valueStore, errorLogging);
    }

    @Bean
    public DBtools dBtools(ValueStore valueStore, ErrorLogging errorLogging) {
        return new DBtools(valueStore, errorLogging);
    }


}
