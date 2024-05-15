package com.blck.MusicReleaseTracker;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import org.springframework.test.context.event.annotation.BeforeTestClass;

import java.nio.file.Paths;

public class DBToolsTest {

    private String testDBpath;
    private ValueStore store;
    private DBqueries dBqueries;

    @BeforeTestClass
    public void setUp() {
        HelperDB.redoTestDB();
        testDBpath = "jdbc:sqlite:" + Paths.get("src", "test", "testresources", "testdb.db");
        store = new ValueStore();
        store.setDBpath(testDBpath);
        dBqueries = new DBqueries(store, null, null);
    }



}
