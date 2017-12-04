package Search;

import Database.MusicBrainzDB;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@ToString
@NoArgsConstructor
public class Result {
    private final Map<String, Work> works = new HashMap<>();

    private Connection getConnection() {
        return MusicBrainzDB.getConnection();
    }

    Work getWork(String gid, int length) {
        if (works.containsKey(gid)) {
            return works.get(gid);
        }
        Work work = new Work(gid, length);
        works.put(gid, work);

        return work;
    }

    void retrieveTerm(String termName) throws SQLException {
        Connection conn = getConnection();

        PreparedStatement ps = conn.prepareStatement(
        "SELECT terms.freq\n" +
            "FROM terms\n" +
            "WHERE terms.term=?"
        );

        ps.setString(1, termName);
        ResultSet resultSet = ps.executeQuery();

        resultSet.next();
        int termFreq = resultSet.getInt(1);

        Term term = new Term(termFreq, termName);

        retrieveDocuments(term);
    }

    private void retrieveDocuments(Term term) throws SQLException {
        Connection conn = getConnection();

        PreparedStatement ps = conn.prepareStatement(
        "SELECT documents.gid, documents.length, documents_terms.freq\n" +
            "FROM terms\n" +
            "INNER JOIN documents_terms ON terms.id=documents_terms.term_id\n" +
            "INNER JOIN documents ON documents_terms.document_id=documents.id\n" +
            "WHERE terms.term=?"
        );

        ps.setString(1, term.getTerm());
        ResultSet resultSet = ps.executeQuery();

        while (resultSet.next()) {
            String gid = resultSet.getString("gid");
            int length = resultSet.getInt("length");
            int freq = resultSet.getInt("freq");

            Work work = getWork(gid, length);
            work.addTermCount(term, freq);
        }
    }

    public static void main(String[] args) throws SQLException {
        Result result = new Result();
        result.retrieveTerm("mozart");
        System.out.println(result);
    }
}
