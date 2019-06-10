package Aggregation.DataStore;

import Aggregation.dataType.MBWork;
import Database.ElasticConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class WorkStore extends DataStore<MBWork> {
    private static final String INDEX = "musicbrainz";
    private static final String TYPE = "work";

    public WorkStore(int lowerID, int higherID) {
        super(lowerID, higherID);
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

    /**
     * Each track must always be associated with a single recording,
     * but a recording can be linked to any number of tracks.
     * A track is a release of a recording.
     */
    private PreparedStatement getTrackName() throws SQLException {
        Connection conn = getConnection();

        return conn.prepareStatement(
        "SELECT DISTINCT track.name, work.gid FROM recording\n" +
            "JOIN l_recording_work ON entity0=recording.id\n" +
            "JOIN work ON entity1=work.id\n" +
            "JOIN track ON track.recording=recording.id\n" +
            "WHERE (WORK.id >= ?) AND (WORK.id < ?)"
        );
    }

    private PreparedStatement getRecordingName() throws SQLException {
        Connection conn = getConnection();

        return conn.prepareStatement(
        "SELECT DISTINCT recording.name, work.gid FROM recording\n" +
            "JOIN l_recording_work ON entity0=recording.id\n" +
            "JOIN work ON entity1=work.id\n" +
            "WHERE (WORK.id >= ?) AND (WORK.id < ?)"
        );
    }

    /**
     * link type 281 indicates that a work is made up of
     * multiple parts (e.g. an orchestral suite broken into movements).
     * eg: work "is part of" workparent.
     */
    private PreparedStatement getWorkParent() throws SQLException {
        Connection conn = getConnection();

        return conn.prepareStatement(
        "SELECT workparent.gid as parent, work.gid FROM work\n" +
            "JOIN l_work_work ON work.id=l_work_work.entity1\n" +
            "JOIN link ON l_work_work.link=link.id\n" +
            "JOIN work AS workparent ON l_work_work.entity0=workparent.id\n" +
            "WHERE (work.id >= ?) AND (work.id < ?)\n" +
            "AND link.link_type=281 AND l_work_work.edits_pending=0"
        );
    }

    private PreparedStatement getArtists() throws SQLException {
        Connection conn = getConnection();

        return conn.prepareStatement(
        "SELECT artist.sort_name as name, work.gid FROM recording " +
            "JOIN l_recording_work ON entity0=recording.id " +
            "JOIN work ON entity1=work.id " +
            "JOIN artist_credit ON artist_credit.id=recording.artist_credit " +
            "JOIN artist_credit_name ON artist_credit_name.artist_credit=artist_credit.id " +
            "JOIN artist ON artist_credit_name.artist=artist.id " +
            "WHERE (work.id >= ?) AND (work.id < ?)"
        );
    }

    private PreparedStatement getComposer() throws SQLException {
        Connection conn = getConnection();

        return conn.prepareStatement(
        "SELECT DISTINCT artist.sort_name as name, artist_alias.name as alias, work.gid FROM artist " +
            "JOIN l_artist_work ON entity0=artist.id " +
            "JOIN work ON entity1=work.id " +
            "JOIN link ON l_artist_work.link=link.id " +
            "LEFT JOIN artist_alias ON artist_alias.artist=artist.id " +
            "AND (locale IS NULL or locale='en' or locale='nl') " +
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

    private void setParents(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            String parent = resultSet.getString("parent");
            String gid = resultSet.getString("gid");
            MBWork work = find(gid);
            work.setWorkParent(parent);
        }
    }

    private void populateArtists(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            String gid = resultSet.getString("gid");
            MBWork work = find(gid);
            String artist = resultSet.getString("name");
            if (artist != null) {
                work.addArtist(artist);
            }
        }
    }

    private void populateComposers(ResultSet resultSet) throws SQLException {
        String currName = "";
        while (resultSet.next()) {
            String gid = resultSet.getString("gid");
            assert gid != null;
            MBWork work = find(gid);
            String composer = resultSet.getString("name");
            String alias = resultSet.getString("alias");
            if (alias != null) {
                if (!currName.equals(composer)) {
                    work.addComposer(composer);
                    currName = composer;
                }
                work.addComposer(alias);
            } else {
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

        ResultSet parents = executePreparedStatement(getWorkParent(), from, to);
        setParents(parents);

        ResultSet workAliases = executePreparedStatement(getWorkAliasNames(), from, to);
        populateNames(workAliases);

        ResultSet recordingNames = executePreparedStatement(getRecordingName(), from, to);
        populateNames(recordingNames);

        ResultSet trackNames = executePreparedStatement(getTrackName(), from, to);
        populateNames(trackNames);

        ResultSet artists = executePreparedStatement(getArtists(), from, to);
        populateArtists(artists);

        ResultSet composers = executePreparedStatement(getComposer(), from, to);
        populateComposers(composers);
    }

    public void aggregateParts() throws SQLException {
        Set<String> keys = new HashSet<>(map.keySet());
        for (String key : keys) {
            MBWork work = map.get(key);
            work.addParts();
        }
    }


    private MBWork find(String gid) {
        MBWork result = map.get(gid);

        if (result == null) {
            result = new MBWork(gid, getConnection(), this);
            map.put(gid, result);
        }

        return result;
    }

    public MBWork retrieve(int id, String gid) throws SQLException {
        MBWork result = map.get(gid);

        if (result == null) {
            aggregateFromDB(id);
            result = map.get(gid);
            assert result != null;
            return result;
        } else {
            return result;
        }
    }

    public void elasticStore() {
        ElasticConnection conn = ElasticConnection.getInstance();
        conn.storeBulk(INDEX, TYPE, this);
    }
}
