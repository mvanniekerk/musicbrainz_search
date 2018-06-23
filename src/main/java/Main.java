import Aggregation.DataStore.RecordingAggregator;
import Aggregation.DataStore.WorkAggregator;
import Scoring.Scoring;
import Search.SearchController;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || args.length > 1) {
            System.out.println(HELP_TEXT);
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
            case "recording-aggregate":
                try {
                    RecordingAggregator.aggregate();
                } catch (SQLException e) {
                    System.out.println(SQL_ERROR_TEXT);
                    e.printStackTrace();
                }
            case "optimize":
                Scoring.run();
            case "webserver":
                SearchController.run();
            default:
                System.out.println(HELP_TEXT);
        }
    }

    private static final String HELP_TEXT =
            "Usage: musicbrainz_search [work-aggregate | recording-aggregate | optimize | webserver] \n" +
                    "   -h      show this screen\n";

    private static final String SQL_ERROR_TEXT =
            "Cannot connect to the database";
}
