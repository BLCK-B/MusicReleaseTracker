package JsonSettings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ModelFactory {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonNode getModelV1() {
        return objectMapper.valueToTree(new HelperModelV1());
    }

    public static JsonNode getModelV2() {
        return objectMapper.valueToTree(new HelperModelV2());
    }

    private static class HelperModelV1 {
        public final String theme = "black";
        public final boolean autoTheme = false;
    }

    private static class HelperModelV2 {
        public final String theme = "black";
        public final boolean autoTheme = false;
        public final boolean V2exclusive = true;
    }
}