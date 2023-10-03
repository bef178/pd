package pd.codec;

import java.util.BitSet;

public class PercentCodec {

    private static final BitSet UNRESERVED = new BitSet(128);

    static {
        // reminder: in rfc3986, reserved + unreserved != ascii
        for (int i = 'A'; i <= 'Z'; i++) {
            UNRESERVED.set(i);
        }
        for (int i = 'a'; i <= 'z'; i++) {
            UNRESERVED.set(i);
        }
        for (int i = '0'; i <= '9'; i++) {
            UNRESERVED.set(i);
        }
        UNRESERVED.set('-');
        UNRESERVED.set('.');
        UNRESERVED.set('_');
        UNRESERVED.set('~');
    }

    static boolean shouldEncode(byte byteValue) {
        return byteValue < 0 || !UNRESERVED.get(byteValue);
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
        if (shouldEncode(byteValue)) {
            if (dst != null) {
                dst[start++] = '%';
                HexCodec.encode1byte(byteValue, dst, start);
            }
            return 3;
        } else {
            if (dst != null) {
                dst[start] = byteValue;
            }
            return 1;
        }
    }
}
