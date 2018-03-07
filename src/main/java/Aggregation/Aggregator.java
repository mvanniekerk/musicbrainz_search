package Aggregation;

import Aggregation.DataStore.RecordingStore;
import Aggregation.DataStore.WorkStore;
import Database.ElasticConnection;
import Database.MusicBrainzDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Aggregator {
    private final int stepSize;
    private final int start;

    private long startTime = 0;

    public Aggregator(int stepSize, int start) {
        this.stepSize = stepSize;
        this.start = start;
    }

    Connection getConnection() {
        return MusicBrainzDB.getInstance();
    }

    int getTotalRows() throws SQLException {
        Connection conn = getConnection();

        PreparedStatement ps = conn.prepareStatement(
        "SELECT id from work " +
            "ORDER BY id DESC " +
            "LIMIT 1"
        );

        ResultSet rs = ps.executeQuery();

        rs.next();
        return rs.getInt(1);
    }

    public void aggregateAll() throws SQLException {
        int totalRows = getTotalRows();

        for (int i = start; i < totalRows; i += stepSize) {
            aggregateWithTime(i, i + stepSize);
        }
    }

    void aggregate(int from, int to) throws SQLException {
        RecordingStore store = new RecordingStore(from, to);
        store.aggregateFromDB();
        System.out.println("Aggregation took: " + (System.currentTimeMillis() - startTime));
        // works.aggregateParts();
        store.elasticStore();
    }

    void aggregateWithTime(int from, int to) throws SQLException {
        System.out.println("indexing from " + from + " to " + to);
        startTime = System.currentTimeMillis();

        aggregate(from, to);

        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;

        System.out.println("done. took " + duration + " ms");
    }

    public static void main(String[] args) throws SQLException {
        Aggregator aggregator = new Aggregator(10000, 0);
        aggregator.aggregateAll();
        ElasticConnection.getInstance().close();
    }
}
