package Search;

import Database.ElasticConnection;
import Database.SearchDB;
import Tokenizer.Tokenizer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import jsonSerializer.JacksonSerializer;
import jsonSerializer.JsonSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ToString(exclude = {"tempWorks"})
@AllArgsConstructor
public class Result {
    @Getter
    private final List<Work> works = new ArrayList<>();

    @JsonIgnore
    private final Map<String, Work> tempWorks = new HashMap<>();

    @Getter
    private final int took;

    void storeTempWorks() {
        for (Work work : tempWorks.values()) {
            String parentGid = work.getParent();

            if (parentGid != null) {
                Work parent = tempWorks.get(parentGid);
                if (parent != null) {
                    parent.addChild(work);
                } else {
                    // getWorkFromGid();
                    // TODO
                    throw new RuntimeException("Parent was not in the list of works");
                }
            }
        }
    }

    static Result fromElastic(String resultString) {
        JsonNode result = JacksonSerializer.getInstance().readTree(resultString);
        int took = result.get("took").intValue();

        Result res = new Result(took);

        JsonNode resultList = result.get("hits").get("hits");
        for (JsonNode workNode : resultList) {
            Work work = Work.fromElastic(workNode);
            if (work.getParent() == null) {
                res.works.add(work);
            }
            res.tempWorks.put(work.getGid(), work);
        }

        res.storeTempWorks();
        return res;
    }

    public static void main(String[] args) {
        String resultString =
                ElasticConnection.getInstance().search("beethoven cello sonata 3", "", "", 0, 50);

        Result result = Result.fromElastic(resultString);
        System.out.println(result.toString());

        ElasticConnection.getInstance().close();
    }
}
