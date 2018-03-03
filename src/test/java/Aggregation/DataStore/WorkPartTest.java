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
    void getPartsTestForSubPart() throws Exception {
        // 4588c721-3537-34b8-b673-3ad2092d1dc3
        int partCount = getPartsHelper(1627063);

        // This is an example of a part of a greater work
        // It should not find its parent work
        assertThat(partCount).isEqualTo(1 + 0);
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

    @Test
    void noDedicatedTo() throws Exception {
        WorkStore works = new WorkStore(0,0);
        works.aggregateFromDB(12624856); // Lutoslawski cello concerto

        for (MBWork work : works) {
            assertThat(work.getComposers()).doesNotContain("Rostropovich, Mstislav");
            // This piece is dedicated to Rostropovich, but not written by him
            // so he is filtered from the composers.
        }
    }

    @Test
    void preferEnglishNameArtists() throws Exception {
        WorkStore works = new WorkStore(0,0);
        works.aggregateFromDB(7820498); // Rococo variations

        for (MBWork work : works) {
            assertThat(work.getArtists()).doesNotContain("Пётр Ильич Чайковский");
            assertThat(work.getArtists()).contains("Tchaikovsky, Pyotr Ilyich");
            // Prefer the english name for artists
        }
    }

    @Test
    void composersIncludeAliases() throws Exception {
        WorkStore works = new WorkStore(0,0);
        works.aggregateFromDB(7820498); // Rococo variations

        for (MBWork work : works) {
            assertThat(work.getComposers()).contains("Pyotor");
            // Include aliases when they exist
        }
    }

    @Test
    void workParentGetsSetCorrectly() throws Exception {
        WorkStore works = new WorkStore(0,0);
        works.aggregateFromDB(11365407); // Beethoven cello sonata 3, Allegro Ma Non Tanto

        for (MBWork work : works) {
            assertThat(work.getWorkParent()).isEqualTo("015dadf8-f382-434b-b88a-3838e7199358");
            // Beethoven cello sonata 3
        }
    }

    @Test
    void noWorkParentGetsSet() throws Exception {
        WorkStore works = new WorkStore(0,0);
        works.aggregateFromDB(12435976); // Beethoven cello sonata 3

        // We expect no parent to be set here, because the piece in the database does not
        // have a parent. But in reality, it could be nice for the piece to be part of
        // the collection of cello sonatas by Beethoven, even though this is arguably not
        // what you would want.

        for (MBWork work : works) {
            assertThat(work.getWorkParent()).isNull();
        }
    }

    int getPartsHelper(int id) throws Exception {
        return getPartsHelper(id, false);
    }


    int getPartsHelper(int id, boolean print) throws Exception {
        WorkStore workStore = new WorkStore(0,0);
        workStore.aggregateFromDB(id);
        for (MBWork work : workStore) work.addParts();

        int count = 0;
        for (MBWork work : workStore) {
            if (print) System.out.println(work.getGid() + " " + work.getNames());
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
