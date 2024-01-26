package com.blck.MusicReleaseTracker;

import com.typesafe.config.*;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

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
    public static void logError(Exception e, String level, String message) {
        Logger logger = Logger.getLogger(ErrorLogging.class.getName());
        String errorLogs = settingsStore.getErrorLogs();
        try {
            //filehandler logging the error
            FileHandler fileHandler = new FileHandler(errorLogs, true);
            fileHandler.setFormatter(new SimpleFormatter());
            //clear log when it reaches approx 0.1 MB
            final long logFileSize = Files.size(Paths.get(errorLogs));
            if (logFileSize > 100000) {
                Files.write(Paths.get(errorLogs), new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
            }
            //log the error
            logger.addHandler(fileHandler);
            switch (level) {
                case ("SEVERE") -> logger.log(Level.SEVERE, message, e);
                case ("WARNING") -> logger.log(Level.WARNING, message, e);
                case ("INFO") -> logger.log(Level.INFO, message);
            }
            fileHandler.close();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
        if (level.equals("SEVERE")) {
            throw new RuntimeException(e);
        }
    }

    public static void path() {
        String appData = null;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) //Windows
            appData = System.getenv("APPDATA");
        else if (os.contains("nix") || os.contains("nux") || os.contains("mac"))  //Linux
            appData = System.getProperty("user.home");
        else
            throw new UnsupportedOperationException("unsupported OS");
        //assemble paths for all appdata files
        File folder = new File(appData + File.separator + "MusicReleaseTracker");
        if (!folder.exists())
            folder.mkdirs();
        //junk folder because sqlite did not delete temp files in "temp"
        File tempfolder = new File(appData + File.separator + "MusicReleaseTracker" + File.separator + "temp");
        if (!tempfolder.exists())
            tempfolder.mkdirs();
        File[] tempfiles = tempfolder.listFiles();
        for (File file : tempfiles) {
            file.delete();
        }
        System.setProperty("org.sqlite.tmpdir", appData + File.separator + "MusicReleaseTracker" + File.separator + "temp");

        String basePath = appData + File.separator + "MusicReleaseTracker" + File.separator;
        String DBpath =             "jdbc:sqlite:" + basePath + "musicdata.db";
        String DBtemplatePath =     "jdbc:sqlite:" + basePath + "DBTemplate.db";
        String configPath =         basePath + "MRTsettings.hocon";
        String configFolder =       basePath + File.separator;
        String errorLogs =          basePath + "errorlogs.txt";
        //save paths to settingsStore
        settingsStore.setConfigFolder(configFolder);
        settingsStore.setConfigPath(configPath);
        settingsStore.setDBpath(DBpath);
        settingsStore.setDBTemplatePath(DBtemplatePath);
        settingsStore.setErrorLogs(errorLogs);
    }

    public static void createTables() {
        //on start: create DB if not exist, check DB structure, if different -> create new from template and refill with all data possible
        File templateFile = new File(settingsStore.getDBTemplatePath().substring(12));
        templateFile.delete();
        createDB(settingsStore.getDBpath());
        createDB(settingsStore.getDBTemplatePath());

        //if different structure, fill template artist table data from musicdata and then rename/delete, make new template
        //this only preserves "artists" data and assumes that the insertion logic will be adjusted after any changes
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

                sql = "insert into artists(artistname, urlmusicbrainz, urlbeatport, urljunodownload, urlyoutube) values(?, ?, ?, ?, ?)";
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
                logError(e, "SEVERE", "error updating DB file");
            }
            try {
                File oldFile = new File(settingsStore.getDBpath().substring(12));
                File newFile = new File(settingsStore.getDBTemplatePath().substring(12));
                //delete old musicdata
                oldFile.delete();
                //rename template to musicdata
                newFile.renameTo(oldFile);
            } catch(Exception e) {
                logError(e, "SEVERE", "error renaming/deleting DB files");
            }
        }

    }

    private static void createDB(String path) {
        try {
            Connection conn = DriverManager.getConnection(path);

            String sql = """
                CREATE TABLE IF NOT EXISTS musicbrainz (
                song text NOT NULL,
                artist text NOT NULL,
                date text NOT NULL
                );
                """;
            Statement stmt = conn.createStatement();
            stmt.execute(sql);

            sql = """
                CREATE TABLE IF NOT EXISTS beatport (
                song text NOT NULL,
                artist text NOT NULL,
                date text NOT NULL,
                type text NOT NULL
                );
                """;
            stmt = conn.createStatement();
            stmt.execute(sql);

            sql = """
                CREATE TABLE IF NOT EXISTS junodownload (
                song text NOT NULL,
                artist text NOT NULL,
                date text NOT NULL
                );
                """;
            stmt = conn.createStatement();
            stmt.execute(sql);

            sql = """
                CREATE TABLE IF NOT EXISTS youtube (
                song text NOT NULL,
                artist text NOT NULL,
                date text NOT NULL
                );
                """;
            stmt = conn.createStatement();
            stmt.execute(sql);

            sql = """
                CREATE TABLE IF NOT EXISTS artists (
                artistname text PRIMARY KEY,
                urlmusicbrainz text,
                urlbeatport text,
                urljunodownload text,
                urlyoutube text
                );
                """;
            stmt = conn.createStatement();
            stmt.execute(sql);

            sql = """
                CREATE TABLE IF NOT EXISTS combview (
                song text NOT NULL,
                artist text NOT NULL,
                date text NOT NULL
                );
                """;
            stmt = conn.createStatement();
            stmt.execute(sql);

            stmt.close();
            conn.close();
        } catch (SQLException e) {
           logError(e, "SEVERE", "error creating DB file");
        }
    }
    public static Map<String, ArrayList<String>> getDBStructure(String path) {
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
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            logError(e, "SEVERE", "error parsing DB structure");
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
            case ("lastScrape") -> settingsStore.setScrapeDate(config.getString("lastScrape"));
            case ("longTimeout") -> settingsStore.setLongTimeout(config.getBoolean("longTimeout"));
            case ("isoDates") -> settingsStore.setIsoDates(config.getBoolean("isoDates"));
        }
        config = null;
    }
    public static void writeSingleConfig(String name, String value) {
        //save single string option state in HOCON
        Config config = ConfigFactory.parseFile(new File(DBtools.settingsStore.getConfigPath()));
        ConfigValue configValue;
        if (value.equals("true") || value.equals("false"))
            configValue = ConfigValueFactory.fromAnyRef(Boolean.parseBoolean(value));
        else
            configValue = ConfigValueFactory.fromAnyRef(value);
        config = config.withValue(name, configValue);
        ConfigRenderOptions renderOptions = ConfigRenderOptions.defaults().setOriginComments(false).setJson(false).setFormatted(true);
        try (PrintWriter writer = new PrintWriter(new FileWriter(DBtools.settingsStore.getConfigPath()))) {
            writer.write(config.root().render(renderOptions));
        } catch (IOException e) {
            logError(e, "WARNING", "could not save " + name + " in config");
        }
    }

    public static void updateSettings() {
        //create config if it does not exist, change to latest structure and transfer data if a different structure is detected

        // appData/MusicReleaseTracker/MRTsettings.hocon
        String configPath = settingsStore.getConfigPath();
        // appData/MusicReleaseTracker/
        String configFolder = settingsStore.getConfigFolder();
        //a default settings structure for current version
        String templateContent = """
            filters {
                Acoustic=false
                Extended=false
                Instrumental=false
                Remaster=false
                Remix=false
                VIP=false
            }
            theme=Black
            accent=Classic
            lastScrape=-
            longTimeout=false
            isoDates=false
            """;
        //create template file / overwrite templateContent
        File templateFile = new File(configFolder + "/MRTsettingsTemplate.hocon");
        try (PrintWriter writer = new PrintWriter(new FileWriter(templateFile))) {
            writer.write(templateContent);
        } catch (IOException e) {
            logError(e, "SEVERE", "could not overwrite templatecontent");
        }
        //create config file if not exist > write templateContent
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(configFile))) {
                writer.write(templateContent);
            } catch (IOException e) {
                logError(e, "SEVERE", "could not overwrite configfile");
            }
        }
        //comparing structure of existing config file and template
        Config config = ConfigFactory.parseFile(new File(configPath));
        Config templateConfig = ConfigFactory.parseFile(new File(configFolder + "/MRTsettingsTemplate.hocon"));

        ArrayList<String> configStructure = extractStructure(config);
        ArrayList<String> templateStructure = extractStructure(templateConfig);

        //if the settings and template options differ
        if (!templateStructure.containsAll(configStructure) || !configStructure.containsAll(templateStructure)) {
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
                logError(e, "SEVERE", "error while saving MRTsettingsTemplate.hocon");
            }
            //overwrite MRTsettings with MRTsettingsTemplate
            try {
                Files.copy(templateFile.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logError(e, "SEVERE", "error while replacing MRTsettings with MRTsettingsTemplate");
            }
            //default templateContent again
            try (PrintWriter writer = new PrintWriter(new FileWriter(templateFile))) {
                writer.write(templateContent);
            } catch (IOException e) {
                logError(e, "WARNING", "error defaulting MRTsettingsTemplate.hocon");
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

   public static void resetSettings() {
       //default the settings
       File configFile = new File(settingsStore.getConfigPath());
       configFile.delete();
       updateSettings();
   }
   public static void resetDB() {
       //default the musicdata
       File musicdata = new File(settingsStore.getDBpath().substring(12));
       musicdata.delete();
       createDB(settingsStore.getDBpath());
   }
}



