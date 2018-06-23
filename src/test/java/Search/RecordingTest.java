package Search;

import static org.assertj.core.api.Assertions.assertThat;

import Database.ElasticConnection;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

@Ignore
public class RecordingTest {

    @Test
    void getRecordingFromWork() throws Exception {
        String doc = ElasticConnection.getInstance().getDocument("c9980c62-29e2-48a8-bb74-4909d3df175d");
        // Prokofiev cello concerto e minor, 1st mvmt

        Work res = Work.fromElastic(doc);
        res.retrieveRecordings("");

        assertThat(res.getRecordings()).hasSize(4);
        // note that, on september 2017 a 5th recording was added
        // however, it is not yet in the version of the db I'm using. So this test will fail in the future

        Recording recording = res.getRecordings().get(0);
        assertThat(recording.getName()).isEqualTo("Cello Concerto in E minor, op. 58: I. Andante");

        ElasticConnection.getInstance().close();
    }
}
