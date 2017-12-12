package Database;


public class SearchTestDB extends DBConnection {
    private static final String URL = "jdbc:postgresql:search";

    private SearchTestDB() {
        super(URL);
    }
}
