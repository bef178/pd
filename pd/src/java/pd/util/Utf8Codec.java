package pd.util;

import pd.fenc.ParsingException;

/**
 * https://en.wikipedia.org/wiki/UTF-8<br/>
 * <br/>
 * Utf8 tuple is able to represent 36 effective bits at most, costing 7 bytes.<br/>
 * If limited to 6 bytes, it can represent 31 bits.<br/>
 * Non-negative Int32, uses 31 bits.<br/>
 * Unicode, stopping at U+10FFFF, uses 21 bits at most.<br/>
 *
 * To keep it simple, Utf8Codec transforms 31 bits of informative representation between non-negative Int32 and max-6-byte utf8 tuple.<br/>
 */
public class Utf8Codec {

    /**
     * consume 1 int32 and produce 1-6 byte(s)<br/>
     * return number of produced bytes
     */
    public int encode1unit(int ch, byte[] dst, int start) {
        if (ch < 0) {
            throw new IllegalArgumentException();
        }

        int n = numBytesByInt32(ch);
        if (n == 1) {
            dst[start] = (byte) ch;
        } else {
            for (int i = 0; i < n; i++) {
                int v = (ch >> ((n - 1 - i) * 6)) & 0x3F;
                if (i == 0) {
                    int headBytePrefix = (0xFF << (8 - n)) & 0xFF;
                    dst[start++] = (byte) (v | headBytePrefix);
                } else {
                    dst[start++] = (byte) (v | 0x80);
                }
            }
        }
        return n;
    }

    /**
     * consume 1-6 byte and produce 1 int32<br/>
     * return number of consumed bytes
     */
    public int decode1unit(byte[] a, int i, int[] dst, int start) {
        byte aByte = a[i++];
        int n = numBytesByHeadByte(aByte);
        switch (n) {
            case 1:
                dst[start] = aByte;
                return 1;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6: {
                int mask = 0xFF >> n;
                int ch = mask & aByte;
                for (int j = 1; j < n; j++) {
                    aByte = a[i++];
                    if (!isBodyByte(aByte)) {
                        throw new IllegalArgumentException();
                    }
                    ch = (ch << 6) | (aByte & 0x3F);
                }
                dst[start] = ch;
                return n;
            }
            default:
                break;
        }
        throw new ParsingException();
    }

    public int numBytesByInt32(int ch) {
        if (ch < 0) {
            throw new IllegalArgumentException();
        }

        if (ch <= 0x7F) {
            return 1;
        } else if (ch <= 0x7FF) {
            return 2;
        } else if (ch <= 0xFFFF) {
            // LE 16-bit
            return 3;
        } else if (ch <= 0x1FFFFF) {
            // LE 21-bit
            return 4;
        } else if (ch <= 0x3FFFFFF) {
            // LE 26-bit
            return 5;
        } else {
            // LE 31-bit
            return 6;
        }
    }

    public int numBytesByHeadByte(byte headByte) {
        int n = 0;
        while (n < 8 && Int8ArrayExtension.getBit(headByte, n)) {
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
                return -1;
        }
    }

    /**
     * value is ascii or in [0b11000000,0b11111110]
     */
    public boolean isHeadByte(byte aByte) {
        int value = aByte & 0xFF;
        return value <= 0x7F || value >= 0xC0 && value <= 0xFE;
    }

    /**
     * value in [0b10000000,0b10111111]
     */
    public boolean isBodyByte(byte aByte) {
        int value = aByte & 0xFF;
        return value >= 0x80 && value <= 0xBF;
    }
}
