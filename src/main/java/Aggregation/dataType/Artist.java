package Aggregation.dataType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jsonSerializer.JacksonSerializer;
import jsonSerializer.JsonSerializer;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(of = {"gid"}, callSuper = false)
public class Artist extends DataType {
    private final Set<String> names = new HashSet<>();

    @Getter
    @JsonIgnore
    private String gid;

    public Artist(String gid) {
        this.gid = gid;
    }

    public void addName(String name) {
        names.add(name);
    }

    public Collection<String> getNameTokens() {
        return getTokensFromList(names);
    }

    @Override
    public byte[] jsonSearchRepr() {
        JsonSerializer serializer = JacksonSerializer.getInstance();

        return serializer.writeAsBytes(this);
    }
}
