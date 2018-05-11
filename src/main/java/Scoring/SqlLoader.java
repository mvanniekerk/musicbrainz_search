package Scoring;

import Database.MusicBrainzDB;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@AllArgsConstructor
public class SqlLoader implements Loader {
    int testSize;
    double seed;


    public TestCase[] loadTestCases() throws SQLException {
        assert seed > 0.0 && seed < 1.0;

        Connection conn = MusicBrainzDB.getInstance();
        PreparedStatement seedStatement = conn.prepareStatement("SELECT setseed(?)");
        seedStatement.setDouble(1, seed);
        seedStatement.executeQuery();

        PreparedStatement ps = conn.prepareStatement(
                "SELECT lastfm_name, work_gid\n" +
                        "FROM lastfm_works\n" +
                        "WHERE work_gid is not null\n" +
                        "ORDER BY random()\n" +
                        "LIMIT ?;");
        ps.setInt(1, testSize);
        ResultSet rs = ps.executeQuery();

        int index = 0;
        TestCase[] testCases = new TestCase[testSize];
        while (rs.next()) {
            String name = rs.getString("lastfm_name");
            String gid = rs.getString("work_gid");

            testCases[index] = new TestCase(name, gid);
            index++;
        }
        assert index == testSize;
        return testCases;
    }
}
