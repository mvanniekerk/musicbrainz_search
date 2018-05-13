package Scoring;

import Database.ElasticConnection;
import Database.CrossFieldSearcher;
import lombok.Getter;
import lombok.Setter;
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
import java.util.Random;

public class Scoring {
    @SuppressWarnings("nullness")
    @Getter
    private TestCase[] testCases;
    @Setter
    private Scorer scorer;
    @Setter
    private Loader loader;

    private ParameterOptimizer optimizer;

    Scoring(Scorer scorer, Loader loader) {
        this.scorer = scorer;
        this.loader = loader;
    }

    void setParameterOptimizer(ParameterOptimizer optimizer) {
        this.optimizer = optimizer;
        optimizer.setScoring(this);
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
        if (!closeIndexResponse.isAcknowledged()) throw new IOException("Index still open");


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

    double safeCalculateScore() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            try {
                return calculateScore();
            } catch (Exception e) {
                if (i == 4) {
                    System.out.println("Tried 5 times...");
                    throw new RuntimeException(e);
                } else {
                    Thread.sleep(1000);
                }
            }
        }
        throw new RuntimeException("Unreachable");
    }

    double calculateScore() throws IOException {
        return scorer.calculateScore(testCases);
    }

    void loadTestCases() throws Exception {
        testCases = loader.loadTestCases();
    }

    void optimize() throws InterruptedException {
        optimizer.optimize();
    }



    public static void main(String[] args) throws Exception {
        double seed = new Random().nextDouble();
        seed = 0.7874337238944252;
        Scorer scorer = new PrecisionScore(new CrossFieldSearcher(), false, 20);

        Loader loader = new SqlLoader(200, seed);
        Scoring scoring = new Scoring(scorer, loader);
//        scoring.loadTestCases();
//        double score = scoring.calculateScore();

        scoring.setLoader(new SqlArtistLoader(200, seed));
        scoring.loadTestCases();
//        double artistScore = scoring.calculateScore();

        ParameterOptimizer optimizer = new GridSearchParameterOptimizer(0.6, 3, 0.4, 0, 1, 0.2);
        scoring.setParameterOptimizer(optimizer);
        scoring.optimize();

//        System.out.println("\nFinal score: " + score);
//        System.out.println("With artists: " + artistScore);
//        System.out.println("Difference: " + (artistScore - score));
        System.out.println("Seed: " + seed);
        ElasticConnection.getInstance().close();
    }
}
