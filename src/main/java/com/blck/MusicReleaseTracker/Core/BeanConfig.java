package com.blck.MusicReleaseTracker.Core;

import com.blck.MusicReleaseTracker.*;
import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.DB.ManageMigrateDB;
import com.blck.MusicReleaseTracker.Scraping.ScrapeProcess;
import com.blck.MusicReleaseTracker.FrontendAPI.SSEController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** config class for spring boot to manage lifecycle of the beans, this helps avoid static classes
    each bean has a single instance, beans defined here can be used by other classes as dependencies */
@Configuration
public class BeanConfig {

    @Bean
    public ValueStore valueStore() {
        return new ValueStore();
    }

    @Bean
    public ErrorLogging errorLogging(ValueStore store) {
        return new ErrorLogging(store);
    }

    @Bean
    public ScrapeProcess scrapeProcess(ErrorLogging errorLogging, DBqueries dBqueries, SSEController sseController) {
        return new ScrapeProcess(errorLogging, dBqueries, sseController);
    }

    @Bean
    public GUIController guiController(ValueStore valueStore, ErrorLogging errorLogging,
                                       ScrapeProcess scrapeProcess, ConfigTools config,
                                       ManageMigrateDB manageMigrateDB, DBqueries dBqueries) {
        return new GUIController(valueStore, errorLogging, scrapeProcess, config, dBqueries, manageMigrateDB);
    }

    @Bean
    public ConfigTools configTools(ValueStore valueStore, ErrorLogging errorLogging) {
        return new ConfigTools(valueStore, errorLogging);
    }

    @Bean
    public ManageMigrateDB manageMigrateDB(ValueStore valueStore, ErrorLogging errorLogging) {
        return new ManageMigrateDB(valueStore, errorLogging);
    }

    @Bean
    public DBqueries dBqueries(ValueStore valueStore, ErrorLogging errorLogging, ConfigTools configTools, ManageMigrateDB manageMigrateDB) {
        return new DBqueries(valueStore, errorLogging, configTools, manageMigrateDB);
    }

    @Bean
    public StartSetup startSetup(ValueStore valueStore, ErrorLogging errorLogging) {
        return new StartSetup(valueStore, errorLogging);
    }

}
