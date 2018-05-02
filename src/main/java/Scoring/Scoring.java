package Scoring;

import Database.ElasticConnection;
import Database.MusicBrainzDB;
import Search.Result;
import Search.Work;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Scoring {
    @SuppressWarnings("nullness")
    @Getter
    TestCase[] testCases;

    private final int numResults = 20;


    void loadTestCases(String filename) throws IOException {
        InputStream file = this.getClass().getResourceAsStream(filename);
        if (file == null) throw new IOException("file does not exist");

        testCases = new ObjectMapper().readValue(file, TestCase[].class);
    }

    void loadSQLTestCases(int testSize, double seed) throws SQLException {
        assert seed > 0.0 && seed < 1.0;

        Connection conn = MusicBrainzDB.getInstance();
        PreparedStatement seedStatement = conn.prepareStatement("SELECT setseed(?)");
        seedStatement.setDouble(1, seed);
        seedStatement.executeQuery();

        PreparedStatement ps = conn.prepareStatement(
                 "SELECT lastfm_name, work_gid\n" +
                        "FROM lastfm_works\n" +
                        "WHERE work_gid is not null\n" +
                        "ORDER BY random()\n" +
                        "LIMIT ?;");
        ps.setInt(1, testSize);
        ResultSet rs = ps.executeQuery();

        int index = 0;
        testCases = new TestCase[testSize];
        while (rs.next()) {
            String name = rs.getString("lastfm_name");
            String gid = rs.getString("work_gid");

            testCases[index] = new TestCase(name, gid);
            index++;
        }
        assert index == testSize;
    }

    double calculateScore(TestCase testCase) {
        String resultString =
                ElasticConnection
                        .getInstance()
                        .search(testCase.query, "", "", 0, numResults);

        Result result = Result.fromElastic(resultString);

        List<Work> resultList = result.getLeaves();
//        System.out.println(resultList
//                .stream()
//                .map(a -> a.getNames().get(0))
//                .reduce("", (str, s) -> str + "; " + s)
//        );

        double score = 0;

        int i = 1;
        for (Work work : resultList) {
            if (work.getGid().equals(testCase.expected)) {
                double dcg = 1 / (Math.log(i + 1) / Math.log(2));
                score += dcg;
            }
            i++;
        }

        return score;
    }

    double calculateScore() {
        double i = 0, sum = 0;
        for (TestCase testCase : testCases) {
            double score = calculateScore(testCase);
            System.out.println(score + ", " + testCase.query + ", " + testCase.expected);
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
        if (rb == null) {
            throw new RuntimeException("rb is null");
        }
        String responseString = rb.string();
        if (!responseString.equals("{\"acknowledged\":true}")) {
            throw new RuntimeException("Scoring change was not accepted, error msg: " + responseString);
        }


        OpenIndexRequest openIndexRequest = new OpenIndexRequest("musicbrainz");
        OpenIndexResponse openIndexResponse = client.indices().open(openIndexRequest);
        if (!openIndexResponse.isShardsAcknowledged()) throw new RuntimeException("Index still closed");
    }

    @ToString
    @AllArgsConstructor
    static class TestCase {
        @Getter private final String query;
        @Getter private final String expected;
    }

    double parameterRange(double lower, double higher, double step) {
        double highestScore = 0;
        double bestK1 = lower;
        double bestB = higher;

        final double lowerB = 0;
        final double higherB = 1;
        final double stepB = 0.1;
        try {
            for (double k1 = lower; k1 < higher; k1 += step) {
                for (double b = lowerB; b < higherB; b += stepB) {
                    updateParameters(k1, b);
                    Thread.sleep(1000);
                    double score = calculateScore();
                    if (score > highestScore) {
                        highestScore = score;
                        bestK1 = k1;
                        bestB = b;
                    }
                    System.out.println(k1 + " " + b + " " + score);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(bestK1 + " " + bestB + " " + highestScore);
        return highestScore;
    }

    public static void main(String[] args) throws Exception {
        Scoring scoring = new Scoring();
        Random random = new Random();
        scoring.loadSQLTestCases(100, 0.333);

        // scoring.updateParameters(3.4, 0.03);
        // Thread.sleep(1000);
//
//        scoring.loadTestCases("/testCases.json");
//
        System.out.println("\nFinal score: " + scoring.calculateScore());
//
//        scoring.parameterRange(0.6, 4.0, 0.2);
        ElasticConnection.getInstance().close();
    }
}
