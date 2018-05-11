package Search;

import static org.assertj.core.api.Assertions.assertThat;

import Database.ElasticConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class ResultTest {

    @AfterAll
    static void close() {
        ElasticConnection.getInstance().close();
    }


    void treeStyleResults() throws IOException {
        String resultString;
        try {
            resultString =
                    ElasticConnection.getInstance().search("beethoven cello sonata 3", "", "", 0, 2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        Result result = Result.fromElastic(resultString);
        assertThat(result.getWorks().get(0).getParent()).isNull();


        // Potentially flaky
        assertThat(result.getTotal()).isEqualTo(19);
        assertThat(result.getWorks()).hasSize(1);
        assertThat(result.getWorks().get(0).getChildren()).hasSize(1);
        assertThat(result.getWorks().get(0).getGid()).isEqualTo("015dadf8-f382-434b-b88a-3838e7199358");
        // Beethoven cello sonata 3
    }


    void strangeCase() throws IOException {
        String resultString =
                ElasticConnection.getInstance().search("Yyz Rush", "", "", 0, 20);
        Result results = Result.fromElastic(resultString);


        double prevScore = Double.MAX_VALUE;
        for (Work resultWork : results.getWorks()) {
            assertThat(resultWork.getMaxScore()).isLessThan(prevScore);
            prevScore = resultWork.getMaxScore();
        }

    }


    void parentsWillBeGotten() throws IOException {

        String resultString =
                ElasticConnection.getInstance().search("beethoven cello sonata 3", "", "", 1, 1);
        Result result = Result.fromElastic(resultString);

        assertThat(result.getWorks()).hasSize(1);
        assertThat(result.getWorks().get(0).getParent()).isNull();
        assertThat(result.getTotal()).isEqualTo(19);
    }

    @Test
    void sortingFlatList() throws IOException {
        Work work1 = new Work("a", null, 0.4);
        Work work2 = new Work("b", null, 0.5);
        Work work3 = new Work("c", null, 0.6);

        Result result = new Result(0, 3);

        result.getTempWorks().put(work1.getGid(), work1);
        result.getTempWorks().put(work2.getGid(), work2);
        result.getTempWorks().put(work3.getGid(), work3);

        result.storeTempWorks();
        result.sort();

        List<Work> leaves = result.getLeaves();

        assertThat(leaves.stream().map(Work::getGid).toArray()).isEqualTo(new String[]{"c","b","a"});
    }

    @Test
    void sortingNestedList() throws IOException {
        Work work1 = new Work("a", null, 0.4);
        Work work2 = new Work("b", null, 0.3);
        Work work3 = new Work("c", "b", 0.6);

        Result result = new Result(0, 3);

        result.getTempWorks().put(work1.getGid(), work1);
        result.getTempWorks().put(work2.getGid(), work2);
        result.getTempWorks().put(work3.getGid(), work3);

        result.storeTempWorks();
        result.sort();

        List<Work> leaves = result.getLeaves();

        assertThat(leaves.stream().map(Work::getGid).toArray()).isEqualTo(new String[]{"c","a"});
    }
}
