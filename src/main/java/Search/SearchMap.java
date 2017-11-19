package Search;

import Database.MusicBrainzDB;
import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    public SearchResult find(String term) throws SQLException {
        assert !term.contains(" ");
        if (!index.containsKey(term)) {
            retrieveTerm(term);
        }

        return index.get(term);
    }

    private void retrieveTerm(String term) throws SQLException {
        Connection conn = MusicBrainzDB.getConnection();

        PreparedStatement ps = conn.prepareStatement(
            "SELECT gid, type " +
                "FROM search " +
                "WHERE search_string = ?"

        );

        ps.setString(1, term);
        ResultSet resultSet = ps.executeQuery();

        while (resultSet.next()) {
            String gid = resultSet.getString("gid");
            int type = resultSet.getInt("type");

            add(term, gid, ResultType.valueOf(type));
        }
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

    public static void main(String[] args) throws SQLException {
        SearchMap searchMap = new SearchMap();
        System.out.println(searchMap.find("mozart"));
    }
}
