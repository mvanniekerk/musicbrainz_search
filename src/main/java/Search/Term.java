package Search;

import Database.SearchDB;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@AllArgsConstructor
@EqualsAndHashCode(of = "term")
@ToString
public class Term {
    @Getter
    private final int frequency;
    @Getter
    private final String term;

    Connection getConnection() {
        return SearchDB.getInstance();
    }

    private PreparedStatement termQuery() throws SQLException {
        Connection conn = getConnection();
        return conn.prepareStatement(
                "INSERT INTO terms (term, freq)\n" +
                        "VALUES (?, ?)\n" +
                        "ON CONFLICT (term) DO UPDATE SET freq = terms.freq + ?\n" +
                        "RETURNING id"
        );
    }

    private PreparedStatement doc_term_query() throws SQLException {
        Connection conn = getConnection();
        return conn.prepareStatement(
                "INSERT INTO documents_terms (term_id, document_id, freq, type) VALUES\n" +
                        "(?, ?, ?, 0)\n" +
                        "ON CONFLICT (term_id, document_id, type) DO UPDATE SET freq = documents_terms.freq + ?"
        );
    }

    public void store(int work_id, int frequency) throws SQLException {
        PreparedStatement query = termQuery();
        query.setString(1, term);
        query.setInt(2, frequency);
        query.setInt(3, frequency);

        ResultSet queryResult = query.executeQuery();
        queryResult.next();
        int id = queryResult.getInt(1);

        PreparedStatement docQuery = doc_term_query();
        docQuery.setInt(1, id);
        docQuery.setInt(2, work_id);
        docQuery.setInt(3, frequency);
        docQuery.setInt(4, frequency);

        docQuery.executeUpdate();
    }
}
