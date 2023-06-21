package com.blck.MusicReleaseTracker;
import javafx.beans.property.SimpleStringProperty;

/*      MusicReleaseTrcker
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

public class TableModel {
    private final SimpleStringProperty column1;
    private final SimpleStringProperty column2;

    public TableModel(String column1, String column2) {
        this.column1 = new SimpleStringProperty(column1);
        this.column2 = new SimpleStringProperty(column2);
    }

    public String getColumn1() {
        return column1.get();
    }
    public String getColumn2() {
        return column2.get();
    }

}