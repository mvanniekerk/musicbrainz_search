package Search;

import static org.assertj.core.api.Assertions.assertThat;

import Database.ElasticConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

public class ResultTest {

    @AfterAll
    static void close() {
        ElasticConnection.getInstance().close();
    }

    @Test
    void treeStyleResults() {
        String resultString =
                ElasticConnection.getInstance().search("beethoven cello sonata 3", "", "", 0, 2);

        Result result = Result.fromElastic(resultString);
        assertThat(result.getWorks().get(0).getParent()).isNull();

        // Potentially flaky
        assertThat(result.getWorks()).hasSize(1);
        assertThat(result.getWorks().get(0).getChildren()).hasSize(1);
        assertThat(result.getWorks().get(0).getGid()).isEqualTo("015dadf8-f382-434b-b88a-3838e7199358");
        // Beethoven cello sonata 3
    }

    @Test
    void parentsWillBeGotten() {
        String resultString =
                ElasticConnection.getInstance().search("beethoven cello sonata 3", "", "", 1, 1);
        Result result = Result.fromElastic(resultString);

        assertThat(result.getWorks()).hasSize(1);
        assertThat(result.getWorks().get(0).getParent()).isNull();
    }


}
