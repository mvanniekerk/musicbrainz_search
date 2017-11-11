import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class tokenizerTest {
    @Test
    void isAlreadyAscii() {
        assertThat(Tokenizer.toAscii("Paul Tortelier")).isEqualTo("Paul Tortelier");
    }

    /**
     * Apparently this is considered a separate letter in Norwegian and Danish. I probably still
     * would like to convert this to an ascii o, since most people searching will use ascii keyboards.
     */
    @Test
    void weirdO() {
        assertThat(Tokenizer.toAscii("Truls Mørk")).isEqualTo("Truls Mork");
    }

    @Test
    void aWithAccent() {
        assertThat(Tokenizer.toAscii("János Starker")).isEqualTo("Janos Starker");
    }

    @Test
    void shouldStayTheSame() {
        assertThat(Tokenizer.toAscii("鷺巣詩郎")).isEqualTo("鷺巣詩郎");
    }

    @Test
    void funkyString() {
        assertThat(Tokenizer.toAscii("Tĥïŝ ĩš â fůňķŷ Šťŕĭńġ")).isEqualTo("This is a funky String");
    }
}
