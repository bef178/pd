package blackbox;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import pd.fenc.HexCodec;

public class Test_HexCodec {

    // vm arguments: -ea
    public static void main(String[] args) {
    }

    @Test
    public void test_decode1byte() {
        byte[] src = { '6', '1' };
        int dstByte = HexCodec.decode1byte(src, 0);
        assertEquals('a', dstByte);
    }

    @Test
    public void test_encode1byte() {
        byte[] dst = new byte[2];
        HexCodec.encode1byte('a', dst, 0);
        assertEquals('6', dst[0]);
        assertEquals('1', dst[1]);
    }
}
