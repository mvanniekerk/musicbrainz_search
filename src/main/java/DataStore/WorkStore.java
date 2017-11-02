package DataStore;

import dataType.Work;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WorkStore extends DataStore implements Iterable<Work> {

    private final Map<String, Work> works;

    public WorkStore(int lowerID, int higherID) {
        super(lowerID, higherID);
        works = new HashMap<>();
    }

    private PreparedStatement getWorkNames() throws SQLException {
        Connection conn = getConnection();

        return conn.prepareStatement(
        "SELECT name, gid FROM work " +
            "WHERE (work.id >= ?) AND (work.id < ?)"
        );
    }

    private PreparedStatement getWorkAliasNames() throws SQLException {
        Connection conn = getConnection();

        return conn.prepareStatement(
        "SELECT work_alias.name, work.gid from work_alias " +
            "LEFT JOIN work on work.id=work_alias.work " +
            "WHERE (work.id >= ?) AND (work.id < ?)"
        );
    }

    private PreparedStatement getRecordingName() throws SQLException {
        Connection conn = getConnection();

        return conn.prepareStatement(
        "SELECT recording.name, work.gid FROM recording " +
            "LEFT JOIN l_recording_work ON entity0=recording.id " +
            "LEFT JOIN work on entity1=work.id " +
            "WHERE (work.id >= ?) AND (work.id < ?)"
        );
    }

    private PreparedStatement getArtists() throws SQLException {
        Connection conn = getConnection();

        return conn.prepareStatement(
        "SELECT artist_credit.name, work.gid from recording " +
            "LEFT JOIN l_recording_work ON entity0=recording.id " +
            "LEFT JOIN work ON entity1=work.id " +
            "LEFT JOIN artist_credit ON artist_credit.id=artist_credit " +
            "WHERE (work.id >= ?) AND (work.id < ?)"
        );
    }

    private PreparedStatement getComposer() throws SQLException {
        Connection conn = getConnection();

        return conn.prepareStatement(
        "SELECT artist.name, work.gid FROM artist " +
            "LEFT JOIN l_artist_work ON entity0=artist.id " +
            "LEFT JOIN work ON entity1=work.id " +
            "WHERE (work.id >= ?) AND (work.id < ?)"
        );
    }

    private void populateNames(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            String gid = resultSet.getString("gid");
            Work work = find(gid);
            String name = resultSet.getString("name");
            if (name != null) {
                work.addName(name);
            }
        }
    }

    private void populateArtists(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            String gid = resultSet.getString("gid");
            Work work = find(gid);
            String artist = resultSet.getString("name");
            if (artist != null) {
                work.addArtist(artist);
            }
        }
    }

    private void populateComposers(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            String gid = resultSet.getString("gid");
            Work work = find(gid);
            String composer = resultSet.getString("name");
            if (composer != null) {
                work.addComposer(composer);
            }
        }
    }

    void aggregateFromDB() throws SQLException {
        ResultSet workNames = executePreparedStatement(getWorkNames());
        populateNames(workNames);

        ResultSet workAliases = executePreparedStatement(getWorkAliasNames());
        populateNames(workAliases);

        ResultSet recordingNames = executePreparedStatement(getRecordingName());
        populateNames(recordingNames);

        ResultSet artists = executePreparedStatement(getArtists());
        populateArtists(artists);

        ResultSet composers = executePreparedStatement(getComposer());
        populateComposers(composers);
    }

    private Work find(String gid) {
        Work result = works.get(gid);

        if (result == null) {
            result = new Work(gid);
            works.put(gid, result);
        }

        return result;
    }

    @Override
    public Iterator<Work> iterator() {
        return works.values().iterator();
    }

    public static void main(String[] args) throws Exception {
        WorkStore works = new WorkStore(0, 1000);

        works.aggregateFromDB();

        for (Work work : works) {
            byte[] json = work.jsonSearchRepr();
            String jsonString = new String(json, "utf-8");

            System.out.print(work.getGid() + ", ");
            System.out.println(jsonString);
        }
    }
}
