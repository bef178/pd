package pd.io.codec;

import static pd.io.Util.checkByte;
import static pd.io.Util.checkByteEx;

import pd.io.ParsingException;
import pd.io.Pullable;
import pd.io.PullablePipe;
import pd.io.Pushable;
import pd.io.PushablePipe;

/**
 * ch => utf8 byte[]
 */
@Deprecated
public class Utf8 {

    public static PullablePipe asPullablePipe() {

        return new PullablePipe() {

            private Pullable upstream = null;

            @Override
            public <T extends Pullable> T join(T upstream) {
                this.upstream = upstream;
                return upstream;
            }

            @Override
            public int pull() {
                return fromUtf8Bytes(upstream);
            }
        };
    }

    public static PushablePipe asPushablePipe() {

        return new PushablePipe() {

            private Pushable downstream = null;

            @Override
            public <T extends Pushable> T join(T downstream) {
                this.downstream = downstream;
                return downstream;
            }

            @Override
            public void push(int ch) {
                toUtf8Bytes(ch, downstream);
            }
        };
    }

    public static int fromUtf8Bytes(Pullable pullable) {
        int firstByte = checkByteEx(pullable.pull());
        int n = utf8LengthByFirstByte(firstByte);
        if (n < 0) {
            throw new ParsingException();
        } else if (n == 1) {
            return firstByte;
        } else {
            int ch = firstByte & ~(0xFF >>> (8 - n) << (8 - n));
            for (int i = 1; i < n; ++i) {
                int b = pullable.pull();
                ch = b & 0x3F | (ch << 6);
            }
            return ch;
        }
    }

    /**
     * a fast encoder without checking content<br/>
     * <br/>
     * returns the size of utf8 bytes
     */
    public static void toUtf8Bytes(int ch, Pushable pushable) {
        final int n = utf8Length(ch);
        if (n == 1) {
            // ASCII
            pushable.push(ch);
        } else {
            for (int i = 0; i < n; ++i) {
                int c = ch;
                int j = n - 1 - i;
                while (j-- > 0) {
                    c >>>= 6;
                }
                if (i == 0) {
                    c = c | (0xFF >> (8 - n) << (8 - n));
                } else {
                    c = c & 0x3F | 0x80;
                }
                pushable.push(c);
            }
        }
    }

    // UTF-8 uses 6b * 5 + 1b = 31b, the highest bit is not used
    private static int utf8Length(final int ch) {
        if (ch >= 0) {
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
            } else if (ch <= 0x7FFFFFFF) {
                // LE 31-bit
                return 6;
            }
        }
        return -1;
    }

    private static int utf8LengthByFirstByte(int aByte) {
        checkByte(aByte);
        if ((aByte & 0x80) == 0) {
            return 1;
        } else if ((aByte & 0x20) == 0) {
            return 2;
        } else if ((aByte & 0x10) == 0) {
            return 3;
        } else if ((aByte & 0x08) == 0) {
            return 4;
        } else if ((aByte & 0x04) == 0) {
            return 5;
        } else if ((aByte & 0x02) == 0) {
            return 6;
        }
        return -1;
    }

}
