package pd.fenc;

import pd.ctype.Cbit8;

/**
 * https://en.wikipedia.org/wiki/UTF-8
 *
 * Treat utf8 as a protocol for integer value to octets.
 * It ranges to 7 bytes, i.e. 36 effective bits, covering entire int32.
 * While, Unicode stops at U+10FFFF, taking 21 effective bits.
 */
public class Utf8Codec {

    private static class Decoder implements IReader {

        private IReader src;

        public Decoder(IReader src) {
            this.src = src;
        }

        @Override
        public boolean hasNext() {
            return src.hasNext();
        }

        @Override
        public int next() {
            int[] dst = new int[1];
            Utf8Codec.decode1unit(src, dst, 0);
            return dst[0];
        }
    }

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

    public static int decode(byte[] src, int i, int j, int[] dst, int start) {
        int k = start;
        while (i < j) {
            i += decode1unit(src, i, dst, k++);
        }
        return k - start;
    }

    public static int decode1unit(byte[] src, int i, int[] dst, int start) {
        return decode1unit(IReader.wrap(src, i, src.length), dst, start);
    }

    /**
     * @return num bytes consumed
     */
    public static int decode1unit(IReader src, int[] dst, int start) {
        int headByte = checkUtf8HeadByte(src.next());
        int n = getNumUtf8BytesByUtf8HeadByte(headByte);
        switch (n) {
            case 0:
                break;
            case 1:
                dst[start] = headByte;
                break;
            default: {
                int unheader = 0xFF >> n;
                int ucs4 = unheader & headByte;
                for (int i = 1; i < n; i++) {
                    ucs4 = (ucs4 << 6) | (checkUtf8FollowerByte(src.next()) & 0x3F);
                }
                dst[start] = ucs4;
                break;
            }
        }
        return n;
    }

    public static IReader decodeToStream(byte[] utf8, int i, int j) {
        return new Decoder(IReader.wrap(utf8, i, j));
    }

    /**
     * a fast encoder without checking content
     */
    public static int encode(int[] src, int i, int j, byte[] dst, int start) {
        int k = start;
        while (i < j) {
            k += encode1unit(src[i++], dst, k);
        }
        return k - start;
    }

    public static int encode1unit(int ucs4, byte[] dst, int start) {
        int n = getNumUtf8Bytes(ucs4);
        if (n == 1) {
            dst[start++] = (byte) ucs4;
            return 1;
        }

        int k = start;
        for (int i = 0; i < n; i++) {
            int v = (ucs4 >> ((n - 1 - i) * 6)) & 0x3F;
            if (i == 0) {
                int header = (0xFF << (8 - n)) & 0xFF;
                dst[k++] = (byte) (v | header);
            } else {
                dst[k++] = (byte) (v | 0x80);
            }
        }
        return k - start;
    }

    public static int getNumUtf8Bytes(int ucs4) {
        if (ucs4 < 0) {
            return 7;
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
        throw new IllegalStateException();
    }

    public static int getNumUtf8BytesByUtf8HeadByte(int headByte) {
        assert isUtf8HeadByte(headByte);

        int n = 0;
        while (n < 8 && Cbit8.getBit((byte) headByte, n)) {
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
                return 0;
        }

    }

    /**
     * value being 0b10??????
     */
    public static boolean isUtf8FollowerByte(int value) {
        return value >= 0x80 && value <= 0xBF;
    }

    /**
     * value being ascii or in range of [0b11000000,0b11111110]
     */
    public static boolean isUtf8HeadByte(int value) {
        return (value >= 0 && value <= 0x7F) || (value >= 0xC0 && value <= 0xFE);
    }
}
