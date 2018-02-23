package Aggregation.DataStore;

import static org.assertj.core.api.Assertions.assertThat;

import Aggregation.dataType.MBWork;
import org.junit.jupiter.api.Test;

import java.util.List;

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
    void getPartsTestRing() throws Exception {
        // gid: 597e017a-2c97-40c0-9eeb-430a0461e4ad
        // This is "Der Ring des Nibelungen, WWV 86"
        int partCount = getPartsHelper(12693839);
        assertThat(partCount).isEqualTo(276);
    }

    @Test
    void getPartsTestHaydn() throws Exception {
        // This is "Haydn cello Concerto in C"
        int partCount = getPartsHelper(12515793);

        // 3 movements plus the collection itself
        assertThat(partCount).isEqualTo(3 + 1);
    }

    @Test
    void getNamesHaydn() throws Exception {
        List<String> names = getTheNamesOfTheCollection(12515793);

        assertThat(names.size()).isGreaterThan(1);
    }

    @Test
    void getPartsTestMatthaus() throws Exception {
        // This is "Matthaus passion"
        int partCount = getPartsHelper(12438832);

        assertThat(partCount).isEqualTo(1 + 2 + 29 + 39 + 48);
    }



    int getPartsHelper(int id) throws Exception {
        WorkStore workStore = new WorkStore(0,0);

        workStore.aggregateFromDB(id);

        for (MBWork work : workStore) work.addParts();

        int count = 0;
        for (MBWork work : workStore) {
            count++;
        }
        return count;
    }

    List<String> getTheNamesOfTheCollection(int id) throws Exception {
        WorkStore workStore = new WorkStore(0,0);

        workStore.aggregateFromDB(id);

        for (MBWork work : workStore) {
            work.addParts();
            return work.getNames();
        }
        return null;
    }

    //@Test
    void aggregateForTiming() throws Exception {
        WorkStore works = new WorkStore(12500000, 12510000);

        long startTime = System.currentTimeMillis();
        works.aggregateFromDB();
        works.aggregateParts();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("done. took " + duration + " ms");
        // before aggregating parts: 27817 ms
        // after aggregating parts: 69680 ms
    }
}
