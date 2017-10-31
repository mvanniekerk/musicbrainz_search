package jsonSerializer;

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

        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
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
