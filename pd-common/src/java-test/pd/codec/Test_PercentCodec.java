package pd.codec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test_PercentCodec {

    @Test
    public void test_encode1byte() {
        int[] a1 = new int[1];
        int[] a3 = new int[3];

        // unreserved
        assertEquals(1, PercentCodec.encode1byte((byte) 'a', a1, 0));
        assertArrayEquals(new int[] { 'a' }, a1);

        // reserved
        assertEquals(3, PercentCodec.encode1byte((byte) '#', a3, 0));
        assertArrayEquals(new int[] { '%', '2', '3' }, a3);

        // neither reserved nor unreserved
        assertEquals(3, PercentCodec.encode1byte((byte) '|', a3, 0));
        assertArrayEquals(new int[] { '%', '7', 'C'}, a3);
    }
}
