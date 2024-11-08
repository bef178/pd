package pd.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class Test_HexCodec {

    HexCodec hexCodec = HexCodec.encodeWithLowerCaseLetters();

    @Test
    public void test_decode1byte() {
        int[] a = { '6', '1' };
        int byteValue = hexCodec.decode1byte(a[0], a[1]) & 0xFF;
        assertEquals('a', byteValue);
    }

    @Test
    public void test_encode1byte() {
        byte byteValue = 0x61;
        int[] dst = new int[2];
        hexCodec.encode1byte(byteValue, dst, 0);
        assertEquals('6', dst[0]);
        assertEquals('1', dst[1]);
    }

    @Test
    public void test_toHexString() {
        // value from md5sum "a\n"
        byte[] a = new byte[] { 96, -73, 37, -15, 12, -100, -123, -57, 13, -105, -120, 13, -2, -127, -111, -77 };
        assertEquals("60b725f10c9c85c70d97880dfe8191b3", hexCodec.toHexString(a));
    }
}
