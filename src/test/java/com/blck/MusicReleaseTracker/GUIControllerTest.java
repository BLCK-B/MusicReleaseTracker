package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Core.ValueStore;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

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

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GUIControllerTest {

    private final ValueStore store = new ValueStore();
    private final DBtools DB = new DBtools(store, null);
    private final GUIController testedClass;

    public GUIControllerTest() {
        // data setup
        String DBpath = "jdbc:sqlite:" + Paths.get("src", "test", "testresources", "testdb.db");
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

        testedClass.vacuum();
    }
}