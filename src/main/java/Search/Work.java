package Search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import jsonSerializer.JacksonSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@ToString(of = {"gid", "children", "score"})
public class Work implements Comparable<Work> {
    private final List<String> artists = new ArrayList<>();
    private final List<String> composers = new ArrayList<>();
    private final List<String> names = new ArrayList<>();

    @Getter
    private final List<Work> children = new ArrayList<>();

    @Getter
    private final String gid;

    @Getter
    @JsonIgnore
    private final String parent;

    @Getter
    @JsonIgnore
    private final double score;

    void addChild(Work work) {
        children.add(work);
    }

    static Work fromElastic(String node) {
        JsonNode jsonNode = JacksonSerializer.getInstance().readTree(node);
        return fromElastic(jsonNode);
    }

    static Work fromElastic(JsonNode node) {
        String gid = node.get("_id").textValue();
        String parent = node.get("_source").get("workParent").textValue();
        double score = node.get("_score").asDouble();

        Work work = new Work(gid, parent, score);

        for (JsonNode artist : node.get("_source").get("artists")) {
            work.artists.add(artist.textValue());
        }

        for (JsonNode name : node.get("_source").get("names")) {
            work.names.add(name.textValue());
        }

        for (JsonNode composer : node.get("_source").get("composers")) {
            work.composers.add(composer.textValue());
        }

        return work;
    }

    void sort() {
        children.sort(Work::compareTo);

        for (Work child : children) child.sort();
    }

    @Override
    public int compareTo(@NotNull Work o) {
        return Double.compare(o.score, score);
    }
}
