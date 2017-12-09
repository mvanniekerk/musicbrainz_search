package Database;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.Connection;

public class SearchDB extends DBConnection {
    private static final String URL = "jdbc:postgresql:search";

    @Nullable
    private static SearchDB searchDB;

    private SearchDB() {
        super(URL);
    }

    public static Connection getInstance() {
        if (searchDB == null) {
            searchDB = new SearchDB();
        }
        return searchDB.getConnection();
    }
}
