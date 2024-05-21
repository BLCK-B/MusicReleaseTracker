package com.blck.MusicReleaseTracker;
import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.SourcesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.DB.DBqueriesClass;
import com.blck.MusicReleaseTracker.DB.ManageMigrateDB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.event.annotation.BeforeTestClass;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DBqueriesTest {

    final String testDBpath = "jdbc:sqlite:" + Paths.get("src", "test", "testresources", "testdb.db");

    @Mock
    ValueStore store;
    @Mock
    ManageMigrateDB manageMigrateDB;
    @Mock
    ErrorLogging log;
    @Mock
    ConfigTools config;
    @InjectMocks
    DBqueriesClass dBqueriesClass;

    @BeforeTestClass
    void setUpDB() {
        HelperDB.DBpath = testDBpath;
        HelperDB.redoTestDB();
    }

    @BeforeEach
    void setUp() {
        HelperDB.DBpath = testDBpath;
        HelperDB.redoTestData();
        when(store.getDBpath()).thenReturn(testDBpath);
        dBqueriesClass = new DBqueriesClass(store, log, config, manageMigrateDB);
    }

    @Test
    @Disabled
    void getNewArtistSourceID() {
        assertEquals("IDBP", dBqueriesClass.getArtistSourceID("artist1", SourcesEnum.beatport));

        dBqueriesClass.updateArtistSourceID("artist1", SourcesEnum.beatport, "newID");

        assertEquals("newID", dBqueriesClass.getArtistSourceID("artist1", SourcesEnum.beatport));
    }


}
