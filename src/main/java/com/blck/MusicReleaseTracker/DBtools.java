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

//class for essential tasks (not only DB)
public class DBtools {

    public final static SettingsStore settingsStore = new SettingsStore();
    public static void path() {
        String DBpath = null;
        String DBtemplatePath = null;
        String configPath = null;
        String configFolder = null;
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) { //Windows
            String appDataPath = System.getenv("APPDATA");
            DBpath = "jdbc:sqlite:" + appDataPath + File.separator + "MusicReleaseTracker" + File.separator + "musicdata.db";
            DBtemplatePath = "jdbc:sqlite:" + appDataPath + File.separator + "MusicReleaseTracker" + File.separator + "DBTemplate.db";
            configPath = appDataPath + File.separator + "MusicReleaseTracker" + File.separator + "MRTsettings.hocon";
            configFolder = appDataPath + File.separator + "MusicReleaseTracker" + File.separator;
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {  //Linux
            String userHome = System.getProperty("user.home");
            File folder = new File(userHome + File.separator + ".MusicReleaseTracker");
            if (!folder.exists())
                folder.mkdirs();
            DBpath = "jdbc:sqlite:" + userHome + File.separator + ".MusicReleaseTracker" + File.separator + "musicdata.db";
            DBtemplatePath = "jdbc:sqlite:" + userHome + File.separator + ".MusicReleaseTracker" + File.separator + "DBTemplate.db";
            configPath = userHome + File.separator + ".MusicReleaseTracker" + File.separator + "MRTsettings.hocon";
            configFolder = userHome + File.separator + ".MusicReleaseTracker" + File.separator;
        }
        else
            throw new UnsupportedOperationException("unsupported OS");

        settingsStore.setConfigFolder(configFolder);
        settingsStore.setConfigPath(configPath);
        settingsStore.setDBpath(DBpath);
        settingsStore.setDBTemplatePath(DBtemplatePath);
    }

    public static void createTables() {
        //on start: create DB if not exist, check DB structure, if different -> create new from template and refill with all data possible
        File templateFile = new File(settingsStore.getDBTemplatePath().substring(12));
        templateFile.delete();
        createDB(settingsStore.getDBpath());
        createDB(settingsStore.getDBTemplatePath());

        //if different structure, fill template artist table data from musicdata and then rename/delete, make new template
        //this only preserves "artists" data and assumes that the insertion logic will be adjusted after any changes...
        //made to the "artists" table: change in order of columns, adding/removing a column or changing a column's name
        Map<String, ArrayList<String>> DBMap = getDBStructure(settingsStore.getDBpath());
        Map<String, ArrayList<String>> DBtemplateMap = getDBStructure(settingsStore.getDBTemplatePath());
        if (!DBMap.equals(DBtemplateMap)) {
            try {
                Connection connDB = DriverManager.getConnection(settingsStore.getDBpath());
                Connection connDBtemplate = DriverManager.getConnection(settingsStore.getDBTemplatePath());

                //insert data from musicdata column to a template column
                String sql = "SELECT * FROM artists";
                Statement stmt = connDB.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                sql = "insert into artists(artistname, urlbrainz, urlbeatport, urljunodownload, urlyoutube) values(?, ?, ?, ?, ?)";
                PreparedStatement pstmt = connDBtemplate.prepareStatement(sql);
                ArrayList<String> columnList = DBMap.get("artists");
                //cycling table rows
                while (rs.next()) {
                    //fill sql query row data and add to batch
                    for (int i = 0; i < columnList.size(); i++) {
                        String column = columnList.get(i);
                        pstmt.setString(i + 1 , rs.getString(column));
                    }
                    pstmt.addBatch();
                }
                connDBtemplate.setAutoCommit(false);
                pstmt.executeBatch();
                connDBtemplate.commit();
                connDBtemplate.setAutoCommit(true);
                pstmt.clearBatch();
                pstmt.close();
                connDB.close();
                connDBtemplate.close();
            } catch(Exception e) {
                System.out.println("error updating DB file");
                e.printStackTrace();
            }
            try {
                File oldFile = new File(settingsStore.getDBpath().substring(12));
                File newFile = new File(settingsStore.getDBTemplatePath().substring(12));
                //delete old musicdata
                oldFile.delete();
                //rename template to musicdata
                newFile.renameTo(oldFile);
            } catch(Exception e) {
                System.out.println("error renaming/deleting DB files");
                e.printStackTrace();
            }
        }

    }

    private static void createDB(String path) {
        try {
            Connection conn = DriverManager.getConnection(path);

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

            sql = "CREATE TABLE IF NOT EXISTS youtube (\n"
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
                    + "	urljunodownload text,\n"
                    + "	urlyoutube text\n"
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
        } catch (SQLException e) {
            System.out.println("error creating DB file");
            e.printStackTrace();
        }
    }
    private static Map<String, ArrayList<String>> getDBStructure(String path) {
        HashMap<String, ArrayList<String>> tableMap = new HashMap<String, ArrayList<String>>();
        try {
            Connection conn = DriverManager.getConnection(path);
            String sql = "SELECT name FROM sqlite_master WHERE type='table'";
            Statement stmt = conn.createStatement();
            ResultSet rsTables = stmt.executeQuery(sql);
            ArrayList<String> tablesList = new ArrayList<String>();
            while(rsTables.next())
                tablesList.add(rsTables.getString(1));

            for (String tableName : tablesList) {
                ArrayList<String> tableColumnsList = new ArrayList<String>();
                ResultSet rsColumns = stmt.executeQuery("PRAGMA table_info(" + tableName + ")");
                while (rsColumns.next())
                    tableColumnsList.add(rsColumns.getString("name"));
                tableMap.put(tableName, tableColumnsList);
            }
            conn.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("error parsing DB structure");
            e.printStackTrace();
        }
        return tableMap;
    }

    public static void readConfig(String option) {
        //any reading from HOCON
        Config config = ConfigFactory.parseFile(new File(settingsStore.getConfigPath()));

        switch (option) {
            case ("filters") -> {
                ArrayList<String> filterWords = new ArrayList<>();
                Config filtersConfig = config.getConfig("filters");
                for (Map.Entry<String, ConfigValue> entry : filtersConfig.entrySet()) {
                    String filter = entry.getKey();
                    boolean enabled = entry.getValue().unwrapped().equals(true);
                    if (enabled)
                        filterWords.add(filter);
                }
                settingsStore.setFilterWords(filterWords);
            }
            case ("themes") -> {
                String theme = config.getString("theme");
                String accent = config.getString("accent");
                Map<String, String> themesMap = new HashMap<>();
                themesMap.put("theme", theme);
                themesMap.put("accent", accent);
                settingsStore.setThemes(themesMap);
            }
            case ("lastScrape") -> {
                String scrapeDate = config.getString("lastScrape");
                settingsStore.setScrapeDate(scrapeDate);
            }
        }
        config = null;
    }
    public static void writeSingleConfig(String name, String value) {
        //save single option state in HOCON
        Config config = ConfigFactory.parseFile(new File(DBtools.settingsStore.getConfigPath()));
        config = config.withValue(name, ConfigValueFactory.fromAnyRef(value));
        ConfigRenderOptions renderOptions = ConfigRenderOptions.defaults().setOriginComments(false).setJson(false).setFormatted(true);
        try (PrintWriter writer = new PrintWriter(new FileWriter(DBtools.settingsStore.getConfigPath()))) {
            writer.write(config.root().render(renderOptions));
        } catch (IOException e) {
            System.out.println("could not save " + name + " in config");
            e.printStackTrace();
        }
    }

    public static void updateSettings() {
        //create config if it does not exist, change to latest structure and transfer data if a different structure is detected

        // appData/MusicReleaseTracker/MRTsettings.hocon
        String configPath = settingsStore.getConfigPath();
        // appData/MusicReleaseTracker/
        String configFolder = settingsStore.getConfigFolder();
        //a default settings structure for current version
        String templateContent =
                "filters {\n" +
                "   Acoustic=false\n" +
                "   Extended=false\n" +
                "   Instrumental=false\n" +
                "   Remaster=false\n" +
                "   Remix=false\n" +
                "   VIP=false\n" +
                "}\n" +
                "theme=Black\n" +
                "accent=Classic\n" +
                "lastScrape=-\n";

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



