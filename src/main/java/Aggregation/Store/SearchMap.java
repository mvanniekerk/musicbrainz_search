package Aggregation.Store;

import Database.MusicBrainzDB;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public class SearchMap {
    @Getter
    private Map<String, SearchResult> index = new HashMap<>();

    public void add(String token, String gid, ResultType resultType) {
        SearchResult results;
        if (index.containsKey(token)) {
            results = index.get(token);
        } else {
            results = new TypedSearchResult(token);
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
            "SELECT terms.freq, documents.gid, \n" +
                "  documents.length, documents_terms.freq, documents_terms.type\n" +
                "FROM terms\n" +
                "INNER JOIN documents_terms ON terms.id=documents_terms.term_id\n" +
                "INNER JOIN documents ON documents_terms.document_id=documents.id\n" +
                "WHERE terms.term=?"
        );

        ps.setString(1, term);
        ResultSet resultSet = ps.executeQuery();

        while (resultSet.next()) {
            String gid = resultSet.getString("gid");
            int type = resultSet.getInt("type");

            assert type == 0 || type == 1 || type == 2;

            add(term, gid, ResultType.valueOf(type));
        }
    }

    public void empty() {
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
            entry.store();
        }
    }

    public static void main(String[] args) throws SQLException {
        SearchMap searchMap = new SearchMap();
        SearchResult searchResult = searchMap.find("mozart");
        System.out.println(searchResult);
        System.out.println(searchResult.getResultSize());
    }
}
