package Aggregation.dataType;

import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class DataTypeTest {

    @Test
    void emptyArtistTest() {
        String result = new Artist("").jsonSearchRepr();

        assertThat(result).isEqualTo("{\"names\":[]}");
    }

    @Test
    void emptyWorkTest() {
        String result = new MBWork("", null).jsonSearchRepr();

        assertThat(result).isEqualTo("{\"artists\":[],\"composers\":[],\"names\":[]}");
    }

    @Test
    void artistName() {
        Artist artist = new Artist("");
        artist.addName("you");

        assertThat(artist.jsonSearchRepr()).isEqualTo("{\"names\":[\"you\"]}");
    }
}
