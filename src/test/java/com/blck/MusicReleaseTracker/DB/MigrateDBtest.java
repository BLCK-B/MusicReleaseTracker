/*
 *         MusicReleaseTracker
 *         Copyright (C) 2023 - 2024 BLCK
 *         This program is free software: you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation, either version 3 of the License, or
 *         (at your option) any later version.
 *         This program is distributed in the hope that it will be useful,
 *         but WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *         GNU General Public License for more details.
 *         You should have received a copy of the GNU General Public License
 *         along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.blck.MusicReleaseTracker.DB;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class MigrateDBtest {

	@Mock
	ValueStore store;
	@Mock
	ErrorLogging log;
	@InjectMocks
	MigrateDB migrateDB;

	@Test
	void createsDBfile() {
		HelperDB.deleteDB();

		migrateDB.createDBandSourceTables(HelperDB.testDBpath);

		assertTrue(Files.exists(HelperDB.DBfilePath));
	}

	@Test
	void createsTables() {
		migrateDB.createDBandSourceTables(HelperDB.testDBpath);

		var structure = migrateDB.getDBStructure(HelperDB.testDBpath);

		assertAll(
			() -> assertTrue(structure.containsKey("artists")),
			() -> assertTrue(structure.containsKey("combview")),
			() -> assertTrue(structure.containsKey("beatport"))
		);
	}

	@Test
	void structureContainsColumns() {
		migrateDB.createDBandSourceTables(HelperDB.testDBpath);

		var structure = migrateDB.getDBStructure(HelperDB.testDBpath);

		assertFalse(structure.get("artists").isEmpty());
	}

	@Test
	void testStuff() {
//		HelperDB.deleteLegacyFiles();
//		HelperDB.createDBv1(HelperDB.legacyTestDBpath);
//		HelperDB.createDBv1(HelperDB.testTemplateDBpath);

		migrateDB.copyArtistsData(HelperDB.legacyTestDBpath, HelperDB.testTemplateDBpath);
	}

}
