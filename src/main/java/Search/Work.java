package Search;

import Database.SearchDB;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


@EqualsAndHashCode(of = {"gid"} )
@ToString
public class Work implements Comparable<Work> {
    private static final int TOTAL_N_DOCS = 851366;
    @Getter
    private final String gid;
    @Getter
    private final int length;

    private int id;

    @Getter
    private double tfIdf = 0;

    // term, termFrequency
    @Getter
    private final Map<Term, Integer> terms = new HashMap<>();


    public Work(String gid, int length) {
        this.gid = gid;
        this.length = length;
    }


    Connection getConnection() {
        return SearchDB.getInstance();
    }

    public void addTermCount(Term term, Integer count) {
        assert count != 0;
        if (terms.containsKey(term)) {
            int oldCount = terms.get(term);
            terms.replace(term, oldCount + count);
        } else {
            terms.put(term, count);
        }
    }

    void calculateTfIdf() {
        double result = 0;
        assert length > 0 : "Length should be a natural number";
        for (Map.Entry<Term, Integer> termCount : terms.entrySet()) {
            double tf = (double) termCount.getValue() / Math.log10(10 + length);
            assert termCount.getValue() > 0 : "Term count should be a natural number";
            int count = termCount.getKey().getFrequency();
            assert count > 0 : "Term count (in work) should be a natural number";
            double idf = Math.log10((double) TOTAL_N_DOCS / count);
            double tf_idf = tf * idf;
            result += tf_idf;
        }
        tfIdf = result;
    }

    @Override
    public int compareTo(Work other) {
        int numTerms = Integer.compare(other.terms.size(), this.terms.size());
        if (numTerms != 0) {
            return numTerms;
        }
        return Double.compare(other.tfIdf, this.tfIdf);
    }


    private PreparedStatement documentQuery() throws SQLException {
        Connection conn = getConnection();
        return conn.prepareStatement(
                "INSERT INTO documents (gid, length)\n" +
                        "VALUES (?::uuid, ?)\n" +
                        "ON CONFLICT (gid) DO UPDATE SET length = documents.length + ?\n" +
                        "RETURNING id"
        );
    }

    public void store() throws SQLException {
        int size = length;
        PreparedStatement docQuery = documentQuery();
        docQuery.setString(1, gid);
        docQuery.setInt(2, size);
        docQuery.setInt(3, size);

        ResultSet docResult = docQuery.executeQuery();
        docResult.next();
        id = docResult.getInt(1);
    }

    public void storeTerms() throws SQLException {
        getConnection().setAutoCommit(false);
        for (Map.Entry<Term, Integer> entry : terms.entrySet()) {
            entry.getKey().store(id,  entry.getValue());
        }
        getConnection().commit();
        getConnection().setAutoCommit(true);
    }
}
