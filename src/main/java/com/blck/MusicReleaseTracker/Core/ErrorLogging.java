package com.blck.MusicReleaseTracker.Core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

@Component
public class ErrorLogging {

    public enum Severity {
        SEVERE, WARNING, INFO
    }
    private final ValueStore store;
    private final Logger logger = Logger.getLogger("errorLogger");
    @Autowired
    public ErrorLogging(ValueStore valueStore) {
        this.store = valueStore;
    }

    public void error(Exception e, Severity level, String message) {
        FileHandler fileHandler = null;
        try {
            final Path errorLogsPath = store.getErrorLogsPath();
            fileHandler = new FileHandler(errorLogsPath.toString(), true);
            fileHandler.setFormatter(new SimpleFormatter());
            // clear log when it reaches approx 0.1 MB
            final long logFileSize = Files.size(errorLogsPath);
            if (logFileSize > 100000)
                Files.write(errorLogsPath, new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
            logger.addHandler(fileHandler);
            switch (level) {
                case SEVERE -> logger.log(Level.SEVERE, message, e);
                case WARNING -> logger.log(Level.WARNING, message, e);
                case INFO -> logger.log(Level.INFO, message);
            }
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        } finally {
            fileHandler.close();
        }
        if (level == Severity.SEVERE) {
            System.exit(1);
        }
    }
}
