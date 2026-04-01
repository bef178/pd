package pd.time;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test_SimpleTimeOffset {

    @Test
    public void test_toString() {
        assertEquals("P-1.500", new SimpleTimeOffset(-1500).toString());
    }
}
