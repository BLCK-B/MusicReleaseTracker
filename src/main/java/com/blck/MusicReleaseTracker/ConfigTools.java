package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.typesafe.config.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*      MusicReleaseTracker
        Copyright (C) 2023 BLCK
        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.
        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.
        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>.*/

/** class that manages config */
public class ConfigTools {

    private final ValueStore store;
    private final ErrorLogging log;

    public enum configOptions {
        filters, themes, lastScrape, longTimeout, isoDates, autoTheme
    }

    @Autowired
    public ConfigTools(ValueStore valueStore, ErrorLogging errorLogging) {
        this.store = valueStore;
        this.log = errorLogging;
    }

    public void writeSingleConfig(String name, String value) {
        // save single string option state in config file
        Config config = ConfigFactory.parseFile(new File(store.getConfigPath()));
        ConfigValue configValue;
        if (value.equals("true") || value.equals("false"))
            configValue = ConfigValueFactory.fromAnyRef(Boolean.parseBoolean(value));
        else
            configValue = ConfigValueFactory.fromAnyRef(value);

        config = config.withValue(name, configValue);
        ConfigRenderOptions renderOptions = ConfigRenderOptions.defaults().setOriginComments(false).setJson(false).setFormatted(true);
        try (PrintWriter writer = new PrintWriter(new FileWriter(store.getConfigPath()))) {
            writer.write(config.root().render(renderOptions));
        } catch (IOException e) {
            log.error(e, ErrorLogging.Severity.WARNING, "could not save " + name + " in config");
        }
    }

    public void readConfig(configOptions o) {
        // any reading from config file
        try {
            Config config = ConfigFactory.parseFile(new File(store.getConfigPath()));
            switch (o) {
                case filters -> {
                    ArrayList<String> filterWords = new ArrayList<>();
                    Config filtersConfig = config.getConfig("filters");
                    for (Map.Entry<String, ConfigValue> entry : filtersConfig.entrySet()) {
                        String filter = entry.getKey();
                        if (entry.getValue().unwrapped().equals(true))
                            filterWords.add(filter);
                    }
                    store.setFilterWords(filterWords);
                }
                case themes -> {
                    Map<String, String> themesMap = new HashMap<>();
                    themesMap.put("theme", config.getString("theme"));
                    themesMap.put("accent", config.getString("accent"));
                    store.setThemes(themesMap);
                }
                case lastScrape -> store.setScrapeDate(config.getString("lastScrape"));
                case isoDates -> store.setIsoDates(config.getBoolean("isoDates"));
                case autoTheme -> store.setAutoTheme(config.getBoolean("autoTheme"));
            }
            config = null;
        }
        catch (Exception e) {
            log.error(e, ErrorLogging.Severity.WARNING, "could not read config: " + o);
        }
    }

    public void updateSettings() {
        // create config if it does not exist, change to latest structure and transfer data if structure is different

        // appData/MusicReleaseTracker/MRTsettings.hocon
        final String configPath = store.getConfigPath();
        // appData/MusicReleaseTracker/
        final String configFolder = store.getAppDataPath();
        // a default settings structure for current version
        final String templateContent = """
            filters {
                Acoustic=false
                Extended=false
                Instrumental=false
                Remaster=false
                Remix=false
                VIP=false
            }
            theme=Black
            accent=Lavender
            lastScrape=-
            isoDates=false
            autoTheme=true
            """;
        // create template file / overwrite templateContent
        File templateFile = new File(configFolder + "/MRTsettingsTemplate.hocon");
        try (PrintWriter writer = new PrintWriter(new FileWriter(templateFile))) {
            writer.write(templateContent);
        } catch (IOException e) {
            log.error(e, ErrorLogging.Severity.SEVERE, "could not overwrite templatecontent");
        }
        // create config file if not exist > write templateContent
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(configFile))) {
                writer.write(templateContent);
            } catch (IOException e) {
                log.error(e, ErrorLogging.Severity.SEVERE, "could not overwrite configfile");
            }
        }
        // comparing structure of existing config file and template
        Config config = ConfigFactory.parseFile(new File(configPath));
        Config templateConfig = ConfigFactory.parseFile(new File(configFolder + "/MRTsettingsTemplate.hocon"));

        ArrayList<String> configStructure = extractStructure(config);
        ArrayList<String> templateStructure = extractStructure(templateConfig);

        // if the settings and template options differ
        if (!templateStructure.containsAll(configStructure) || !configStructure.containsAll(templateStructure)) {
            // different structure > transfer all possible data from config to template
            // > overwrite old config with renamed template > create new template

            // transfer the states of options from MRTsettings to MRTsettingsTemplate
            for (Map.Entry<String, ConfigValue> configEntry : config.entrySet()) {
                String option = configEntry.getKey();
                ConfigValue value = configEntry.getValue();
                // string
                if (value.valueType() == ConfigValueType.BOOLEAN && templateConfig.hasPath(option)) {
                    boolean state = config.getBoolean(option);
                    templateConfig = templateConfig.withValue(option, ConfigValueFactory.fromAnyRef(state));
                }
                // boolean
                else if (value.valueType() == ConfigValueType.STRING && templateConfig.hasPath(option)) {
                    String stringValue = config.getString(option);
                    templateConfig = templateConfig.withValue(option, ConfigValueFactory.fromAnyRef(stringValue));
                }
            }
            // save the updated template config to MRTsettingsTemplate.hocon
            try (PrintWriter writer = new PrintWriter(new FileWriter(configFolder + "MRTsettingsTemplate.hocon"))) {
                ConfigRenderOptions renderOptions = ConfigRenderOptions.defaults().setOriginComments(false).setJson(false).setFormatted(true);
                String renderedConfig = templateConfig.root().render(renderOptions);
                writer.write(renderedConfig);
            } catch (IOException e) {
                log.error(e, ErrorLogging.Severity.SEVERE, "error while saving MRTsettingsTemplate.hocon");
            }
            // overwrite MRTsettings with MRTsettingsTemplate
            try {
                Files.copy(templateFile.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                log.error(e, ErrorLogging.Severity.SEVERE, "error while replacing MRTsettings with MRTsettingsTemplate");
            }
            // default templateContent again
            try (PrintWriter writer = new PrintWriter(new FileWriter(templateFile))) {
                writer.write(templateContent);
            } catch (IOException e) {
                log.error(e, ErrorLogging.Severity.WARNING, "error defaulting MRTsettingsTemplate.hocon");
            }
        }
    }

    private ArrayList<String> extractStructure(Config config) {
        ArrayList<String> structure = new ArrayList<>();
        Set<Map.Entry <String, ConfigValue> > entries = config.entrySet();
        for (Map.Entry<String, ConfigValue> entry : entries) {
            structure.add(entry.getKey());
        }
        return structure;
    }

    public void resetSettings() {
        // default the settings
        File configFile = new File(store.getConfigPath());
        configFile.delete();
        updateSettings();
    }
}
