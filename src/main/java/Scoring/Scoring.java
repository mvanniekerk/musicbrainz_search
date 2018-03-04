package Scoring;

import Database.ElasticConnection;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jsonSerializer.JacksonSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.close.CloseIndexResponse;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexResponse;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.io.InputStream;

public class Scoring {
    @SuppressWarnings("nullness")
    @Getter
    TestCase[] testCases;

    private final int numResults = 50;


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

        int i = 1;
        for (JsonNode work : resultList) {
            if (work.get("_id").asText().equals(testCase.expected)) {
                return ((double) numResults / i) / numResults;
            }
            i++;
        }

        return 0;
    }

    double calculateScore() {
        double i = 0, sum = 0;
        for (TestCase testCase : testCases) {
            double score = calculateScore(testCase);
            System.out.println("Score: " + score + " , query: " + testCase.query);
            sum += score;
            i++;
        }
        return sum / i;
    }

    private RequestBody createRequestBody(double k1, double b) {
        MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String jsonString =
            "{\n" +
            "    \"settings\" : {\n" +
            "        \"index\" : {\n" +
            "            \"similarity\" : {\n" +
            "              \"my_similarity\" : {\n" +
            "                \"type\" : \"BM25\",\n" +
            "                \"k1\" : \"" + k1 + "\",\n" +
            "                \"b\" : \"" + b + "\",\n" +
            "                \"discount_overlaps\" : \"true\"\n" +
            "                \n" +
            "              }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}";

        return RequestBody.create(JSON, jsonString);
    }

    void updateParameters(double k1, double b) throws IOException {
        RestHighLevelClient client = ElasticConnection.getInstance().getClient();

        CloseIndexRequest closeIndexRequest = new CloseIndexRequest("musicbrainz");
        CloseIndexResponse closeIndexResponse = client.indices().close(closeIndexRequest);
        if (!closeIndexResponse.isAcknowledged()) throw new RuntimeException("Index still open");


        // Sadly, elasticsearch does not seem to be completely up to date with the high level API
        // Updating settings is not yet possible, so for this one request, I needed to pull in
        // a new dependency: OkHttpClient
        // The code is rather ugly, but I see it as a temporary workaround.

        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://192.168.99.100:9200/musicbrainz/_settings")
                .addHeader("Content-Type", "application/json")
                .put(createRequestBody(k1, b))
                .build();

        Response response = httpClient.newCall(request).execute();
        ResponseBody rb = response.body();
        if (rb != null && !rb.string().equals("{\"acknowledged\":true}"))
            throw new RuntimeException("Scoring change was not accepted, error msg: " + rb.string());

        OpenIndexRequest openIndexRequest = new OpenIndexRequest("musicbrainz");
        OpenIndexResponse openIndexResponse = client.indices().open(openIndexRequest);
        if (!openIndexResponse.isAcknowledged()) throw new RuntimeException("Index still closed");
    }

    @ToString
    @AllArgsConstructor
    static class TestCase {
        @Getter private final String query;
        @Getter private final String expected;
    }

    public static void main(String[] args) throws IOException {
        Scoring scoring = new Scoring();

        scoring.updateParameters(1.4, 0.3);

        scoring.loadTestCases("/testCases.json");
        System.out.println("\nFinal score: " + scoring.calculateScore());
        ElasticConnection.getInstance().close();
    }
}
