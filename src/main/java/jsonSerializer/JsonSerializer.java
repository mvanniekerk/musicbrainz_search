package jsonSerializer;

public interface JsonSerializer {
    byte[] writeAsBytes(Object value);
    String writeAsString(Object value);
}
