package com.blck.MusicReleaseTracker;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GUIControllerTest {

    private final GUIController GUI;

    @Autowired
    public GUIControllerTest(GUIController guiController) {
        this.GUI = guiController;
    }

    private static String DBpath;

    String getDBpath() {
        String os = System.getProperty("os.name").toLowerCase();
        String DBpath = null;
        String appDataPath = null;
        if (os.contains("win")) // Windows
            appDataPath = System.getenv("APPDATA");
        else if (os.contains("nix") || os.contains("nux") || os.contains("mac"))  // Linux
            appDataPath = System.getProperty("user.home");
        else
            throw new UnsupportedOperationException("unsupported OS");

        DBpath = "jdbc:sqlite:" + appDataPath + File.separator + "MusicReleaseTracker" + File.separator + "testingdata.db";
        return DBpath;
    }

    @Test
    @Order(1)
    void AddLoadArtist() {
        String DBpath = getDBpath();
        // artistAddConfirm
        GUI.artistAddConfirm("Joe", DBpath);
        // verify with loadList
        try {
            String oneArtist = GUI.loadList(DBpath).get(0);
            assertEquals(oneArtist, "Joe");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(2)
    void AddVerifyUrl() {
        String DBpath = getDBpath();
        // saveUrl
        GUI.saveUrl(DBpath);
        // verify with checkExistUrl
        try {
            boolean exists = GUI.checkExistURL(DBpath);
            assertTrue(exists);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(3)
    void DeleteVerifyUrl() {
        String DBpath = getDBpath();
        // deleteUrl
        GUI.deleteUrl(DBpath);
        // verify with checkExistUrl
        try {
            boolean exists = GUI.checkExistURL(DBpath);
            assertFalse(exists);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(4)
    void DeleteArtist() {
        String DBpath = getDBpath();
        // artistClickDelete
        GUI.artistClickDelete(DBpath);
        // verify with loadList
        try {
            assertTrue(GUI.loadList(DBpath).isEmpty());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
