package Search;

import Database.ElasticConnection;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import jsonSerializer.JacksonSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

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
            storeTempWork(work);
        }
    }

    void storeTempWork(Work work) {
        String parentGid = work.getParent();
        if (parentGid != null) {
            Work parent = tempWorks.get(parentGid);
            if (parent == null) {
                String parentDoc = ElasticConnection.getInstance().getDocument(parentGid);
                parent = Work.fromElastic(parentDoc);
                storeTempWork(parent);
            }
            parent.addChild(work);
        } else {
            works.add(work);
        }
    }

    static Result fromElastic(String resultString) {
        JsonNode result = JacksonSerializer.getInstance().readTree(resultString);
        int took = result.get("took").intValue();

        Result res = new Result(took);

        JsonNode resultList = result.get("hits").get("hits");
        for (JsonNode workNode : resultList) {
            Work work = Work.fromElastic(workNode);
            res.tempWorks.put(work.getGid(), work);
        }

        res.storeTempWorks();
        return res;
    }
}
