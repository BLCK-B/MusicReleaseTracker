package com.blck.MusicReleaseTracker;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import com.blck.MusicReleaseTracker.Core.SourcesEnum;
import com.blck.MusicReleaseTracker.Core.ValueStore;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.*;

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

public class DBtools {

    private final ValueStore store;
    private final ErrorLogging log;

    @Autowired
    public DBtools(ValueStore valueStore, ErrorLogging errorLogging) {
        this.store = valueStore;
        this.log = errorLogging;
    }

    public void truncateDB() {
        try (Connection conn = DriverManager.getConnection(store.getDBpath())) {
            for (SourcesEnum sourceTable : SourcesEnum.values()) {
                String sql = "DELETE FROM " + sourceTable;
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(sql);
            }
            String sql = "DELETE FROM combview";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            log.error(e, ErrorLogging.Severity.WARNING, "error clearing DB");
        }
    }
}



