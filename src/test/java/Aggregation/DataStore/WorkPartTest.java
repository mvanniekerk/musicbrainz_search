package Aggregation.DataStore;

import static org.assertj.core.api.Assertions.assertThat;

import Aggregation.dataType.MBWork;
import org.junit.jupiter.api.Test;

public class WorkPartTest {

    @Test
    void setup() throws Exception {
        WorkStore workStore = new WorkStore(0,0);
        // gid: 597e017a-2c97-40c0-9eeb-430a0461e4ad
        // This is "Der Ring des Nibelungen, WWV 86"

        workStore.aggregateFromDB(12693839);

        for (MBWork work : workStore) {
            assertThat(work.getPartsAsID()).isEmpty();
        }
    }
}
