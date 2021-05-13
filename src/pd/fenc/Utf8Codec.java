package pd.fenc;

/**
 * https://en.wikipedia.org/wiki/UTF-8<br/>
 * <br/>
 * Utf8 works as a protocol between int32 stream and int8 stream.<br/>
 * An Utf8 tuple costs 7 bytes at most, being able to represent 36 effective bits.<br/>
 * Non-negative int32 uses 31 bits at most, costing 6 bytes.<br/>
 * While, stopping at U+10FFFF, an Unicode actually uses 21 bits at most, that is 4 bytes.<br/>
 * Without code point validation, we can enhance the performance.<br/>
 */
public class Utf8Codec {

    public static int checkUtf8FollowerByte(int value) {
        if (isUtf8FollowerByte(value)) {
            return value;
        }
        throw new ParsingException(
                String.format("expected a utf8 follower byte, actual [0x%X]", value));
    }

    public static int checkUtf8HeadByte(int value) {
        if (isUtf8HeadByte(value)) {
            return value;
        }
        throw new ParsingException(
                String.format("expected a utf8 head byte, actual [0x%X]", value));
    }

    /**
     * get a single ucs4 from utf8 stream
     */
    public static int decode1unit(byte[] a, int i, int[] dst, int start) {
        int headByte = checkUtf8HeadByte(a[i++]);
        int n = getNumUtf8BytesByUtf8HeadByte(headByte);
        switch (n) {
            case 1:
                dst[start] = headByte;
                return 1;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6: {
                int unheader = 0xFF >> n;
                int ucs4 = unheader & headByte;
                for (int j = 1; j < n; j++) {
                    ucs4 = (ucs4 << 6) | (checkUtf8FollowerByte(a[i++]) & 0x3F);
                }
                dst[start] = ucs4;
                return n;
            }
            default:
                break;
        }
        throw new ParsingException();
    }

    /**
     * a fast encoder without checking content
     */
    public static void encode1unit(int ucs4, IWriter utf8) {
        int n = getNumUtf8Bytes(ucs4);
        if (n == 1) {
            utf8.push(ucs4);
        } else {
            for (int i = 0; i < n; i++) {
                int v = (ucs4 >> ((n - 1 - i) * 6)) & 0x3F;
                if (i == 0) {
                    int header = (0xFF << (8 - n)) & 0xFF;
                    utf8.push(v | header);
                } else {
                    utf8.push(v | 0x80);
                }
            }
        }
    }

    public static int getNumUtf8Bytes(int ucs4) {
        if (ucs4 < 0) {
            // dummy
        } else if (ucs4 <= 0x7F) {
            return 1;
        } else if (ucs4 <= 0x7FF) {
            return 2;
        } else if (ucs4 <= 0xFFFF) {
            // LE 16-bit
            return 3;
        } else if (ucs4 <= 0x1FFFFF) {
            // LE 21-bit
            return 4;
        } else if (ucs4 <= 0x3FFFFFF) {
            // LE 26-bit
            return 5;
        } else if (ucs4 <= 0x7FFFFFFF) {
            // LE 31-bit
            return 6;
        }
        throw new ParsingException();
    }

    public static int getNumUtf8BytesByUtf8HeadByte(int utf8HeadByte) {
        assert isUtf8HeadByte(utf8HeadByte);

        int n = 0;
        while (n < 8 && Cstream.getBit((byte) utf8HeadByte, n)) {
            ++n;
        }
        switch (n) {
            case 0:
                return 1;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return n;
            default:
                // invalid utf8 head
                throw new ParsingException();
        }

    }

    /**
     * value in [0b10000000,0b10111111]
     */
    public static boolean isUtf8FollowerByte(int value) {
        return value >= 0x80 && value <= 0xBF;
    }

    /**
     * value is ascii or in [0b11000000,0b11111110]
     */
    public static boolean isUtf8HeadByte(int value) {
        return (value >= 0 && value <= 0x7F) || (value >= 0xC0 && value <= 0xFE);
    }
}
