package Database;


import java.sql.Connection;
import java.sql.SQLException;

public class SearchTestDB extends DBConnection {
    private static final String URL = "jdbc:postgresql:searchtest";

    public static void removeCreate() throws SQLException {
        DBConnection postgres = new DBConnection("jdbc:postgresql:postgres");
        Connection connection = postgres.getConnection();
        connection.createStatement().execute(
                "SELECT pg_terminate_backend(pg_stat_activity.pid)\n" +
                        "FROM pg_stat_activity\n" +
                        "WHERE pg_stat_activity.datname = 'searchtest'\n" +
                        "  AND pid <> pg_backend_pid();" +
                "DROP DATABASE IF EXISTS searchtest"
        );
        connection.createStatement().execute(
                "CREATE DATABASE searchtest"
        );
    }

    public SearchTestDB() {
        super(URL);

    }

    public static void createTables() throws SQLException {
        SearchTestDB searchTestDB = new SearchTestDB();
        searchTestDB.getConnection().createStatement().execute(
                "CREATE TABLE terms\n" +
                        "(\n" +
                        "  id SERIAL PRIMARY KEY,\n" +
                        "  term CHAR(50) UNIQUE NOT NULL,\n" +
                        "  freq INTEGER NOT NULL\n" +
                        ");\n" +
                        "\n" +
                        "CREATE TABLE documents\n" +
                        "(\n" +
                        "  id SERIAL PRIMARY KEY,\n" +
                        "  gid UUID UNIQUE NOT NULL,\n" +
                        "  length INTEGER NOT NULL\n" +
                        ");\n" +
                        "\n" +
                        "CREATE TABLE documents_terms\n" +
                        "(\n" +
                        "  term_id INTEGER REFERENCES terms(id),\n" +
                        "  document_id INTEGER REFERENCES documents(id),\n" +
                        "  freq INTEGER NOT NULL,\n" +
                        "  type SMALLINT,\n" +
                        "  PRIMARY KEY (term_id, document_id, type)\n" +
                        ");"
        );
    }
}
