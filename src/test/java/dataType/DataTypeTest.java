package dataType;

import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class DataTypeTest {

    @Test
    void emptyArtistTest() {
        byte[] result = new Artist("").jsonSearchRepr();

        assertThat(decode(result)).isEqualTo("{\"names\":[]}");
    }

    @Test
    void emptyWorkTest() {
        byte[] result = new Work("").jsonSearchRepr();

        assertThat(decode(result)).isEqualTo("{\"artists\":[],\"composers\":[],\"names\":[]}");
    }

    @Test
    void artistName() {
        Artist artist = new Artist("");
        artist.addName("you");

        assertThat(decode(artist.jsonSearchRepr())).isEqualTo("{\"names\":[\"you\"]}");
    }

    private String decode(byte[] inp) {
        try {
            return new String(inp, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            fail(e.getMessage());
            return ""; // why is this even necessary?
        }
    }
}
