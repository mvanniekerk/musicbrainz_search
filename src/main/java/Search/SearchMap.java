package Search;

import DataStore.WorkStore;
import Database.MusicBrainzDB;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor
public class SearchMap {
    private Map<String, SearchResult> index = new HashMap<>();

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

    Connection getConnection() {
        return MusicBrainzDB.getConnection();
    }

    /**
     * Store in the database.
     */
    public void store() throws SQLException {
        Connection conn = getConnection();

        for (Map.Entry<String, SearchResult> entry : index.entrySet()) {
            for (String gid : entry.getValue()) {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO search(search_string, gid, type) " +
                            "VALUES (?, ?::uuid, 0) " +
                            "ON CONFLICT (search_string, gid) DO NOTHING "
                );

                ps.setString(1, entry.getKey());
                ps.setObject(2, gid);

                ps.executeUpdate();
            }

        }
    }

    public static void main(String[] args) throws SQLException {
        WorkStore works = new WorkStore(3500000, 3600000);

        works.aggregateFromDB();

        SearchMap searchMap = new SearchMap();
        works.populateSearchMap(searchMap);

        searchMap.store();
    }
}
