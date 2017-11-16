package Search;

import lombok.NoArgsConstructor;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public class SearchMap {
    private Map<String, SearchResult> index = new HashMap<>();

    public void add(String token, String gid, ResultType resultType) {
        SearchResult results;
        if (index.containsKey(token)) {
            results = index.get(token);
        } else {
            results = new SearchResult(token);
            index.put(token, results);
        }
        results.add(gid, resultType);
    }

    void empty() {
        index = new HashMap<>();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, SearchResult> entry : index.entrySet()) {
            result.append(entry.getKey());
            result.append(": ");
            result.append(entry.getValue().toString());
            result.append(System.lineSeparator());
        }
        return result.toString();
    }

    /**
     * Store in the database.
     */
    public void store() throws SQLException {
        for (SearchResult entry : index.values()) {
            SearchResult searchResult = entry;
            searchResult.store();
        }
    }
}
