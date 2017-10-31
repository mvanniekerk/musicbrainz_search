package jsonSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.checkerframework.checker.nullness.qual.Nullable;

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
}
