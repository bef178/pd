package libcliff.io.codec;

import java.util.Arrays;

import libcliff.io.BytePipe;
import libcliff.io.Pullable;
import libcliff.io.Pushable;

public class Base64 implements BytePipe {

    private class Puller {

        private int[] base64 = null;

        private int[] parsed = new int[3];

        private int pIndex = 3;

        private boolean ends = false;

        private void decodeBase64Bytes(int[] base64, int[] dst) {
            dst[0] = (base64[0] << 2) | (base64[1] >> 4);
            dst[1] = (base64[1] << 4) | (base64[2] >> 2);
            dst[2] = (base64[2] << 6) | base64[3];
        }

        public int pull(Pullable pullable) {
            if (ends) {
                return -1;
            }
            if (base64 == null) {
                // the first time
                pullBase64Bytes(pullable, base64);
            }
            if (pIndex == 3) {
                decodeBase64Bytes(base64, parsed);
                pullBase64Bytes(pullable, base64);
                if (base64[0] == '=') {
                    parsed[0] = -1;
                    if (base64[1] == '=') {
                        parsed[1] = -1;
                    }
                }
                pIndex = 0;
            }
            int i = parsed[pIndex++];
            if (i == -1) {
                ends = true;
            }
            return i;
        }

        private int pullBase64Bytes(Pullable pullable, int[] base64) {
            assert base64.length >= 4;
            for (int i = 0; i < 4; ++i) {
                int j = pullable.pull();
                if (j == -1) {
                    return i;
                }
                base64[i] = DECODE_MAP[j & 0xFF];
            }
            return 4;
        }
    }

    private class Pusher {

        private int[] parsed = new int[3];

        private int pIndex = 3;

        private boolean ends = false;

        public int push(int i, Pushable pushable) {
            if (ends) {
                return -1;
            }
            parsed[pIndex++] = i & 0xFF;
            if (pIndex == 3) {
                pIndex = 0;
                return toBase64Bytes(parsed, 0, 3, pushable);
            }
            return 0;
        }

        public int pushEnd(Pushable pushable) {
            ends = true;
            return toBase64Bytes(parsed, 0, pIndex, pushable);
        }

        private int toBase64Bytes(int byte0, int byte1, int byte2,
                Pushable pushable) {
            int size = 0;
            size += pushable.push(
                    ENCODE_MAP[(byte0 & 0xFF) >>> 2]);
            size += pushable.push(
                    ENCODE_MAP[((byte0 & 0x03) << 4) | ((byte1 & 0xFF) >>> 4)]);
            size += pushable.push(
                    ENCODE_MAP[((byte1 & 0x0F) << 2) | ((byte2 & 0xFF) >>> 6)]);
            size += pushable.push(
                    ENCODE_MAP[byte2 & 0x3F]);
            return size;
        }

        private int toBase64Bytes(int[] a, int i, int j,
                Pushable pushable) {
            switch (j - i) {
                case 0:
                    return 0;
                case 1: {
                    int size = 0;
                    size += toBase64Bytes(a[i], 0, 0, pushable);
                    size += pushable.push('=');
                    size += pushable.push('=');
                    return size;
                }
                case 2: {
                    int size = 0;
                    size += toBase64Bytes(a[i], a[i + 1], 0, pushable);
                    size += pushable.push('=');
                    return size;
                }
                case 3:
                    return toBase64Bytes(a[i], a[i + 1], a[i + 2], pushable);
                default:
                    throw new ParsingException();
            }
        }
    }

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

    private BytePipe downstream = null;

    private Puller puller = null;

    private Pusher pusher = null;

    public Base64(BytePipe downstream) {
        this.downstream = downstream;
    }

    @Override
    public int pull() {
        if (puller == null) {
            puller = new Puller();
        }
        return puller.pull(downstream);
    }

    @Override
    public int push(int i) {
        if (pusher == null) {
            pusher = new Pusher();
        }
        return pusher.push(i, downstream);
    }

    public int pushEnd() {
        if (pusher == null) {
            pusher = new Pusher();
        }
        return pusher.pushEnd(downstream);
    }
}
