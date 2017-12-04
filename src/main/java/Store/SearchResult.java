package Store;

import dataType.Work;

import java.sql.SQLException;

public interface SearchResult {
    void add(String gid, ResultType type);
    void add(Work work, ResultType resultType);
    void store() throws SQLException;
    int getResultSize();
}
