package Search;

import dataType.Work;

public class UnTypedSearchResult implements SearchResult {
    @Override
    public void add(String gid, ResultType type) {

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
