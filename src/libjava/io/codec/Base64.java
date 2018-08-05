package libjava.io.codec;

import java.util.Arrays;

import libjava.io.ParsingException;
import libjava.io.Pullable;
import libjava.io.PullablePipe;
import libjava.io.Pushable;
import libjava.io.PushablePipe;

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

        DECODE_MAP = new int[127];
        Arrays.fill(DECODE_MAP, -1);
        for (int i = 0; i < ENCODE_MAP.length; ++i) {
            DECODE_MAP[ENCODE_MAP[i]] = i;
        }
    }

    public static final int P_FLUSH = -1;

    public static PullablePipe asPullablePipe() {

        return new PullablePipe() {

            private int[] src = new int[3];

            private int srcIndex = 3;

            private boolean eof = false;

            /**
             * accept byte pullable
             */
            private Pullable upstream = null;

            @Override
            public PullablePipe join(Pullable upstream) {
                this.upstream = upstream;
                return this;
            }

            @Override
            public int pull() {
                if (eof) {
                    return -1;
                }
                if (srcIndex == 3) {
                    fromBase64Bytes(src, upstream);
                    srcIndex = 0;
                }
                int ch = src[srcIndex++];
                if (ch == -1) {
                    eof = true;
                }
                return ch;
            }
        };
    }

    public static PushablePipe asPushablePipe() {

        return new PushablePipe() {

            private int[] src = new int[3];

            private int srcIndex = 0;

            private Pushable downstream = null;

            private boolean eof = false;

            @Override
            public PushablePipe join(Pushable downstream) {
                this.downstream = downstream;
                return this;
            }

            @Override
            public void push(final int ch) {
                CheckedByte.checkByteEx(ch);
                if (eof) {
                    return;
                }
                if (ch == P_FLUSH) {
                    eof = true;
                    int j = srcIndex;
                    srcIndex = 0;
                    toBase64Bytes(src, 0, j, downstream);
                    return;
                }
                src[srcIndex++] = ch;
                if (srcIndex == 3) {
                    srcIndex = 0;
                    toBase64Bytes(src, 0, 3, downstream);
                    return;
                }
            }
        };
    }

    private static void fromBase64Bytes(int[] a, Pullable pullable) {
        int i = CheckedByte.checkByteEx(pullable.pull());
        i = i == -1 ? -1 : DECODE_MAP[i];
        int j = CheckedByte.checkByteEx(pullable.pull());
        j = j == -1 ? -1 : DECODE_MAP[j];
        int k = CheckedByte.checkByteEx(pullable.pull());
        k = k == -1 ? -1 : DECODE_MAP[k]; // '=' => -1
        int l = CheckedByte.checkByteEx(pullable.pull());
        l = l == -1 ? -1 : DECODE_MAP[l];
        a[0] = (i << 2) | (j >> 4);
        a[1] = k != -1 ? (j << 4) & 0xFF | (k >> 2) : -1;
        a[2] = l != -1 ? (k << 6) & 0xFF | l : -1;
    }

    private static int toBase64Bytes(int[] a, int i, int j,
            Pushable pushable) {
        switch (j - i) {
            case 0:
                return 0;
            case 1: {
                pushable.push(ENCODE_MAP[(a[i] & 0xFF) >>> 2]);
                pushable.push(ENCODE_MAP[(a[i] & 0x03) << 4]);
                pushable.push('=');
                pushable.push('=');
                break;
            }
            case 2: {
                pushable.push(ENCODE_MAP[(a[i] & 0xFF) >>> 2]);
                pushable.push(ENCODE_MAP[((a[i] & 0x03) << 4)
                        | ((a[i + 1] & 0xFF) >>> 4)]);
                pushable.push(ENCODE_MAP[((a[i + 1] & 0x0F) << 2)]);
                pushable.push('=');
                break;
            }
            case 3:
                pushable.push(ENCODE_MAP[(a[i] & 0xFF) >>> 2]);
                pushable.push(ENCODE_MAP[((a[i] & 0x03) << 4)
                        | ((a[i + 1] & 0xFF) >>> 4)]);
                pushable.push(ENCODE_MAP[((a[i + 1] & 0x0F) << 2)
                        | ((a[i + 2] & 0xFF) >>> 6)]);
                pushable.push(ENCODE_MAP[a[i + 2] & 0x3F]);
                break;
            default:
                throw new ParsingException();
        }
        return 4;
    }
}
