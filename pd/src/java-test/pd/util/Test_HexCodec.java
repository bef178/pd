package pd.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class Test_HexCodec {

    HexCodec hexCodec = HexCodec.withLowerCaseLetters();

    @Test
    public void test_decode1byte() {
        assertEquals(0xB2, hexCodec.decode('B', '2') & 0xFF);
    }

    @Test
    public void test_encode1byte() {
        int[] dst = new int[2];
        hexCodec.encode((byte) 0x61, dst, 0);
        assertEquals('6', dst[0]);
        assertEquals('1', dst[1]);
    }

    @Test
    public void test_encodeToString() {
        // value from md5sum "a\n"
        byte[] a = new byte[] { 96, -73, 37, -15, 12, -100, -123, -57, 13, -105, -120, 13, -2, -127, -111, -77 };
        assertEquals("60b725f10c9c85c70d97880dfe8191b3", hexCodec.encodeToString(a));
    }
}
