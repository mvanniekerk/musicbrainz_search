package Aggregation.DataStore;

import Aggregation.dataType.MBWork;
import Database.ElasticConnection;
import Search.Work;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WorkStore extends DataStore implements Iterable<MBWork> {

    private final Map<String, MBWork> works;

    public WorkStore(int lowerID, int higherID) {
        super(lowerID, higherID);
        works = new HashMap<>();
    }

    private PreparedStatement getWorkNames() throws SQLException {
        Connection conn = getConnection();

        return conn.prepareStatement(
        "SELECT name, gid, comment FROM work " +
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
        "SELECT artist.sort_name as name, work.gid FROM recording " +
            "JOIN l_recording_work ON entity0=recording.id " +
            "JOIN work ON entity1=work.id " +
            "JOIN artist_credit ON artist_credit.id=artist_credit " +
            "JOIN artist_credit_name ON artist_credit_name.artist_credit=artist_credit.id " +
            "JOIN artist ON artist_credit_name.artist=artist.id " +
            "WHERE (work.id >= ?) AND (work.id < ?)"
        );
    }

    private PreparedStatement getComposer() throws SQLException {
        Connection conn = getConnection();

        return conn.prepareStatement(
        "SELECT artist.sort_name as name, work.gid FROM artist " +
            "JOIN l_artist_work ON entity0=artist.id " +
            "JOIN work ON entity1=work.id " +
            "JOIN link ON l_artist_work.link=link.id " +
            "WHERE link.link_type != 846 " + // 846 is dedicated to
            "AND (work.id >= ?) AND (work.id < ?)"
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

    private void checkForDuplicates(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            String comment = resultSet.getString("comment");
            String gid = resultSet.getString("gid");
            MBWork work = find(gid);
            if (comment != null && comment.equals("catch-all for arrangements")) {
                work.setIgnore(true);
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

    public void aggregateFromDB(int from, int to) throws SQLException {
        ResultSet workNames = executePreparedStatement(getWorkNames(), from, to);
        populateNames(workNames);

        // TODO: This solution is not very clean, since we query the result set twice
        ResultSet workDuplicateCheck = executePreparedStatement(getWorkNames(), from, to);
        checkForDuplicates(workDuplicateCheck);

        ResultSet workAliases = executePreparedStatement(getWorkAliasNames(), from, to);
        populateNames(workAliases);

        ResultSet recordingNames = executePreparedStatement(getRecordingName(), from, to);
        populateNames(recordingNames);

        ResultSet artists = executePreparedStatement(getArtists(), from, to);
        populateArtists(artists);

        ResultSet composers = executePreparedStatement(getComposer(), from, to);
        populateComposers(composers);
    }

    public void aggregateParts() throws SQLException {
        Set<String> keys = new HashSet<>(works.keySet());
        for (String key : keys) {
            works.get(key).addParts();
        }
    }

    private MBWork find(String gid) {
        MBWork result = works.get(gid);

        if (result == null) {
            result = new MBWork(gid, getConnection(), this);
            works.put(gid, result);
        }

        return result;
    }

    public MBWork retrieve(int id, String gid) throws SQLException {
        MBWork result = works.get(gid);

        if (result == null) {
            aggregateFromDB(id);
            result = works.get(gid);
            assert result != null;
            return result;
        } else {
            return result;
        }
    }

    public void elasticStore() {
        ElasticConnection conn = ElasticConnection.getInstance();

        for (MBWork work : works.values()) {
            String json = work.jsonSearchRepr();
            String gid = work.getGid();
            if (!work.isIgnore()) {
                conn.storeDocument(json, gid);
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
