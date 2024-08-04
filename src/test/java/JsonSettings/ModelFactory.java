package JsonSettings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

public class ModelFactory {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ObjectNode getModelV1() {
        var model = objectMapper.createObjectNode();
        for (HelperModelV1 setting : HelperModelV1.values())
            model.put(setting.name(), setting.getValue().toString());
        return model;
    }

    public static ObjectNode getModelV2() {
        var model = objectMapper.createObjectNode();
        for (HelperModelV2 setting : HelperModelV2.values())
            model.put(setting.name(), setting.getValue().toString());
        return model;
    }

    private enum HelperModelV1 {
        theme("black"),
        autoTheme(false);

        private final Object value;
        HelperModelV1(Object value) {
            this.value = value;
        }
        public Object getValue() {
            return value;
        }
    }

    private enum HelperModelV2 {
        theme("black"),
        autoTheme(false),
        V2exclusive(true);

        private final Object value;
        HelperModelV2(Object value) {
            this.value = value;
        }
        public Object getValue() {
            return value;
        }
    }
}