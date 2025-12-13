
package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Core.AppConfig;
import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DB.MigrateDB;
import com.blck.MusicReleaseTracker.JsonSettings.SettingsIO;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * entry point class with startup logic
 */
@SpringBootApplication
@EnableConfigurationProperties(AppConfig.class)
public class Main {

    private final SettingsIO settingsIO;

    private final ErrorLogging log;

    private final MigrateDB manageDB;

    private final StartSetup startSetup;

    private final ValueStore store;

    private final AppConfig appConfig;

    @Autowired
    public Main(AppConfig appConfig, ValueStore store, SettingsIO settingsIO,
                ErrorLogging errorLogging, StartSetup startSetup, MigrateDB manageDB) {
        this.settingsIO = settingsIO;
        this.log = errorLogging;
        this.startSetup = startSetup;
        this.manageDB = manageDB;
        this.store = store;
        this.appConfig = appConfig;
    }

    static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    /**
     * CORS redirect mapping for dev cross-origin permission TODO: chrome again
     */
    @NullMarked
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
            }
        };
    }

    @NullMarked
    @Component
    public class StartupRunner implements CommandLineRunner {
        /**
         * entry method: load config, paths, create file structure, update DB and settings
         */
        @Override
        public void run(String... args) {
            System.out.println("""
                         __  __ ____ _____
                        |  \\/  |  _ \\_   _|
                        | |\\/| | |_) || |
                        | |  | |  _ < | |
                        |_|  |_|_| \\_\\|_|
                    """);
            store.setAppVersion(appConfig.version());
            startSetup.createPaths();
            startSetup.createDirs();
            manageDB.migrateDB(store.getDBpath(), store.getDBpathTemplate());
            settingsIO.updateSettings();
            if (settingsIO.readSetting("theme").equals("black")) { // TODO: future release
                settingsIO.writeSetting("theme", "dark");
            }
            store.setBackendReady();
        }
    }

}