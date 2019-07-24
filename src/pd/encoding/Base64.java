package pd.encoding;

import java.util.Arrays;

/**
 * byte[3] => byte[4]
 */
public class Base64 {

    private static final int[] ENCODE_MAP;
    private static final int[] DECODE_MAP;

    static {
        ENCODE_MAP = new int[] {
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
                'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
                'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
                'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
                'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', '+', '/'
        };

        DECODE_MAP = new int[128];
        Arrays.fill(DECODE_MAP, -1);
        for (int i = 0; i < ENCODE_MAP.length; ++i) {
            DECODE_MAP[ENCODE_MAP[i]] = i;
        }
    }

    private static int decode(byte i, byte j, byte k, byte l, byte[] decoded, int start) {
        assert decoded != null;
        assert start >= 0 && start + 3 <= decoded.length;
        i = (byte) ((i >= 0) ? DECODE_MAP[i] : -1);
        j = (byte) ((j >= 0) ? DECODE_MAP[j] : -1);
        k = (byte) ((k >= 0) ? DECODE_MAP[k] : -1); // '=' => -1
        l = (byte) ((l >= 0) ? DECODE_MAP[l] : -1);
        decoded[start++] = (byte) ((i << 2) | (j >> 4));
        if (k < 0) {
            return 1;
        }
        decoded[start++] = (byte) ((j << 4) & 0xFF | (k >> 2));
        if (l < 0) {
            return 2;
        }
        decoded[start++] = (byte) ((k << 6) & 0xFF | l);
        return 3;
    }

    /**
     * consume byte[4] and produce byte[3]<br/>
     * return size of written bytes
     */
    public static int decode(byte[] base64, int i, int j, byte[] decoded, int start) {
        assert base64 != null;
        assert i >= 0 && i < j && j <= base64.length && (j - i) % 4 == 0;

        int n = 0;
        while (i < j) {
            n += decode(base64[i], base64[i + 1], base64[i + 2], base64[i + 3], decoded,
                    start + n);
            i += 4;
        }
        return n;
    }

    private static void encode(byte i, byte j, byte k, byte[] encoded, int start) {
        encoded[start++] = (byte) ENCODE_MAP[(i & 0xFF) >>> 2];
        encoded[start++] = (byte) ENCODE_MAP[((i & 0x03) << 4) | ((j & 0xFF) >>> 4)];
        encoded[start++] = (byte) ENCODE_MAP[((j & 0x0F) << 2) | ((k & 0xFF) >>> 6)];
        encoded[start++] = (byte) ENCODE_MAP[k & 0x3F];
    }

    private static void encode(byte i, byte j, byte[] encoded, int start) {
        encoded[start++] = (byte) ENCODE_MAP[(i & 0xFF) >>> 2];
        encoded[start++] = (byte) ENCODE_MAP[((i & 0x03) << 4) | ((j & 0xFF) >>> 4)];
        encoded[start++] = (byte) ENCODE_MAP[((j & 0x0F) << 2)];
        encoded[start++] = '=';
    }

    private static void encode(byte i, byte[] encoded, int start) {
        encoded[start++] = (byte) ENCODE_MAP[(i & 0xFF) >>> 2];
        encoded[start++] = (byte) ENCODE_MAP[(i & 0x03) << 4];
        encoded[start++] = '=';
        encoded[start++] = '=';
    }

    /**
     * consume byte[3] and produce byte[4]<br/>
     * return size of written bytes
     */
    public static int encode(byte[] src, int i, int j, byte[] encoded, int start) {
        assert src != null;
        assert i >= 0 && i < j && j <= src.length;
        assert (j - i + 2) / 3 * 4 <= encoded.length - start;

        int k = 0;
        while (i + 3 <= j) {
            encode(src[i], src[i + 1], src[i + 2], encoded, k);
            i += 3;
            k += 4;
        }

        switch (j - i) {
            case 2: {
                encode(src[i], src[i + 1], encoded, k);
                k += 4;
                break;
            }
            case 1: {
                encode(src[i], encoded, k);
                k += 4;
                break;
            }
            default:
                break;
        }
        return k - start;
    }
}
