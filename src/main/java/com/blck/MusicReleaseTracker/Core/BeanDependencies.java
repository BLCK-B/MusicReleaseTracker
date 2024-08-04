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

package com.blck.MusicReleaseTracker.Core;

import com.blck.MusicReleaseTracker.DB.DBqueries;
import com.blck.MusicReleaseTracker.DB.ManageMigrateDB;
import com.blck.MusicReleaseTracker.FrontendAPI.SSEController;
import com.blck.MusicReleaseTracker.GUIController;
import com.blck.MusicReleaseTracker.JsonSettings.SettingsIO;
import com.blck.MusicReleaseTracker.Scraping.ScrapeProcess;
import com.blck.MusicReleaseTracker.StartSetup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanDependencies {

    @Bean
    public ValueStore valueStore() {
        return new ValueStore();
    }

    @Bean
    public ErrorLogging errorLogging(ValueStore store) {
        return new ErrorLogging(store);
    }

    @Bean
    public SettingsIO settingsIO(ValueStore valueStore, ErrorLogging errorLogging) {
        return new SettingsIO(valueStore, errorLogging);
    }

    @Bean
    public ScrapeProcess scrapeProcess(ErrorLogging errorLogging, DBqueries dBqueries,
                                       SSEController sseController) {
        return new ScrapeProcess(errorLogging, dBqueries, sseController);
    }

    @Bean
    public GUIController guiController(ValueStore valueStore, ErrorLogging errorLogging,
                                       ScrapeProcess scrapeProcess, SettingsIO settingsIO,
                                       ManageMigrateDB manageMigrateDB, DBqueries dBqueries) {
        return new GUIController(valueStore, errorLogging, scrapeProcess, settingsIO, dBqueries, manageMigrateDB);
    }

    @Bean
    public ManageMigrateDB manageMigrateDB(ValueStore valueStore, ErrorLogging errorLogging) {
        return new ManageMigrateDB(valueStore, errorLogging);
    }

    @Bean
    public DBqueries dBqueries(ValueStore valueStore, ErrorLogging errorLogging, SettingsIO settingsIO, ManageMigrateDB manageMigrateDB) {
        return new DBqueries(valueStore, errorLogging, settingsIO, manageMigrateDB);
    }

    @Bean
    public StartSetup startSetup(ValueStore valueStore, ErrorLogging errorLogging) {
        return new StartSetup(valueStore, errorLogging);
    }

}
