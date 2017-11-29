package Search;

import dataType.Work;

public interface SearchResult {
    void add(String gid, ResultType type);
    void add(Work work, ResultType resultType);
}
