package Aggregation.DataStore;

import Database.ElasticConnection;

import java.sql.SQLException;

public class WorkAggregator extends Aggregator {
    public WorkAggregator(int stepSize, int start) {
        super(stepSize, start);
    }

    @Override
    void aggregate(int from, int to) throws SQLException {
        WorkStore works = new WorkStore(from, to);
        works.aggregateFromDB();
        works.aggregateParts();
        works.elasticStore();
    }

    public static void main(String[] args) throws SQLException {
        Aggregator aggregator = new WorkAggregator(10000, 12430000);
        aggregator.aggregateAll();
        ElasticConnection.getInstance().close();
    }
}
