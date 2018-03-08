package Search;

import Database.ElasticConnection;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import jsonSerializer.JacksonSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@ToString
public class Recording {
    @JsonIgnore
    @Getter
    private final String gid;
    @Getter private final String name;
    @Getter private final String work_gid;

    @Getter private final List<String> artists = new ArrayList<>();
    @Getter private final List<Release> releases = new ArrayList<>();

    static Recording fromElastic(JsonNode node) {
        String gid = node.get("_id").textValue();
        String name = node.get("_source").get("name").textValue();
        String work_gid = node.get("_source").get("work_gid").textValue();

        Recording recording = new Recording(gid, name, work_gid);

        for (JsonNode artist : node.get("_source").get("artists")) {
            recording.artists.add(artist.textValue());
        }

        for (JsonNode release : node.get("_source").get("releases")) {
            recording.releases.add(Release.fromElastic(release));
        }

        return recording;
    }

    @AllArgsConstructor
    @ToString
    static class Release {
        @Getter private final String gid;
        @Getter private final String release_name;
        @Getter private final String coverArtUrl;

        static Release fromElastic(JsonNode node) {
            return JacksonSerializer.getInstance().readValue(node, Release.class);
        }
    }
}
