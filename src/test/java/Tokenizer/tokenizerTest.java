package Tokenizer;

import Tokenizer.Tokenizer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class tokenizerTest {
    @Test
    void isAlreadyAscii() {
        assertThat(Tokenizer.toAscii("Paul Tortelier")).isEqualTo("Paul Tortelier");
    }

    @Test
    void dots() {
        assertThat(Tokenizer.tokenize("The Saga Of H.M.S. Bounty Vangelis")).isEqualTo(new String[0]);
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

    @Test
    void lemmatizerTest() {
        assertThat(Tokenizer.lemmatize("Tĥïŝ ĩš â fůňķŷ Šťŕĭńġ")).isEqualTo("this is a funky string");
    }

    @Test
    void punctuationShouldPersist() {
        assertThat(Tokenizer.lemmatize("Dora Mae's Funeral")).isEqualTo("dora mae's funeral");
    }

    @Test
    void tokenizer() {
        String[] result = {"justin", "at", "mr", "chin's", "justin's", "theme"};
        assertThat(Tokenizer.tokenize("Justin at Mr. Chin's (Justin's Theme)")).isEqualTo(result);
    }

    @Test
    void classicalTokenizer() {
        String input = "Cello Suite no. 1 in G major, BWV 1007: I. Prelude";
        String[] result = {"cello", "suite", "no", "1", "in", "g", "major", "bwv", "1007", "i", "prelude"};
        assertThat(Tokenizer.tokenize(input)).isEqualTo(result);
    }

    @Test
    void withStrangeChars() {
        String input = "Suite NR. 1 G-DUR, BWV 1007 - Sarabande";
        String[] result = {"suite", "nr", "1", "g", "dur", "bwv", "1007", "sarabande"};
        assertThat(Tokenizer.tokenize(input)).isEqualTo(result);
    }

    @Test
    void emptyTest() {
        assertThat(Tokenizer.tokenize("")).isEqualTo(new String[0]);
    }

    @Test
    void leadingWhiteSpace() {
        String[] result = {"suite"};
        assertThat(Tokenizer.tokenize(" Suite")).isEqualTo(result);
    }

    @Test
    void leadingWhiteChar() {
        String[] result = {"suite"};
        assertThat(Tokenizer.tokenize(", Suite")).isEqualTo(result);
    }

    @Test
    void allIgnorable() {
        assertThat(Tokenizer.tokenize(", ")).isEqualTo(new String[0]);
    }

    @Test
    void endingWhiteSpace() {
        String[] result = {"suite"};
        assertThat(Tokenizer.tokenize("   ,     Suite    ,   ")).isEqualTo(result);
    }
}
