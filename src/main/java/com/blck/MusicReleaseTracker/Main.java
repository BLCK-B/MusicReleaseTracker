package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Simple.ErrorLogging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

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

/** entry point class with startup logic */
@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    private final ConfigTools config;
    private final ErrorLogging log;
    private final DBtools DB;

    @Autowired
    public Main(ConfigTools configTools, ErrorLogging errorLogging, DBtools dBtools) {
        this.config = configTools;
        this.log = errorLogging;
        this.DB = dBtools;
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
            try {
                DB.path();
            } catch (Exception e) {
                throw new RuntimeException("error in DB path method", e);
            }
            try {
                DB.createTables();
            } catch (Exception e) {
                log.error(e, ErrorLogging.Severity.SEVERE, "error in DBtools createTables method");
            }
            try {
                config.updateSettings();
            } catch (Exception e) {
                log.error(e, ErrorLogging.Severity.WARNING, "error handling config file");
            }
            // open port in web browser
            try {
                String os = System.getProperty("os.name").toLowerCase();

                String[] cmd = null;
                if (os.contains("win"))
                    cmd = new String[]{"cmd.exe", "/c", "start", "http://localhost:8080"};
                else if (os.contains("nix") || os.contains("nux"))
                    cmd = new String[]{"xdg-open", "http://localhost:8080"};
                else if (os.contains("mac"))
                    cmd = new String[]{"open", "http://localhost:8080"};

                Runtime.getRuntime().exec(cmd);
            } catch (Exception e) {
                log.error(e, ErrorLogging.Severity.WARNING, "could not open port in browser");
            }
        }
    }

}