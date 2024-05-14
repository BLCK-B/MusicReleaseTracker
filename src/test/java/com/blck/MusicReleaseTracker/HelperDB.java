package com.blck.MusicReleaseTracker;

import java.io.File;
import java.nio.file.Paths;

public class HelperDB {

    private static DBtools dBtools;

    public static void redoTestDB() {
        File testDB = new File(DBpath());
        if (testDB.exists())
            testDB.delete();
        dBtools.createDBandSourceTables(DBpath());
    }

    public static void redoTestData() {

    }

    private static String DBpath() {
        return "jdbc:sqlite:" + Paths.get("src", "test", "testresources", "testdb.db");
    }

}
