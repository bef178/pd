package pd.encoding;

/**
 * https://en.wikipedia.org/wiki/UTF-8<br/>
 *
 * Consider utf8 a protocol for integer value to octets.
 * It ranges to 7 bytes, i.e. 36 effective bits, covering entire int32.
 * While, Unicode stops at U+10FFFF, taking 21 effective bits.
 */
public class Utf8 {

    public static int decode(byte[] utf8) {
        assert utf8 != null && utf8.length > 0;
        int n = getNumUtf8BytesByLeaderByte(utf8[0]);
        if (n == 1) {
            return utf8[0];
        } else {
            int value = utf8[0] & 0xFF & ~(0xFF >>> (8 - n) << (8 - n));
            for (int i = 1; i < n; ++i) {
                value = (value << 6) | (utf8[i] & 0x3F);
            }
            return value;
        }
    }

    /**
     * a fast encoder without checking content<br/>
     * <br/>
     * returns the size of utf8 bytes
     */
    public static byte[] encode(int value) {
        int n = getNumUtf8Bytes(value);
        byte[] utf8 = new byte[n];
        if (n == 1) {
            utf8[0] = (byte) value;
        } else {
            utf8[0] = (byte) (0xFF >> (8 - n) << (8 - n));
            while (--n > 0) {
                utf8[n] = (byte) (value & 0x3F | 0x80);
                value >>>= 6;
            }
            utf8[0] |= value;
        }
        return utf8;
    }

    /**
     * in [1,7]
     */
    private static int getNumUtf8Bytes(final int value) {
        if (value >= 0) {
            if (value <= 0x7F) {
                return 1;
            } else if (value <= 0x7FF) {
                return 2;
            } else if (value <= 0xFFFF) {
                // LE 16-bit
                return 3;
            } else if (value <= 0x1FFFFF) {
                // LE 21-bit
                return 4;
            } else if (value <= 0x3FFFFFF) {
                // LE 26-bit
                return 5;
            } else if (value <= 0x7FFFFFFF) {
                // LE 31-bit
                return 6;
            }
        }
        return 7;
    }

    private static int getNumUtf8BytesByLeaderByte(int aByte) {
        aByte = aByte & 0xFF;
        int n = 0;
        for (int mask = 1 << 7; mask > 0; mask >>= 1) {
            if ((mask & aByte) == 0) {
                break;
            }
            n++;
        }
        switch (n) {
            case 0:
                return 1;
            case 1:
                throw new IllegalArgumentException();
            case 8:
                throw new IllegalArgumentException();
            default:
                break;
        }
        return n;
    }

    public static boolean isUtf8FollowerByte(int aByte) {
        return (aByte & 0xC0) == 0x80;
    }

    public static boolean isUtf8LeaderByte(int aByte) {
        return (aByte & 0xFF) != 0xFF && !isUtf8FollowerByte(aByte);
    }
}
