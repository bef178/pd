package pd.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test_PercentCodec {

    PercentCodec percentCodec = new PercentCodec();

    @Test
    public void test_encode1byte() {
        int[] a1 = new int[1];
        int[] a3 = new int[3];

        // unreserved
        assertEquals(1, percentCodec.encode1byte((byte) 'a', a1, 0));
        assertArrayEquals(new int[] { 'a' }, a1);

        // reserved
        assertEquals(3, percentCodec.encode1byte((byte) '#', a3, 0));
        assertArrayEquals(new int[] { '%', '2', '3' }, a3);

        // neither reserved nor unreserved
        assertEquals(3, percentCodec.encode1byte((byte) '|', a3, 0));
        assertArrayEquals(new int[] { '%', '7', 'C'}, a3);
    }

    @Test
    public void test_encode() {
        assertEquals("azAZ09-._~", percentCodec.encode("azAZ09-._~"));
        assertEquals("%5E%60%7B%7D%7C", percentCodec.encode("^`{}|"));
        assertEquals("%E7%99%BE%E5%88%86%E5%8F%B7%E7%BC%96%E7%A0%81", percentCodec.encode("百分号编码"));
    }
}
