package pd.encoding;

/**
 * unicode code point => byte[4]
 */
public class Utf32 {

    public static int decodeBe(byte[] a) {
        int codepoint = 0;
        for (int i = 0; i < 4; ++i) {
            codepoint = codepoint << 8 | a[i];
        }
        return codepoint;
    }

    public static byte[] encodeBe(final int codepoint) {
        byte[] a = new byte[4];
        a[0] = (byte) ((codepoint >>> 24) & 0xFF);
        a[1] = (byte) ((codepoint >>> 16) & 0xFF);
        a[2] = (byte) ((codepoint >>> 8) & 0xFF);
        a[3] = (byte) ((codepoint >>> 0) & 0xFF);
        return a;
    }
}
