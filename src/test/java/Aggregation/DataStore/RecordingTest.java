package Aggregation.DataStore;

import static org.assertj.core.api.Assertions.assertThat;

import Aggregation.dataType.Recording;
import lombok.ToString;
import org.junit.jupiter.api.Test;

public class RecordingTest {
    @Test
    void getRecordingsTest() throws Exception {
        RecordingStore recordingStore = new RecordingStore(0,0);

        recordingStore.aggregateFromDB(12834183);

        assertThat(recordingStore.getRecordings().size()).isEqualTo(3);

        for (Recording recording : recordingStore) {
            assertThat(recording.getWork_gid()).isEqualTo("80a09313-56b6-4bb1-9870-6b5ac5fbc0aa");
            System.out.println(recording);
        }
    }

    @Test
    void getRecordingsJson() throws Exception {
        RecordingStore recordingStore = new RecordingStore(0,0);

        recordingStore.aggregateFromDB(12834183);

        for (Recording recording : recordingStore) {
            System.out.println(recording.jsonSearchRepr());
        }
    }
}
