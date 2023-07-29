package pd.codec.json;

import org.junit.jupiter.api.Test;
import pd.codec.json.datafactory.JsonFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestJsonNumber {

    JsonFactory jsonFactory = JsonFactory.getFactory();

    @Test
    public void test_isRoundNumber() {
        assertTrue(jsonFactory.createJsonNumber(4294967296L).isRoundNumber());
        assertFalse(jsonFactory.createJsonNumber(5.000001).isRoundNumber());
    }
}
