package jsonSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.InputStream;

public class JacksonSerializer implements JsonSerializer {

    @Nullable
    private static JacksonSerializer jsonSerializer;

    private ObjectMapper objectMapper;

    private JacksonSerializer() {
        objectMapper = new ObjectMapper();

        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    public static JacksonSerializer getInstance() {
        if (jsonSerializer == null) {
            jsonSerializer = new JacksonSerializer();
        }
        return jsonSerializer;
    }

    public byte[] writeAsBytes(Object value) {
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String writeAsString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonNode readTree(String jsonString) {
        try {
            return objectMapper.readTree(jsonString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T readValue(JsonNode node, Class<T> o) {
        try {
            return objectMapper.treeToValue(node, o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
