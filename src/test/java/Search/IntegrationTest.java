package Search;

import Database.SearchTestDB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class IntegrationTest {
    private SearchTestDB searchTestDB;
    private WorkTest work;
    public static final int length = 200;

    @BeforeEach
    void setUp() throws SQLException {
        SearchTestDB.removeCreate();
        SearchTestDB.createTables();
        searchTestDB = new SearchTestDB();
        createWork();
    }

    private void createWork() throws SQLException {
        work = new WorkTest("5c2c958e-c754-479c-bf9e-993919b2d863", length);
        for (int i = 0; i < length/2; i++) {
            String str = getSaltString();
            work.addTermCount(new TermTest(1, str), 2);
        }
        insert();
    }

    private String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

    @Test
    void workCountIsEqual() throws SQLException {
        ResultSet re = searchTestDB.getConnection().createStatement().executeQuery(
                "SELECT length from documents"
        );
        re.next();
        assertThat(re.getInt(1)).isEqualTo(length);
        re.close();
    }

    @Test
    void termCountIsEqual() throws SQLException {
        ResultSet re = searchTestDB.getConnection().createStatement().executeQuery(
                "SELECT sum(freq) from terms"
        );
        re.next();
        assertThat(re.getInt(1)).isEqualTo(length);
    }

    private void insert() throws SQLException {
        // insert
        long startTime = System.currentTimeMillis();
        work.store();
        work.storeTerms();

        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;

        System.out.println("done insert. took " + duration + " ms");
    }

    private void update() throws SQLException {

        long startTime = System.currentTimeMillis();
        work.store();
        work.storeTerms();

        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;

        System.out.println("done update. took " + duration + " ms");
    }


    private class WorkTest extends Work {

        public WorkTest(String gid, int length) {
            super(gid, length);
        }

        @Override
        Connection getConnection() {
            return searchTestDB.getConnection();
        }
    }

    private class TermTest extends Term {

        public TermTest(int frequency, String term) {
            super(frequency, term);
        }

        @Override
        Connection getConnection() {
            return searchTestDB.getConnection();
        }
    }
}
