package Scoring;


import Database.ElasticConnection;
import lombok.AllArgsConstructor;
import lombok.Setter;
import okhttp3.*;
import org.apache.http.ConnectionClosedException;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.close.CloseIndexResponse;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexResponse;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;

@AllArgsConstructor
public class GridSearchParameterOptimizer extends ParameterOptimizer {
    private double lowerK1;
    private double higherK1;
    private double stepK1;
    private double lowerB;
    private double higherB;
    private double stepB;

    @Override
    void optimize() throws InterruptedException {
        double highestScore = 0;
        double bestK1 = -1;
        double bestB = -1;

        for (double k1 = lowerK1; k1 < higherK1; k1 += stepK1) {
            for (double b = lowerB; b < higherB; b += stepB) {
                try {
                    updateParameters(k1, b);
                } catch (IOException e) {
                    if (e instanceof ConnectionClosedException) {
                        System.out.println("Connection closed. Trying again in 10 seconds");
                        Thread.sleep(10_000);
                        optimize();
                    }
                    throw new RuntimeException(e);
                }

                Thread.sleep(1000);
                double score = scoring.safeCalculateScore();

                if (score > highestScore) {
                    highestScore = score;
                    bestK1 = k1;
                    bestB = b;
                }
                System.out.println("Running tests with parameters: k1=" + k1 + ", b=" + b + ", score=" + score);
            }
        }
        System.out.println("Best search function parameters: k1=" + bestK1 + ", b=" + bestB + ", score=" + highestScore);
        try {
            updateParameters(bestK1, bestB);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static RequestBody createRequestBody(double k1, double b) {
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

    private static void updateParameters(double k1, double b) throws IOException {
        RestHighLevelClient client = ElasticConnection.getInstance().getClient();

        CloseIndexRequest closeIndexRequest = new CloseIndexRequest("musicbrainz");
        CloseIndexResponse closeIndexResponse = client.indices().close(closeIndexRequest);
        if (!closeIndexResponse.isAcknowledged()) throw new IOException("Index still open");


        // Sadly, elasticsearch does not seem to be completely up to date with the high level API
        // Updating settings is not yet possible, so for this one request, I needed to pull in
        // a new dependency: OkHttpClient
        // The code is rather ugly, but I see it as a temporary workaround.

        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://127.0.0.1:9200/musicbrainz/_settings")
                .addHeader("Content-Type", "application/json")
                .put(createRequestBody(k1, b))
                .build();

        Response response = httpClient.newCall(request).execute();
        ResponseBody rb = response.body();
        if (rb == null) {
            throw new IOException("rb is null");
        }
        String responseString = rb.string();
        if (!responseString.equals("{\"acknowledged\":true}")) {
            throw new IOException("Scoring change was not accepted, error msg: " + responseString);
        }


        OpenIndexRequest openIndexRequest = new OpenIndexRequest("musicbrainz");
        OpenIndexResponse openIndexResponse = client.indices().open(openIndexRequest);
        if (!openIndexResponse.isShardsAcknowledged()) throw new IOException("Index still closed");
    }
}
