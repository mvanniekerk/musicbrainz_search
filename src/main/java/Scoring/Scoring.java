package Scoring;

import Database.ElasticConnection;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jsonSerializer.JacksonSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.io.InputStream;

public class Scoring {
    @SuppressWarnings("nullness")
    @Getter
    TestCase[] testCases;

    final int numResults = 50;
    final double total = (double) numResults;

    Scoring() {}

    void loadTestCases(String filename) throws IOException {
        InputStream file = this.getClass().getResourceAsStream(filename);
        if (file == null) throw new IOException("file does not exist");

        testCases = new ObjectMapper().readValue(file, TestCase[].class);
    }

    double calculateScore(TestCase testCase) {
        String resultString =
                ElasticConnection.getInstance().search(testCase.query, "", "", 0, numResults);

        JsonNode result = JacksonSerializer.getInstance().readTree(resultString);
        JsonNode resultList = result.get("hits").get("hits");

        int i = 0;
        for (JsonNode work : resultList) {
            if (work.get("_id").asText().equals(testCase.expected)) break;
            i++;
        }

        return (total - i) / total;
    }

    double calculateScore() {
        double i = 0;
        double sum = 0;
        for (TestCase testCase : testCases) {
            sum += calculateScore(testCase);
            i++;
        }
        return sum / i;
    }

    @ToString
    @AllArgsConstructor
    static class TestCase {
        @Getter
        private final String query;
        @Getter
        private final String expected;
    }

    public static void main(String[] args) throws IOException {
        Scoring scoring = new Scoring();
        scoring.loadTestCases("/testCases.json");
        System.out.println(scoring.calculateScore());
        ElasticConnection.getInstance().close();
    }
}
