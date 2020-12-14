package blackbox;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import pd.fenc.Hexdig;

public class Test_Hexdig {

    // vm arguments: -ea
    public static void main(String[] args) {
    }

    @Test
    public void test_decode1byte() {
        byte[] src = { '6', '1' };
        int dstByte = Hexdig.decode1byte(src, 0);
        assertEquals('a', dstByte);
    }

    @Test
    public void test_encode1byte() {
        byte[] dst = new byte[2];
        Hexdig.encode1byte('a', dst, 0);
        assertEquals('6', dst[0]);
        assertEquals('1', dst[1]);
    }
}
