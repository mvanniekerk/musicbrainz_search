package Search;

import DataStore.WorkStore;
import Database.MusicBrainzDB;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Aggregator {
    private final int stepSize;
    private final SearchMap searchMap;

    public Aggregator(int stepSize) {
        this.stepSize = stepSize;
        searchMap = new SearchMap();
    }

    Connection getConnection() {
        return MusicBrainzDB.getConnection();
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

        for (int i = 0; i < totalRows; i += stepSize) {
            aggregateWithTime(i, i + stepSize);
        }
    }

    void aggregate(int from, int to) throws SQLException {
        WorkStore works = new WorkStore(from, to);
        works.aggregateFromDB();

        works.populateSearchMap(searchMap);

        searchMap.store();
        searchMap.empty();
    }

    void aggregateWithTime(int from, int to) throws SQLException {
        System.out.println("indexing from " + from + " to " + to);
        long startTime = System.currentTimeMillis();

        aggregate(from, to);

        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;

        System.out.println("done. took " + duration + " ms");
    }

    public static void main(String[] args) throws SQLException {
        Aggregator aggregator = new Aggregator(50000);
        aggregator.aggregateAll();
    }
}
