package Search;

import Database.ElasticConnection;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import jsonSerializer.JacksonSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@ToString(exclude = {"tempWorks"})
@AllArgsConstructor
public class Result {
    @Getter
    private final List<Work> works = new ArrayList<>();

    @JsonIgnore
    @Getter
    private final Map<String, Work> tempWorks = new HashMap<>();

    @Getter private final int took;
    @Getter private int total;

    void storeTempWorks() throws IOException {
        for (Work work : tempWorks.values()) {
            storeTempWork(work);
        }
    }

    void sort() {
        works.sort(Work::compareTo);

        for (Work work : works) work.sort();
    }

    private void storeTempWork(Work work) throws IOException {
        String parentGid = work.getParent();
        if (parentGid != null) {
            Work parent = tempWorks.get(parentGid);
            if (parent == null) {
                String parentDoc = ElasticConnection.getInstance().getDocument(parentGid);
                JsonNode jsonNode = JacksonSerializer.getInstance().readTree(parentDoc);
                if (jsonNode.get("found").asBoolean()) {
                    parent = Work.fromElastic(jsonNode);
                    storeTempWork(parent);
                } else {
                    System.out.println("Parent does not exist: {child: " + work.getGid() + ", parent: " + work.getParent() + "}");
                }
            }
            if (parent != null) parent.addChild(work);
        } else {
            works.add(work);
        }
    }

    public List<Work> getLeaves() {
        List<Work> leaves = new ArrayList<>();
        for (Work work : works) {
            leaves.addAll(work.getLeaves());
        }
        return leaves;
    }

    public List<List<Work>> getTraversals() {
        return works.stream()
                .map(Work::getDFSTraversals)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public static Result fromElastic(String resultString) throws IOException {
        JsonNode result = JacksonSerializer.getInstance().readTree(resultString);
        int took = result.get("took").asInt(0);
        int total = result.get("hits").get("total").intValue();

        Result res = new Result(took, total);

        JsonNode resultList = result.get("hits").get("hits");
        for (JsonNode workNode : resultList) {
            Work work = Work.fromElastic(workNode);

            res.tempWorks.put(work.getGid(), work);
        }

        res.storeTempWorks();
        res.sort();

        return res;
    }
}
