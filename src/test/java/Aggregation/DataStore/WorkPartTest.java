package Aggregation.DataStore;

import static org.assertj.core.api.Assertions.assertThat;

import Aggregation.dataType.MBWork;
import org.junit.jupiter.api.Test;

public class WorkPartTest {

    @Test
    void getPartsAsIdTest() throws Exception {
        WorkStore workStore = new WorkStore(0,0);
        // gid: 597e017a-2c97-40c0-9eeb-430a0461e4ad
        // This is "Der Ring des Nibelungen, WWV 86"

        workStore.aggregateFromDB(12693839);

        for (MBWork work : workStore) {
            // It has 4 parts
            assertThat(work.getPartsAsID()).hasSize(4);
        }
    }

    @Test
    void getPartsTest() throws Exception {
        WorkStore workStore = new WorkStore(0,0);
        // gid: 597e017a-2c97-40c0-9eeb-430a0461e4ad
        // This is "Der Ring des Nibelungen, WWV 86"

        workStore.aggregateFromDB(12693839);

        for (MBWork work : workStore) {
            // It has 4 parts
            work.addParts();
        }

        for (MBWork work : workStore) {
            System.out.println(work.jsonSearchRepr());
        }
    }
}
