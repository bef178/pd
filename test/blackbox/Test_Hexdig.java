package blackbox;

import pd.fenc.Hexdig;

public class Test_Hexdig {

    public static void main(String[] args) {
        test_encode1byte();
        test_decode1byte();
        System.out.println("all done");
    }

    private static void test_decode1byte() {
        byte[] src = { '9', '7' };
        int dstByte = Hexdig.decode1byte(src, 0);
        assert (dstByte == 'a');
    }

    private static void test_encode1byte() {
        byte[] dst = new byte[2];
        Hexdig.encode1byte('a', dst, 0);
        assert (dst[0] == '9');
        assert (dst[1] == '7');
    }
}
