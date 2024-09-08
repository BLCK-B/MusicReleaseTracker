/*
 *         MusicReleaseTracker
 *         Copyright (C) 2023 - 2024 BLCK
 *         This program is free software: you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation, either version 3 of the License, or
 *         (at your option) any later version.
 *         This program is distributed in the hope that it will be useful,
 *         but WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *         GNU General Public License for more details.
 *         You should have received a copy of the GNU General Public License
 *         along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Core.AppConfig;
import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DB.MigrateDB;
import com.blck.MusicReleaseTracker.JsonSettings.SettingsIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** entry point class with startup logic */
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

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**").allowedOrigins("http://localhost:5173");
            }
        };
    }

    @Component
    public class StartupRunner implements CommandLineRunner {

        // on startup of springboot server
        @Override
        public void run(String... args) {
            System.out.println("----------LOCAL SERVER STARTED----------");
            System.out.println("""
                 __  __ ____ _____
                |  \\/  |  _ \\_   _|
                | |\\/| | |_) || |
                | |  | |  _ < | |
                |_|  |_|_| \\_\\|_|
            """);
            store.setAppVersion(appConfig.version());
            startSetup.createPathsAndDirs();
            manageDB.migrateDB();
            settingsIO.updateSettings();
        }
    }

}