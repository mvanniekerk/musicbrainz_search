package Aggregation.Store;

import Aggregation.dataType.MBWork;

import java.sql.SQLException;

public interface SearchResult {
    void add(MBWork work, ResultType resultType);
    void store() throws SQLException;
}
