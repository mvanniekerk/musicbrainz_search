import Aggregation.DataStore.RecordingAggregator;
import Aggregation.DataStore.WorkAggregator;
import Scoring.Scoring;
import Search.SearchController;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws Exception {
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

    private static final String HELP_TEXT =
            "Usage: musicbrainz_search [work-aggregate | recording-aggregate | optimize | webserver] \n" +
                    "   -h      show this screen";

    private static final String SQL_ERROR_TEXT =
            "Cannot connect to the database";
}
