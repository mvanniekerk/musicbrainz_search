package Store;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum ResultType {
    WORK_NAME(0),
    WORK_ARTIST(1),
    WORK_COMPOSER(2);

    @Getter
    private final int index;

    private static final Map<Integer, ResultType> map = new HashMap<>();

    static {
        for (ResultType resultType : ResultType.values()) {
            map.put(resultType.index, resultType);
        }
    }

    public static ResultType valueOf(int index) {
        assert map.containsKey(index) : "Illegal value of index " + index;
        return map.get(index);
    }
}
