package Aggregation.DataStore;

import Database.ElasticConnection;
import com.fasterxml.jackson.databind.JsonNode;
import jsonSerializer.JacksonSerializer;
import okhttp3.*;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Scanner;

public class WorkAggregator extends Aggregator {
    public WorkAggregator(int stepSize, int start) {
        super(stepSize, start);
    }

    void resetWorks() throws IOException, InterruptedException {
        ElasticConnection conn = ElasticConnection.getInstance();
        try {
            conn.deleteIndex();
        } catch (Exception e) {
            System.out.println("Elastic database is not yet online, retrying in 10 seconds.");
            Thread.sleep(10_000);
            resetWorks();
        }
    }

    private String getSettings() throws IOException {
        InputStream file = WorkAggregator.class.getResourceAsStream("/elasticSettings.json");
        if (file == null) throw new IOException("file does not exist");
        return new Scanner(file, "UTF-8")
                .useDelimiter("\\A")
                .next();
    }

    private RequestBody createRequestBody() throws IOException {
        String settings = getSettings();
        MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        return RequestBody.create(JSON, settings);
    }

    void storeSettings() throws IOException {
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://127.0.0.1:9200/musicbrainz/")
                .addHeader("Content-Type", "application/json")
                .put(createRequestBody())
                .build();

        Response response = httpClient.newCall(request).execute();
        ResponseBody rb = response.body();
        if (rb == null) {
            throw new IOException("rb is null");
        }
        String responseString = rb.string();
        JsonNode node = JacksonSerializer.getInstance().readTree(responseString).get("acknowledged");
        if (node == null || !node.booleanValue()) {
            throw new IOException("Scoring change was not accepted, error msg: " + responseString);
        }
    }

    @Override
    void aggregate(int from, int to) throws SQLException {
        WorkStore works = new WorkStore(from, to);
        works.aggregateFromDB();
        works.aggregateParts();
        works.elasticStore();
    }

    public static void aggregate() throws Exception {
        WorkAggregator aggregator = new WorkAggregator(5000, 0);
        System.out.println("Removing elasticsearch index");
        aggregator.resetWorks();
        System.out.println("Creating elasticsearch index and storing settings");
        aggregator.storeSettings();
        aggregator.aggregateAll();
        ElasticConnection.getInstance().close();
    }
}
