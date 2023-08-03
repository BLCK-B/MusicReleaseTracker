package com.blck.MusicReleaseTracker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.typesafe.config.*;

/*      MusicReleaseTrcker
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
    public static String DBpath;
    public static String ConfigPath;
    public static String ConfigFolder;

    public static void path() {
        String os = System.getProperty("os.name").toLowerCase();
        String ide = System.getProperty("ide.environment");

        if (ide != null && ide.equals("IDE")) { //IDE
            String appDataPath = System.getenv("APPDATA");
            DBpath = "jdbc:sqlite:musicdata.db";
            ConfigPath = appDataPath + File.separator + "MusicReleaseTracker" + File.separator + "MRTsettings.hocon";
            ConfigFolder = appDataPath + File.separator + "MusicReleaseTracker" + File.separator;
        } else if (os.contains("win")) { //Windows
            String appDataPath = System.getenv("APPDATA");
            DBpath = "jdbc:sqlite:" + appDataPath + File.separator + "MusicReleaseTracker" + File.separator + "musicdata.db";
            ConfigPath = appDataPath + File.separator + "MusicReleaseTracker" + File.separator + "MRTsettings.hocon";
            ConfigFolder = appDataPath + File.separator + "MusicReleaseTracker" + File.separator;
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {  //Linux
            String userHome = System.getProperty("user.home");
            File folder = new File(userHome + File.separator + ".MusicReleaseTracker");
            if (!folder.exists())
                folder.mkdirs();
            DBpath = "jdbc:sqlite:" + userHome + File.separator + ".MusicReleaseTracker" + File.separator + "musicdata.db";
            ConfigPath = userHome + File.separator + ".MusicReleaseTracker" + File.separator + "MRTsettings.hocon";
            ConfigFolder = userHome + File.separator + ".MusicReleaseTracker" + File.separator;
        }
        else
            throw new UnsupportedOperationException("unsupported OS");
    }

    public static void createTables() throws SQLException {
        Connection conn = DriverManager.getConnection(DBpath);

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

    public static List<String> filterWords = new ArrayList<>();
    public static int entriesLimit = 0;

    public static void readCombviewConfig() {
        //read filters
        filterWords.clear();
        Config config = ConfigFactory.parseFile(new File(ConfigPath));
        Config filtersConfig = config.getConfig("filters");
        for (Map.Entry<String, ConfigValue> entry : filtersConfig.entrySet()) {
            String filter = entry.getKey();
            boolean enabled = entry.getValue().unwrapped().equals(true);
            if (enabled)
                filterWords.add(filter);
        }
        //read combviewlength
        switch (config.getString("combviewlength")) {
            case "short" -> entriesLimit = 7;
            case "medium" -> entriesLimit = 14;
            case "long" -> entriesLimit = 40;
        }
    }

    public static void updateSettingsDB() {
        //create config if it does not exist, transfer data to new structure if update changed it
        //version:settings and DBversion:database should be changed on any respective structure update
        //to reflect DBversion change, version is changed too
        final int DBversion = 1;
        final int version = DBversion + 2;
        String templateContent =
                "version=" + version + "\n" +
                "DBversion=" + DBversion + "\n" +
                        "filters {\n" +
                        "   Acoustic=false\n" +
                        "   Extended=false\n" +
                        "   Instrumental=false\n" +
                        "   Remaster=false\n" +
                        "   Remix=false\n" +
                        "   VIP=false\n" +
                        "}\n" +
                        "combviewlength=short";
        File templateFile = new File(ConfigFolder + "/MRTsettingsTemplate.hocon");
        if (!templateFile.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(templateFile))) {
                writer.write(templateContent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        File configFile = new File(ConfigPath);
        if (!configFile.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(configFile))) {
                writer.write(templateContent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        //comparing versions
        Config config = ConfigFactory.parseFile(new File(ConfigPath));
        int fileVersion = config.getInt("version");
        if (fileVersion != version) {
            //if different version > update template > transfer all possible data to template > replace files
            System.out.println("updating settings structure");
            try (PrintWriter writer = new PrintWriter(new FileWriter(templateFile))) {
                writer.write(templateContent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //transfer the states of options from MRTsettings to MRTsettingsTemplate, update version
            config = ConfigFactory.parseFile(new File(ConfigPath));
            Config templateConfig = ConfigFactory.parseFile(new File(ConfigFolder + "MRTsettingsTemplate.hocon"));
            for (Map.Entry<String, ConfigValue> entry : config.entrySet()) {
                String key = entry.getKey();
                ConfigValue value = entry.getValue();

                if (value.valueType() == ConfigValueType.BOOLEAN && templateConfig.hasPath(key)) {
                    boolean state = config.getBoolean(key);
                    templateConfig = templateConfig.withValue(key, ConfigValueFactory.fromAnyRef(state));
                }
                else if (value.valueType() == ConfigValueType.STRING && templateConfig.hasPath(key)) {
                    String stringValue = config.getString(key);
                    templateConfig = templateConfig.withValue(key, ConfigValueFactory.fromAnyRef(stringValue));
                }
            }
            config = config.withValue("version", ConfigValueFactory.fromAnyRef(version));
            //save the updated template config to MRTsettingsTemplate.hocon
            try (PrintWriter writer = new PrintWriter(new FileWriter(ConfigFolder + "MRTsettingsTemplate.hocon"))) {
                ConfigRenderOptions renderOptions = ConfigRenderOptions.defaults().setOriginComments(false).setJson(false).setFormatted(true);
                String renderedConfig = templateConfig.root().render(renderOptions);
                writer.write(renderedConfig);
            } catch (IOException e) {
                throw new RuntimeException("Error while saving MRTsettingsTemplate.hocon", e);
            }
            //replace contents of MRTsettings (old) with MRTsettingsTemplate (new), default the template again
            try {
                Files.copy(templateFile.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Error while replacing MRTsettings with MRTsettingsTemplate", e);
            }
            try (PrintWriter writer = new PrintWriter(new FileWriter(templateFile))) {
                writer.write(templateContent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            }

        }
}



