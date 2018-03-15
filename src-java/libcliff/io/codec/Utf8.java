package libcliff.io.codec;

import libcliff.io.Pullable;
import libcliff.io.PullablePipe;
import libcliff.io.Pushable;
import libcliff.io.PushablePipe;

/**
 * ch => utf8 byte[]
 */
public class Utf8 {

    public static PullablePipe asPuller() {

        return new PullablePipe() {

            private Pullable upstream = null;

            @Override
            public PullablePipe join(Pullable pullable) {
                upstream = pullable;
                return this;
            }

            @Override
            public int pull() {
                return fromUtf8Bytes(upstream);
            }
        };
    }

    public static PushablePipe asPusher() {

        return new PushablePipe() {

            private Pushable downstream = null;

            @Override
            public PushablePipe join(Pushable pushable) {
                downstream = pushable;
                return this;
            }

            @Override
            public int push(int ch) {
                return toUtf8Bytes(ch, downstream);
            }
        };
    }

    public static int fromUtf8Bytes(Pullable pullable) {
        int firstByte = CheckedByte.checkByteEx(pullable.pull());
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
    public static int toUtf8Bytes(int ch, Pushable pushable) {
        final int n = utf8Length(ch);
        if (n == 1) {
            // ASCII
            return pushable.push(ch);
        } else {
            int size = 0;
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
                size += pushable.push(c);
            }
            return size;
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

    private static int utf8LengthByFirstByte(int i) {
        CheckedByte.checkByte(i);
        if ((i & 0x80) == 0) {
            return 1;
        } else if ((i & 0x20) == 0) {
            return 2;
        } else if ((i & 0x10) == 0) {
            return 3;
        } else if ((i & 0x08) == 0) {
            return 4;
        } else if ((i & 0x04) == 0) {
            return 5;
        } else if ((i & 0x02) == 0) {
            return 6;
        }
        return -1;
    }

}
