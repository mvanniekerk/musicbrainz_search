package Scoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.InputStream;

@AllArgsConstructor
public class FileLoader implements Loader {
    String filename;

    public TestCase[] loadTestCases() throws IOException {
        InputStream file = this.getClass().getResourceAsStream(filename);
        if (file == null) throw new IOException("file does not exist");

        return new ObjectMapper().readValue(file, TestCase[].class);
    }
}
