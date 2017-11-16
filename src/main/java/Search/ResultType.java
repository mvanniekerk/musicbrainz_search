package Search;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ResultType {
    WORK_NAME(0),
    WORK_ARTIST(1),
    WORK_COMPOSER(2);

    @Getter
    private final int index;
}
