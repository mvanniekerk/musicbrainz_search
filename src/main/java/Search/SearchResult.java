package Search;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@NoArgsConstructor
public class SearchResult implements Iterable<String> {
    final Set<String> gid_list = new HashSet<>();

    public void add(String gid) {
        gid_list.add(gid);
    }

    public Set<String> get() {
        return new HashSet<>(gid_list);
    }

    public Set<String> intersect(SearchResult searchResult) {
        Set<String> intersection = new HashSet<>(this.gid_list);
        intersection.retainAll(searchResult.gid_list);
        return intersection;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        for (String result : gid_list) {
            sb.append(result);
            sb.append(", ");
        }
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 2);
        }
        sb.append("}");
        return sb.toString();
    }


    @NotNull
    @Override
    public Iterator<String> iterator() {
        return gid_list.iterator();
    }
}
