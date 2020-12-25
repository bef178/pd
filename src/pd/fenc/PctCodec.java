package pd.fenc;

import java.nio.charset.StandardCharsets;
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
     * @return num produced bytes of src
     */
    public static int decode(byte[] src, int i, int j, byte[] dst, int start) {
        int k = start;
        while (i < j) {
            i += decode1byte(src, i, dst, k++);
            assert i <= j;
        }
        return k - start;
    }

    public static String decode(String s) {
        byte[] src = s.getBytes(StandardCharsets.UTF_8);
        int numProduced = decode(src, 0, src.length, null, 0);
        byte[] dst = new byte[numProduced];
        decode(src, 0, src.length, dst, 0);
        return new String(dst, StandardCharsets.UTF_8);
    }

    private static int decode1byte(byte[] src, int i, byte[] dst, int start) {
        int srcByte = src[i++] & 0xFF;
        if (srcByte == '%') {
            if (dst != null) {
                dst[start] = (byte) HexCodec.decode1byte(src, i);
            }
            return 3;
        } else {
            if (dst != null) {
                dst[start] = (byte) srcByte;
            }
            return 1;
        }
    }

    /**
     * '[' => '%','5','B'<br/>
     * <br/>
     * @return num produced bytes of dst
     */
    public static int encode(byte[] src, int i, int j, byte[] dst, int start) {
        int k = start;
        while (i < j) {
            k += encode1byte(src[i++], dst, k);
        }
        return k - start;
    }

    public static String encode(String s) {
        byte[] src = s.getBytes(StandardCharsets.UTF_8);
        int numProduced = encode(src, 0, src.length, null, 0);
        byte[] dst = new byte[numProduced];
        encode(src, 0, src.length, dst, 0);
        return new String(dst, StandardCharsets.UTF_8);
    }

    private static int encode1byte(byte srcByte, byte[] dst, int start) {
        if (srcByte >= 0x20 && srcByte < 0x7F && !SHOULD_ENCODE.get(srcByte)) {
            if (dst != null) {
                dst[start] = srcByte;
            }
            return 1;
        } else {
            if (dst != null) {
                dst[start++] = '%';
                HexCodec.encode1byte(srcByte, dst, start);
            }
            return 3;
        }
    }
}
