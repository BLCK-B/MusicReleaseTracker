package com.blck.MusicReleaseTracker;

import com.typesafe.config.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.*;

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

//class for essential tasks
public class DBtools {

    public final static SettingsStore settingsStore = new SettingsStore();
    public static void path() {
        String DBpath = null;
        String configPath = null;
        String configFolder = null;
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) { //Windows
            String appDataPath = System.getenv("APPDATA");
            DBpath = "jdbc:sqlite:" + appDataPath + File.separator + "MusicReleaseTracker" + File.separator + "musicdata.db";
            configPath = appDataPath + File.separator + "MusicReleaseTracker" + File.separator + "MRTsettings.hocon";
            configFolder = appDataPath + File.separator + "MusicReleaseTracker" + File.separator;
            settingsStore.setConfigFolder(configFolder);
            settingsStore.setConfigPath(configPath);
            settingsStore.setDBpath(DBpath);
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {  //Linux
            String userHome = System.getProperty("user.home");
            File folder = new File(userHome + File.separator + ".MusicReleaseTracker");
            if (!folder.exists())
                folder.mkdirs();
            DBpath = "jdbc:sqlite:" + userHome + File.separator + ".MusicReleaseTracker" + File.separator + "musicdata.db";
            configPath = userHome + File.separator + ".MusicReleaseTracker" + File.separator + "MRTsettings.hocon";
            configFolder = userHome + File.separator + ".MusicReleaseTracker" + File.separator;
            settingsStore.setConfigFolder(configFolder);
            settingsStore.setConfigPath(configPath);
            settingsStore.setDBpath(DBpath);
        }
        else
            throw new UnsupportedOperationException("unsupported OS");
    }

    public static void createTables() throws SQLException {
        Connection conn = DriverManager.getConnection(settingsStore.getDBpath());

        String sql = "CREATE TABLE IF NOT EXISTS musicbrainz (\n"
                + "	song text NOT NULL,\n"
                + "	artist text NOT NULL,\n"
                + "	date text NOT NULL\n"
                + ");";
        Statement stmt = conn.createStatement();
        stmt.execute(sql);

        sql = "CREATE TABLE IF NOT EXISTS beatport (\n"
                + "	song text NOT NULL,\n"
                + "	artist text NOT NULL,\n"
                + "	date text NOT NULL,\n"
                + " type text NOT NULL\n"
                + ");";
        stmt = conn.createStatement();
        stmt.execute(sql);

        sql = "CREATE TABLE IF NOT EXISTS junodownload (\n"
                + "	song text NOT NULL,\n"
                + "	artist text NOT NULL,\n"
                + "	date text NOT NULL\n"
                + ");";
        stmt = conn.createStatement();
        stmt.execute(sql);

        sql = "CREATE TABLE IF NOT EXISTS artists (\n"
                + "	artistname text PRIMARY KEY,\n"
                + "	urlbrainz text,\n"
                + "	urlbeatport text,\n"
                + "	urljunodownload text\n"
                + ");";
        stmt = conn.createStatement();
        stmt.execute(sql);

        sql = "CREATE TABLE IF NOT EXISTS combview (\n"
                + "	song text NOT NULL,\n"
                + "	artist text NOT NULL,\n"
                + "	date text NOT NULL\n"
                + ");";
        stmt = conn.createStatement();
        stmt.execute(sql);

        conn.close();
        stmt.close();
    }

    public static void readCombviewConfig() {
        ArrayList<String> filterWords = new ArrayList<>();
        int entriesLimit = 0;
        //read filters
        Config config = ConfigFactory.parseFile(new File(settingsStore.getConfigPath()));
        Config filtersConfig = config.getConfig("filters");
        for (Map.Entry<String, ConfigValue> entry : filtersConfig.entrySet()) {
            String filter = entry.getKey();
            boolean enabled = entry.getValue().unwrapped().equals(true);
            if (enabled)
                filterWords.add(filter);
        }
        settingsStore.setFilterWords(filterWords);
    }

    public static void updateSettingsDB() {
        //create config if it does not exist, change to latest structure and transfer data if a different structure is detected
        //"version" is deprecated
        //DBversion may be deprecated in future, currently serves for any structural changes of DB

        // appData/MusicReleaseTracker/MRTsettings.hocon
        String configPath = settingsStore.getConfigPath();
        // appData/MusicReleaseTracker/
        String configFolder = settingsStore.getConfigFolder();
        //a default settings structure for current version
        final int DBversion = 1;
        String templateContent =
                "version=4\n" +
                "DBversion=" + DBversion + "\n" +
                        "filters {\n" +
                        "   Acoustic=false\n" +
                        "   Extended=false\n" +
                        "   Instrumental=false\n" +
                        "   Remaster=false\n" +
                        "   Remix=false\n" +
                        "   VIP=false\n" +
                        "}\n";

        //create template file / overwrite templateContent
        File templateFile = new File(configFolder + "/MRTsettingsTemplate.hocon");
        try (PrintWriter writer = new PrintWriter(new FileWriter(templateFile))) {
            writer.write(templateContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //create config file if not exist > write templateContent
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(configFile))) {
                writer.write(templateContent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        //comparing structure of existing config file and template
        Config config = ConfigFactory.parseFile(new File(configPath));
        Config templateConfig = ConfigFactory.parseFile(new File(configFolder + "/MRTsettingsTemplate.hocon"));

        ArrayList<String> configStructure = extractStructure(config);
        ArrayList<String> templateStructure = extractStructure(templateConfig);

        boolean different = false;
        //checking divergence
        for (String option : templateStructure) {
            if (!configStructure.contains(option)) {
                different = true;
                break;
            }
        }
        if (!different) {
            for (String option : configStructure) {
                if (!templateStructure.contains(option)) {
                    different = true;
                    break;
                }
            }
        }

        if (different) {
            //different structure > transfer all possible data from config to template
            // > overwrite old config with renamed template > create new template

            //transfer the states of options from MRTsettings to MRTsettingsTemplate
            for (Map.Entry<String, ConfigValue> configEntry : config.entrySet()) {
                String option = configEntry.getKey();
                ConfigValue value = configEntry.getValue();
                //string
                if (value.valueType() == ConfigValueType.BOOLEAN && templateConfig.hasPath(option)) {
                    boolean state = config.getBoolean(option);
                    templateConfig = templateConfig.withValue(option, ConfigValueFactory.fromAnyRef(state));
                }
                //boolean
                else if (value.valueType() == ConfigValueType.STRING && templateConfig.hasPath(option)) {
                    String stringValue = config.getString(option);
                    templateConfig = templateConfig.withValue(option, ConfigValueFactory.fromAnyRef(stringValue));
                }
            }
            //save the updated template config to MRTsettingsTemplate.hocon
            try (PrintWriter writer = new PrintWriter(new FileWriter(configFolder + "MRTsettingsTemplate.hocon"))) {
                ConfigRenderOptions renderOptions = ConfigRenderOptions.defaults().setOriginComments(false).setJson(false).setFormatted(true);
                String renderedConfig = templateConfig.root().render(renderOptions);
                writer.write(renderedConfig);
            } catch (IOException e) {
                throw new RuntimeException("Error while saving MRTsettingsTemplate.hocon", e);
            }
            //overwrite MRTsettings with MRTsettingsTemplate
            try {
                Files.copy(templateFile.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Error while replacing MRTsettings with MRTsettingsTemplate", e);
            }
            //default templateContent again
            try (PrintWriter writer = new PrintWriter(new FileWriter(templateFile))) {
                writer.write(templateContent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

   private static ArrayList<String> extractStructure(Config config) {
       ArrayList<String> structure = new ArrayList<>();
       Set<Map.Entry <String, ConfigValue> > entries = config.entrySet();
       for (Map.Entry<String, ConfigValue> entry : entries) {
           structure.add(entry.getKey());
       }
       return structure;
   }
}



