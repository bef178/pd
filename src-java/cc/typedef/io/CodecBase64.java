package cc.typedef.io;

import java.io.IOException;
import java.util.Arrays;

class CodecBase64 {

    private static final char[] ENCODE_MAP;
    private static final char[] DECODE_MAP;

    static {
        ENCODE_MAP = new char[] {
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
                'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
                'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
                'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
                'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', '+', '/'
        };

        DECODE_MAP = new char[127];
        Arrays.fill(DECODE_MAP, (char) -1);
        for (char i = 0; i < ENCODE_MAP.length; ++i) {
            DECODE_MAP[ENCODE_MAP[i]] = i;
        }
    }

    private static int decodeTuple(int b0, int b1, int b2, int b3,
            byte[] a, int i) {
        assert a != null && i >= 0 && i + 3 <= a.length;
        b0 = (byte) DECODE_MAP[b0 & 0xFF];
        b1 = (byte) DECODE_MAP[b1 & 0xFF];
        b2 = (byte) DECODE_MAP[b2 & 0xFF];
        b3 = (byte) DECODE_MAP[b3 & 0xFF];
        assert b0 != -1;
        assert b1 != -1;
        assert b2 != -1;
        assert b3 != -1;

        a[i] = (byte) ((b0 << 2) | (b1 >> 4));
        a[i + 1] = (byte) ((b1 << 4) | (b2 >> 2));
        a[i + 2] = (byte) ((b2 << 6) | b3);
        return 3;
    }

    private static InstallmentByteBuffer encode(byte[] a, int i, int j) {
        assert a != null;
        assert i >= 0 && i < a.length;
        assert j >= i && j < a.length;

        InstallmentByteBuffer ibb = new InstallmentByteBuffer();
        while (j - i >= 3) {
            ibb.append(encodeTuple(a, i, i + 3));
            i += 3;
        }
        if (j > i) {
            ibb.append(encodeTuple(a, i, j));
        }
        return ibb;
    }

    public static byte[] encode(byte[] a, int bytesPerLine,
            int firstOffset, byte[] prefix, byte[] suffix)
            throws IOException {
        return encode(a, 0, a.length, bytesPerLine, firstOffset,
                prefix, suffix);
    }

    public static InstallmentByteBuffer encode(byte[] a, int bytesPerLine,
            int firstOffset, byte[] prefix, byte[] suffix,
            InstallmentByteBuffer o)
            throws IOException {
        return encode(a, 0, a.length, bytesPerLine, firstOffset,
                prefix, suffix, o);
    }

    public static byte[] encode(byte[] a, int i, int j, int bytesPerLine,
            int firstOffset, byte[] prefix, byte[] suffix)
            throws IOException {
        InstallmentByteBuffer o = new InstallmentByteBuffer();
        encode(a, i, j, bytesPerLine, firstOffset, prefix, suffix, o);
        return o.toByteArray();
    }

    /**
     * it is user who should put the prefix/suffix before/after invoke this
     * method if necessary
     */
    public static InstallmentByteBuffer encode(byte[] a, int i, int j,
            int bytesPerLine, int firstOffset,
            byte[] prefix, byte[] suffix, InstallmentByteBuffer o)
            throws IOException {
        assert a != null;
        assert i >= 0 && i < a.length;
        assert j >= i && j <= a.length;
        assert bytesPerLine > 0;
        assert firstOffset >= 0;

        if (firstOffset > 0) {
            assert bytesPerLine >= prefix.length + suffix.length
                    + firstOffset;
        } else {
            assert bytesPerLine > prefix.length + suffix.length;
        }

        InstallmentByteBuffer.Reader ibbr = encode(a, i, j).reader();

        ibbr.rewind();
        int m = ibbr.size();

        // the first line
        int rest = bytesPerLine - firstOffset - suffix.length;
        if (rest > m) {
            rest = m;
        }
        for (int k = 0; k < rest; ++i) {
            o.append(ibbr.next());
        }
        m -= rest;
        if (m > 0) {
            o.append(suffix);
        }

        // non-first lines
        rest = bytesPerLine - prefix.length - suffix.length;
        while (m >= rest) {
            o.append(prefix);
            while (rest-- > 0) {
                o.append(ibbr.next());
            }
            o.append(suffix);
            m -= rest;
        }

        if (m > 0) {
            rest = m;
            o.append(prefix);
            while (rest-- > 0) {
                o.append(ibbr.next());
            }
        }
        return o;
    }

    private static byte[] encodeTuple(byte[] a, int i, int j) {
        switch (j - i) {
            case 3: {
                byte[] r = new byte[4];
                encodeTuple(a[i], a[i + 1], a[i + 2], r, i);
                return r;
            }
            case 2: {
                byte[] r = new byte[5];
                encodeTuple(a[i], a[i + 1], 0, r, i);
                r[4] = '=';
                return r;
            }
            case 1: {
                byte[] r = new byte[6];
                encodeTuple(a[i], (byte) 0, 0, r, i);
                r[4] = '=';
                r[5] = '=';
                return r;
            }
            default:
                break;
        }
        throw new ParsingException();
    }

    private static int encodeTuple(int b0, int b1, int b2,
            byte[] a, int i) {
        assert a != null && i >= 0 && i + 4 <= a.length;
        a[i] = (byte) ENCODE_MAP[(b0 & 0xFF) >>> 2];
        a[i + 1] = (byte) ENCODE_MAP[((0x03 & b0) << 4) | ((b1 & 0xFF) >>> 4)];
        a[i + 2] = (byte) ENCODE_MAP[((0x0F & b1) << 2) | ((b2 & 0xFF) >>> 6)];
        a[i + 3] = (byte) ENCODE_MAP[0x3F & b2];
        return 4;
    }
}
