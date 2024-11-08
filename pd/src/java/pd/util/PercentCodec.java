package pd.util;

import java.nio.charset.StandardCharsets;

public class PercentCodec {

    private final HexCodec hexCodec = new HexCodec();

    /**
     * total 7
     */
    boolean isGenDelimiters(int ch) {
        switch (ch) {
            case ':':
            case '/':
            case '?':
            case '#':
            case '[':
            case ']':
            case '@':
                return true;
            default:
                return false;
        }
    }

    /**
     * total 11
     */
    boolean isSubDelimiters(int ch) {
        switch (ch) {
            case '!':
            case '$':
            case '&':
            case '\'':
            case '(':
            case ')':
            case '*':
            case '+':
            case ',':
            case ';':
            case '=':
                return true;
            default:
                return false;
        }
    }

    boolean isReserved(int ch) {
        return isGenDelimiters(ch) || isSubDelimiters(ch);
    }

    /**
     * total 66
     */
    boolean isUnreserved(int ch) {
        if (ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z') {
            return true;
        }
        if (ch >= '0' && ch <= '9') {
            return true;
        }
        switch (ch) {
            case '-':
            case '.':
            case '_':
            case '~':
                return true;
            default:
                return false;
        }
    }

    boolean shouldEncode(byte byteValue) {
        return byteValue < 0 || !isUnreserved(byteValue);
    }

    public String encode(String s) {
        return encode(s, new StringBuilder()).toString();
    }

    public StringBuilder encode(String s, StringBuilder sb) {
        for (byte i : s.getBytes(StandardCharsets.UTF_8)) {
            encode1byte(i, sb);
        }
        return sb;
    }

    public void encode1byte(byte byteValue, StringBuilder sb) {
        int[] dst = new int[3];
        int n = encode1byte(byteValue, dst, 0);
        if (n == 1) {
            sb.appendCodePoint(dst[0]);
        } else if (n == 3) {
            sb.appendCodePoint(dst[0]);
            sb.appendCodePoint(dst[1]);
            sb.appendCodePoint(dst[2]);
        }
    }

    /**
     * consume 1 byte and produce 1 or 3 int32<br/>
     * return number of produced bytes
     */
    public int encode1byte(byte byteValue, int[] dst, int start) {
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

    public String decode(String s) {
        return new String(decodeToBytes(s), StandardCharsets.UTF_8);
    }

    public byte[] decodeToBytes(String s) {
        InstallmentByteBuffer byteBuffer = new InstallmentByteBuffer();
        byte[] dst = new byte[1];
        int[] unicodes = s.codePoints().toArray();
        int i = 0;
        while (i < unicodes.length) {
            i += decode1byte(unicodes, i, dst, 0);
            byteBuffer.push(dst[0]);
        }
        return byteBuffer.copyBytes();
    }

    /**
     * consume 1 or 3 int32 and produce 1 byte<br/>
     * return number of consumed int32
     */
    public int decode1byte(int[] a, int i, byte[] dst, int start) {
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
}
