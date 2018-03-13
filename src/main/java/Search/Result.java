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

    @Getter private final int took;
    @Getter private int total;

    private void storeTempWorks() {
        for (Work work : tempWorks.values()) {
            storeTempWork(work);
        }
    }

    private void sort() {
        works.sort(Work::compareTo);

        for (Work work : works) work.sort();
    }

    private void storeTempWork(Work work) {
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
        int took = result.get("took").asInt(0);
        int total = result.get("hits").get("total").intValue();

        Result res = new Result(took, total);

        JsonNode resultList = result.get("hits").get("hits");
        int recordingTop = 5;
        for (JsonNode workNode : resultList) {
            Work work = Work.fromElastic(workNode);

            if (recordingTop > 0) work.retrieveRecordings("");
            recordingTop--;

            res.tempWorks.put(work.getGid(), work);
        }

        res.storeTempWorks();
        res.sort();

        return res;
    }
}
