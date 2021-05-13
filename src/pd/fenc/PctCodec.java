package pd.fenc;

import java.util.BitSet;

public class PctCodec {

    private static final BitSet SHOULD_ENCODE = new BitSet(256);

    static {
        // final byte[] UNRESERVED = { '-', '_', '.', '~' };
        final byte[] GEN_DELIMS = { ':', '/', '?', '#', '[', ']', '@' };
        for (int i = 0; i < GEN_DELIMS.length; i++) {
            SHOULD_ENCODE.set(GEN_DELIMS[i]);
        }
        final byte[] SUB_DELIMS = { '!', '$', '&', '\'', '(', ')', '*', '+', ',', ';', '=' };
        for (int i = 0; i < SUB_DELIMS.length; i++) {
            SHOULD_ENCODE.set(SUB_DELIMS[i]);
        }
    }

    /**
     * consume 1 or 3 int32 and produce 1 byte<br/>
     * return number of consumed int32
     */
    public static int decode1byte(int[] a, int i, byte[] dst, int start) {
        if (a[i] == '%') {
            if (dst != null) {
                int hiByte = a[i + 1];
                int loByte = a[i + 2];
                dst[start] = HexCodec.decode1byte(hiByte, loByte);
            }
            return 3;
        } else {
            if (dst != null) {
                dst[start] = (byte) a[i];
            }
            return 1;
        }
    }

    /**
     * consume 1 byte and produce 1 or 3 int32<br/>
     * return number of produced bytes
     */
    public static int encode1byte(byte byteValue, int[] dst, int start) {
        if (byteValue >= 0x20 && byteValue < 0x7F && !SHOULD_ENCODE.get(byteValue)) {
            if (dst != null) {
                dst[start] = byteValue;
            }
            return 1;
        } else {
            if (dst != null) {
                dst[start++] = '%';
                HexCodec.encode1byte(byteValue, dst, start);
            }
            return 3;
        }
    }
}
