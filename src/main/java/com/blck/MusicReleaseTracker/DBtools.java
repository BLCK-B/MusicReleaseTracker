package com.blck.MusicReleaseTracker;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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

    public static void path() {
        String os = System.getProperty("os.name").toLowerCase();
        String ide = System.getProperty("ide.environment");

        if (ide != null && ide.equals("IDE")) { //IDE
            String appDataPath = System.getenv("APPDATA");
            DBpath = "jdbc:sqlite:musicdata.db";
        } else if (os.contains("win")) { //Windows
            String appDataPath = System.getenv("APPDATA");
            DBpath = "jdbc:sqlite:" + appDataPath + File.separator + "MusicReleaseTracker" + File.separator + "musicdata.db";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {  //Linux
            String userHome = System.getProperty("user.home");
            DBpath = "jdbc:sqlite:" + userHome + File.separator + ".MusicReleaseTracker" + File.separator + "musicdata.db";
        }
        else
            throw new UnsupportedOperationException("Unsupported operating system.");
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

    public static void readFilters() {

    }

    public static void UpdateSettingsDB() {

    }



}
