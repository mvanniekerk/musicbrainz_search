package Search;

import DataStore.WorkStore;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor
public class SearchMap {
    private final Map<String, SearchResult> index = new HashMap<>();

    public void add(String token, String gid) {
        SearchResult results;
        if (index.containsKey(token)) {
            results = index.get(token);
        } else {
            results = new SearchResult();
            index.put(token, results);
        }
        results.add(gid);
    }

    @Nullable
    public Set<String> find(String token) {
        if (index.containsKey(token)) {
            return index.get(token).get();
        }

        return null;
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

    public static void main(String[] args) throws SQLException {
        WorkStore works = new WorkStore(3566000, 4000000);

        works.aggregateFromDB();

        SearchMap searchMap = new SearchMap();
        works.populateSearchMap(searchMap);

        System.out.println(searchMap.toString());
    }
}
