package Aggregation.DataStore;

import Aggregation.dataType.Recording;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class RecordingStore extends DataStore implements Iterable<Recording> {

    @Getter
    private final Map<String, Recording> recordings = new HashMap<>();

    public RecordingStore(int lowerID, int higherID) {
        super(lowerID, higherID);
    }

    private Recording find(String gid, String name, String work_gid) {
        Recording recording = recordings.get(gid);

        if (recording == null) {
            recording = new Recording(gid, name, work_gid);
            recordings.put(gid, recording);
        }

        return recording;
    }

    private PreparedStatement getArtists() throws SQLException {
        Connection conn = getConnection();

        return conn.prepareStatement(
        "SELECT artist.sort_name as name, recording.gid FROM recording " +
                "JOIN l_recording_work ON entity0=recording.id " +
                "JOIN work ON entity1=work.id " +
                "JOIN artist_credit ON artist_credit.id=artist_credit " +
                "JOIN artist_credit_name ON artist_credit_name.artist_credit=artist_credit.id " +
                "JOIN artist ON artist_credit_name.artist=artist.id " +
                "WHERE (work.id >= ?) AND (work.id < ?)"
        );
    }

    private PreparedStatement getReleases() throws SQLException {
        Connection conn = getConnection();

        return conn.prepareStatement(
        "SELECT work.gid AS work, track.gid AS track, " +
                "  recording.gid AS recording, recording.name AS name, " +
                "  release.name AS release, release.gid AS release_gid, " +
                "  index_listing.id AS cover_art FROM recording " +
                "JOIN l_recording_work ON l_recording_work.entity0=recording.id " +
                "JOIN work ON entity1=work.id " +
                "JOIN track ON track.recording=recording.id " +
                "JOIN medium ON track.medium=medium.id " +
                "JOIN release ON medium.release=release.id " +
                "LEFT JOIN cover_art_archive.index_listing " +
                "ON index_listing.release=release.id AND is_front " +
                "WHERE (work.id >= ?) AND (work.id < ?)"
        );
    }

    private void populateReleases(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            String gid = resultSet.getString("recording");
            String name = resultSet.getString("name");
            String work_gid = resultSet.getString("work");

            String track = resultSet.getString("track");
            String release_name = resultSet.getString("release");
            String release_gid = resultSet.getString("release_gid");
            long cover_art = resultSet.getLong("cover_art");

            Recording recording = find(gid, name, work_gid);

            Recording.Release release = new Recording.Release(track, release_name, release_gid, cover_art);

            recording.addRelease(release);
        }
    }

    @Override
    void aggregateFromDB(int from, int to) throws SQLException {
        ResultSet releases = executePreparedStatement(getReleases(), from, to);
        populateReleases(releases);
    }

    @Override
    public Iterator<Recording> iterator() {
        return recordings.values().iterator();
    }
}
