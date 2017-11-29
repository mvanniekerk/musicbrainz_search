package Search;

import Database.MusicBrainzDB;
import dataType.DataType;
import dataType.Work;
import lombok.Getter;
import org.postgresql.util.PSQLException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TypedSearchResult implements SearchResult {
    private final List<DataType> work_name = new ArrayList<>();
    private final List<DataType> work_artist = new ArrayList<>();
    private final List<DataType> work_composer = new ArrayList<>();

    @Getter
    private final String term;
    @Getter
    private int frequency;

    TypedSearchResult(String term) {
        this.term = term;
    }

    public void add(String gid, ResultType type) {
        Work work = new Work(gid);
        switch (type) {
            case WORK_ARTIST:
                work_artist.add(work); break;
            case WORK_NAME:
                work_name.add(work); break;
            case WORK_COMPOSER:
                work_composer.add(work); break;
        }
    }

    public void add(Work work, ResultType type) {
        add(work.getGid(), type);
    }

    Connection getConnection() {
        return MusicBrainzDB.getConnection();
    }

    private PreparedStatement termQuery() throws SQLException {
        Connection conn = getConnection();
        return conn.prepareStatement(
        "INSERT INTO terms (term, freq)\n" +
            "VALUES (?, 1)\n" +
            "ON CONFLICT (term) DO UPDATE SET freq = terms.freq + 1\n" +
            "RETURNING id"
        );
    }

    private PreparedStatement documentQuery() throws SQLException {
        Connection conn = getConnection();
        return conn.prepareStatement(
        "INSERT INTO documents (gid, length)\n" +
            "VALUES (?::uuid, 1)\n" +
            "ON CONFLICT (gid) DO UPDATE SET length = documents.length + 1\n" +
            "RETURNING id"
        );
    }

    private PreparedStatement doc_term_query() throws SQLException {
        Connection conn = getConnection();
        return conn.prepareStatement(
        "INSERT INTO documents_terms (term_id, document_id, type, freq) VALUES\n" +
            "(?, ?, ?, 1)\n" +
            "ON CONFLICT (term_id, document_id, type) DO UPDATE SET freq = documents_terms.freq + 1"
        );
    }

    private void executeQuery(List<DataType> documents, ResultType resultType) throws SQLException {
        for (DataType dataType : documents) {
            PreparedStatement termQuery = termQuery();
            termQuery.setString(1, getTerm());
            int termID;
            try {
                ResultSet termResult = termQuery.executeQuery();
                termResult.next();
                termID = termResult.getInt(1);
            } catch (PSQLException e) {
                e.printStackTrace();
                return;
            }
            PreparedStatement documentQuery = documentQuery();
            documentQuery.setObject(1, dataType.getGid());
            ResultSet docResult = documentQuery.executeQuery();
            docResult.next();
            int docID = docResult.getInt(1);

            PreparedStatement doc_term_query = doc_term_query();
            doc_term_query.setInt(1, termID);
            doc_term_query.setInt(2, docID);
            doc_term_query.setInt(3, resultType.getIndex());
            doc_term_query.executeUpdate();
        }
    }

    void store() throws SQLException {
        executeQuery(work_artist, ResultType.WORK_ARTIST);
        executeQuery(work_composer, ResultType.WORK_COMPOSER);
        executeQuery(work_name, ResultType.WORK_NAME);
    }

    int getResultSize() {
        return work_artist.size() + work_composer.size() + work_name.size();
    }

    @Override
    public String toString() {
        return "TypedSearchResult{" +
                "term=" + term +
                ", work_artist=" + work_artist +
                ", work_composer=" + work_composer +
                ", work_name='" + work_name + '\'' +
                '}';
    }
}
