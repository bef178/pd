package blackbox;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import pd.codec.HexCodec;

public class Test_HexCodec {

    @Test
    public void test_decode1byte() {
        int[] a = { '6', '1' };
        int byteValue = HexCodec.decode1byte(a[0], a[1]) & 0xFF;
        assertEquals('a', byteValue);
    }

    @Test
    public void test_encode1byte() {
        byte byteValue = 0x61;
        int[] dst = new int[2];
        HexCodec.encode1byte(byteValue, dst, 0);
        assertEquals('6', dst[0]);
        assertEquals('1', dst[1]);
    }
}
