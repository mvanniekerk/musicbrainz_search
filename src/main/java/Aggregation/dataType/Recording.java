package Aggregation.dataType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jsonSerializer.JacksonSerializer;
import jsonSerializer.JsonSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@ToString
public class Recording {
    @Getter private final String gid;
    @Getter private final String name;
    @Getter private final String work_gid;

    @Getter private final List<String> artists = new ArrayList<>();
    @Getter private final List<Release> releases = new ArrayList<>();


    public void addRelease(Release release) {
        releases.add(release);
    }

    public String jsonSearchRepr() {
        JsonSerializer serializer = JacksonSerializer.getInstance();

        return serializer.writeAsString(this);
    }

    @AllArgsConstructor
    @ToString
    public static class Release {
        @Getter private final String gid;
        @Getter private final String release_name;
        @JsonIgnore private final String release_gid;
        @JsonIgnore private final long cover_art;

        @Nullable
        public String getCoverArtUrl() {
            if (cover_art == 0) return null;
            return "https://coverartarchive.org/release/" + release_gid + "/" + cover_art + ".jpg";
        }
    }
}
