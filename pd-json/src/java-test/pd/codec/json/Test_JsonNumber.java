package pd.codec.json;

import org.junit.jupiter.api.Test;
import pd.codec.json.datafactory.JsonFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Test_JsonNumber {

    private static final JsonFactory f = JsonCodec.f;

    @Test
    public void test_IJsonNumber_isRoundNumber() {
        assertTrue(f.createJsonNumber(4294967296L).isRoundNumber());
        assertFalse(f.createJsonNumber(5.000001).isRoundNumber());
    }
}
