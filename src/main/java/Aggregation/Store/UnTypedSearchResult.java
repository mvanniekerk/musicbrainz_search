package Aggregation.Store;

import Aggregation.dataType.MBWork;

import java.util.HashMap;
import java.util.Map;

public class UnTypedSearchResult implements SearchResult {
    private final Map<String, MBWork> searchResults = new HashMap<>();

    public void add(String gid, ResultType type) {
        assert !searchResults.containsKey(gid);
        MBWork work = new MBWork(gid);
        searchResults.put(gid, work);
    }

    @Override
    public void add(MBWork work, ResultType resultType) {

    }

    @Override
    public void store() {
        throw new RuntimeException("This method should not be called");
    }
}
