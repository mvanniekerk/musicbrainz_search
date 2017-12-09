package Aggregation.DataStore;

import Aggregation.Store.ResultType;
import Aggregation.Store.SearchMap;
import Aggregation.dataType.MBWork;
import Search.Work;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WorkStore extends DataStore implements Iterable<MBWork> {

    private final Map<String, MBWork> works;

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
            assert gid != null;
            MBWork work = find(gid);
            String name = resultSet.getString("name");
            if (name != null) {
                work.addName(name);
            }
        }
    }

    private void populateArtists(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            String gid = resultSet.getString("gid");
            assert gid != null;
            MBWork work = find(gid);
            String artist = resultSet.getString("name");
            if (artist != null) {
                work.addArtist(artist);
            }
        }
    }

    private void populateComposers(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            String gid = resultSet.getString("gid");
            assert gid != null;
            MBWork work = find(gid);
            String composer = resultSet.getString("name");
            if (composer != null) {
                work.addComposer(composer);
            }
        }
    }

    public void aggregateFromDB() throws SQLException {
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

    private MBWork find(String gid) {
        MBWork result = works.get(gid);

        if (result == null) {
            result = new MBWork(gid);
            works.put(gid, result);
        }

        return result;
    }

    public void populateSearchMap(SearchMap searchMap) {
        for (MBWork work : works.values()) {
            Collection<String> artists = work.getArtistTokens();
            Collection<String> composers = work.getComposerTokens();
            Collection<String> names = work.getNameTokens();

            for (String artist : artists) {
                searchMap.add(artist, work, ResultType.WORK_ARTIST);
            }

            for (String composer : composers) {
                searchMap.add(composer, work, ResultType.WORK_COMPOSER);
            }

            for (String name : names) {
                searchMap.add(name, work, ResultType.WORK_NAME);
            }
        }
    }

    public void store() throws SQLException {
        for (MBWork mbWork : works.values()) {
            Work work = mbWork.toSearchWork();
            work.store();
            work.storeTerms();
        }
    }

    @Override
    public Iterator<MBWork> iterator() {
        return works.values().iterator();
    }
}
