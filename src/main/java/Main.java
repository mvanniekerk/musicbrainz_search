import Aggregation.DataStore.RecordingAggregator;
import Aggregation.DataStore.WorkAggregator;
import Scoring.Scoring;
import Search.SearchController;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws Exception {
        elasticAlive();

        if (args.length != 1) {
            System.out.println(HELP_TEXT);
            return;
        }
        String arg = args[0];

        switch (arg) {
            case "work-aggregate":
                try {
                    WorkAggregator.aggregate();
                } catch (SQLException e) {
                    System.out.println(SQL_ERROR_TEXT);
                    e.printStackTrace();
                }
                return;
            case "recording-aggregate":
                try {
                    RecordingAggregator.aggregate();
                } catch (SQLException e) {
                    System.out.println(SQL_ERROR_TEXT);
                    e.printStackTrace();
                }
                return;
            case "optimize":
                Scoring.run();
                return;
            case "webserver":
                SearchController.run();
                return;
            default:
                System.out.println(HELP_TEXT);
        }
    }

    private static void elasticAlive() throws InterruptedException, IOException {
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://127.0.0.1:9200/musicbrainz/_settings")
                .get()
                .build();

        for (int tries = 0; tries < 5; tries++) {
            try {
                httpClient.newCall(request).execute();
                System.out.println("Elastic Database online, continuing");
                return;
            } catch (IOException c) {
                System.out.println("Elastic Database is not online, retrying in 5 seconds...");
                Thread.sleep(5000);
            }
        }
    }

    private static final String HELP_TEXT =
            "Usage: musicbrainz_search [work-aggregate | recording-aggregate | optimize | webserver] \n" +
                    "   -h      show this screen";

    private static final String SQL_ERROR_TEXT =
            "Cannot connect to the database";
}
