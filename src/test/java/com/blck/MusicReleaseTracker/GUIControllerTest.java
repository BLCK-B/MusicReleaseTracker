package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Core.ValueStore;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.File;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GUIControllerTest {

    private final ValueStore store = new ValueStore();
    private final DBtools DB = new DBtools(store, null);
    private final String testDBpath;
    private final GUIController testedClass;

    public GUIControllerTest() {
        // data setup
        String DBpath = null;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) { // Windows
            String appDataPath = System.getenv("APPDATA");
            DBpath = "jdbc:sqlite:" + appDataPath + File.separator + "MusicReleaseTracker" + File.separator + "testingdata.db";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {  // Linux
            String userHome = System.getProperty("user.home");
            DBpath = "jdbc:sqlite:" + userHome + File.separator + ".MusicReleaseTracker" + File.separator + "testingdata.db";
        }
        else
            throw new UnsupportedOperationException("unsupported OS");

        testDBpath = DBpath;
        store.setDBpath(DBpath);
        testedClass = new GUIController(store, null, null, null, DB);
    }

    @Test
    @Order(1)
    void AddLoadArtist() {
        // artistAddConfirm
        testedClass.artistAddConfirm("Joe");
        // verify with loadList
        String oneArtist = testedClass.loadList().get(0);
        assertEquals(oneArtist, "Joe");
    }

    @Test
    @Order(2)
    void AddVerifyUrl() {
        // saveUrl
        testedClass.saveUrl();
        // verify with checkExistUrl
        try {
            boolean exists = testedClass.checkExistURL();
            assertTrue(exists);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(3)
    void DeleteVerifyUrl() {
        // deleteUrl
        testedClass.deleteUrl();
        // verify with checkExistUrl
        try {
            boolean exists = testedClass.checkExistURL();
            assertFalse(exists);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(4)
    void DeleteArtist() {
        // artistClickDelete
        testedClass.artistClickDelete();
        // verify with loadList
        assertTrue(testedClass.loadList().isEmpty());
    }
}
