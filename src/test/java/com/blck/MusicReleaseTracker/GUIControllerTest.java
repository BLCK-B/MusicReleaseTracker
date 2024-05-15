package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Core.SourcesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import org.junit.jupiter.api.*;
import org.springframework.test.context.event.annotation.BeforeTestClass;

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

public class GUIControllerTest {

    private String DBpath;
    private ValueStore store;
    private DBtools DB;
    private GUIController guiController;
    
    @BeforeEach
    void setUp() {
        DBpath = "jdbc:sqlite:" + Paths.get("src", "test", "testresources", "testdb.db");
        store = new ValueStore();
        store.setDBpath(DBpath);
        DB = new DBtools(store, null, null);
        guiController = new GUIController(store, null, null, null, DB, null);
        guiController.setTestData("Joe", SourcesEnum.beatport);
    }

    @AfterEach
    void cleanUp() {

    }

}