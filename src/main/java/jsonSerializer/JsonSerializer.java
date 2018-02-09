package jsonSerializer;

import com.fasterxml.jackson.databind.JsonNode;

public interface JsonSerializer {
    byte[] writeAsBytes(Object value);
    String writeAsString(Object value);
    JsonNode readTree(String jsonValue);
}
