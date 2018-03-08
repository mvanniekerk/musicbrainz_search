package Aggregation.DataStore;

import static org.assertj.core.api.Assertions.assertThat;

import Aggregation.dataType.MBRecording;
import Database.ElasticConnection;
import com.fasterxml.jackson.databind.JsonNode;
import jsonSerializer.JacksonSerializer;
import org.junit.jupiter.api.Test;

public class RecordingTest {
    @Test
    void getRecordingsTest() throws Exception {
        RecordingStore recordingStore = new RecordingStore(0,0);

        recordingStore.aggregateFromDB(12834183);

        assertThat(recordingStore.getMap().size()).isEqualTo(3);

        for (MBRecording recording : recordingStore) {
            assertThat(recording.getWork_gid()).isEqualTo("80a09313-56b6-4bb1-9870-6b5ac5fbc0aa");
            System.out.println(recording);
        }
    }

    @Test
    void shouldHaveArtist() throws Exception {
        String gid = "5fe6dc83-72a7-41b1-8cbe-f3f748b4e308";
        RecordingStore recordingStore = new RecordingStore(0,0);

//        recordingStore.aggregateFromDB(19609);
//        recordingStore.aggregateFromDB(19624);
//        recordingStore.aggregateFromDB(19820);
//        recordingStore.aggregateFromDB(19823);
//        recordingStore.aggregateFromDB(19825);
//        recordingStore.aggregateFromDB(19827);
//        recordingStore.aggregateFromDB(19937);

        recordingStore.aggregateFromDB(19609, 19938);

        boolean containsIt = false;
        System.out.println(recordingStore.getMap());
        for (MBRecording recording : recordingStore) {
            if (recording.getGid().equals(gid)) {
                assertThat(recording.getArtists()).containsOnly("Radiohead");
                containsIt = true;
            }
        }
        assertThat(containsIt).isTrue();


        recordingStore.elasticStore();
        String doc = ElasticConnection.getInstance().getDocument(gid, "mb", "recording");
        JsonNode node = JacksonSerializer.getInstance().readTree(doc);
        String artist = node.get("_source").get("artists").get(0).asText();
        assertThat(artist).isEqualTo("Radiohead");
    }

    @Test
    void getRecordingsJson() throws Exception {
        RecordingStore recordingStore = new RecordingStore(0,0);

        recordingStore.aggregateFromDB(12834183);

        for (MBRecording recording : recordingStore) {
            System.out.println(recording.jsonSearchRepr());
        }
    }
}
