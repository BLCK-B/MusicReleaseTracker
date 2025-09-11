package com.blck.MusicReleaseTracker.JsonSettings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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