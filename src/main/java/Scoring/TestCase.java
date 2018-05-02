package Scoring;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@AllArgsConstructor
class TestCase {
    @Getter
    private final String query;
    @Getter private final String expected;
}
