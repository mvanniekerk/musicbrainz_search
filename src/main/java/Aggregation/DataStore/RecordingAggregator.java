package Aggregation.DataStore;

import Database.ElasticConnection;

import java.sql.SQLException;

public class RecordingAggregator extends Aggregator {
    public RecordingAggregator(int stepSize, int start) {
        super(stepSize, start);
    }

    void aggregate(int from, int to) throws SQLException {
        RecordingStore store = new RecordingStore(from, to);
        store.aggregateFromDB();
        System.out.println("Aggregation took: " + (System.currentTimeMillis() - startTime));
        store.elasticStore();
    }

    public static void aggregate() throws SQLException {
        Aggregator aggregator = new RecordingAggregator(10000, 0);
        aggregator.aggregateAll();
        ElasticConnection.getInstance().close();
    }
}
