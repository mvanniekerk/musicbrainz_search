package Aggregation.Store;

import Aggregation.dataType.Work;

import java.util.HashMap;
import java.util.Map;

public class UnTypedSearchResult implements SearchResult {
    private final Map<String, Work> searchResults = new HashMap<>();

    @Override
    public void add(String gid, ResultType type) {
        assert !searchResults.containsKey(gid);
        Work work = new Work(gid);
        searchResults.put(gid, work);
    }

    @Override
    public void add(Work work, ResultType resultType) {

    }

    @Override
    public void store() {
        throw new RuntimeException("This method should not be called");
    }

    @Override
    public int getResultSize() {
        return 0;
    }
}
