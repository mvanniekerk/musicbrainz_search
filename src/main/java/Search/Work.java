package Search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@ToString(of = {"gid", "children"})
public class Work {
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

    void addChild(Work work) {
        children.add(work);
    }

    static Work fromElastic(JsonNode node) {
        String gid = node.get("_id").textValue();
        String parent = node.get("_source").get("workParent").textValue();

        Work work = new Work(gid, parent);

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
}
