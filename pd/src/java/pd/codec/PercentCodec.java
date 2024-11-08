package pd.codec;

import java.nio.charset.StandardCharsets;
import java.util.BitSet;

import pd.util.HexCodec;
import pd.util.InstallmentByteBuffer;

public class PercentCodec {

    private static final HexCodec hexCodec = new HexCodec();

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
     * consume 1 byte and produce 1 or 3 int32<br/>
     * return number of produced bytes
     */
    public static int encode1byte(byte byteValue, int[] dst, int start) {
        if (shouldEncode(byteValue)) {
            if (dst != null) {
                dst[start++] = '%';
                hexCodec.encode1byte(byteValue, dst, start);
            }
            return 3;
        } else {
            if (dst != null) {
                dst[start] = byteValue;
            }
            return 1;
        }
    }

    public static int encode1byte(byte byteValue, StringBuilder sb) {
        if (shouldEncode(byteValue)) {
            if (sb != null) {
                sb.append('%');
                int[] dst = new int[2];
                hexCodec.encode1byte(byteValue, dst, 0);
                sb.appendCodePoint(dst[0]).appendCodePoint(dst[1]);
            }
            return 3;
        } else {
            if (sb != null) {
                sb.appendCodePoint(byteValue);
            }
            return 1;
        }
    }

    public static StringBuilder encode(String s, StringBuilder sb) {
        for (byte i : s.getBytes(StandardCharsets.UTF_8)) {
            encode1byte(i, sb);
        }
        return sb;
    }

    public static String encode(String s) {
        return encode(s, new StringBuilder()).toString();
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
                dst[start] = hexCodec.decode1byte(hiByte, loByte);
            }
            return 3;
        } else {
            if (dst != null) {
                dst[start] = (byte) a[i];
            }
            return 1;
        }
    }

    public static byte[] decodeToBytes(String s) {
        InstallmentByteBuffer byteBuffer = new InstallmentByteBuffer();
        byte[] dst = new byte[1];
        int[] unicodes = s.codePoints().toArray();
        int i = 0;
        while (i < unicodes.length) {
            int numConsumed = PercentCodec.decode1byte(unicodes, i, dst, 0);
            i += numConsumed;
            byteBuffer.push(dst[0]);
        }
        return byteBuffer.copyBytes();
    }

    public static String decode(String s) {
        return new String(decodeToBytes(s), StandardCharsets.UTF_8);
    }
}
