package pd.io.codec;

import java.util.Arrays;

import pd.ctype.Ctype;
import pd.encoding.Hexdig;
import pd.io.Pullable;
import pd.io.PullablePipe;
import pd.io.Pushable;
import pd.io.PushablePipe;

/**
 * translator: byte => byte[]<br/>
 * ascii in [0,7F]<br/>
 * keep original value if not ascii<br/>
 */
public class Escaped {

    private static final int[] ENCODE_MAP;

    private static final int[] DECODE_MAP;

    static {
        DECODE_MAP = new int[128];
        Arrays.fill(DECODE_MAP, -1);
        DECODE_MAP['\\'] = '\\';
        DECODE_MAP['a'] = 0x07;
        DECODE_MAP['b'] = '\b'; // 0x08
        DECODE_MAP['t'] = '\t'; // 0x09
        DECODE_MAP['n'] = '\n'; // 0x0A
        DECODE_MAP['v'] = 0x0B;
        DECODE_MAP['f'] = '\f'; // 0x0C
        DECODE_MAP['r'] = '\r'; // 0x0D
        DECODE_MAP['\''] = '\''; // 0x0D
        DECODE_MAP['\"'] = '\"'; // 0x0D
        DECODE_MAP['0'] = '\0'; // 0x0D

        ENCODE_MAP = new int[DECODE_MAP.length];
        Arrays.fill(ENCODE_MAP, -1);
        for (int ch = 0; ch < DECODE_MAP.length; ++ch) {
            if (DECODE_MAP[ch] >= 0) {
                ENCODE_MAP[DECODE_MAP[ch]] = ch;
            }
        }
    }

    public static int encode(byte value, byte[] dst, final int start) {
        int d = value & 0xFF;
        int i = start;
        if (d > 0x7F) {
            dst[i++] = (byte) d;
        } else if (ENCODE_MAP[d] > 0) {
            dst[i++] = '\\';
            dst[i++] = (byte) ENCODE_MAP[d];
        } else if (Ctype.isVisible(d)) {
            dst[i++] = (byte) d;
        } else {
            dst[i++] = '\\';
            dst[i++] = 'x';
            dst[i++] = (byte) Hexdig.encode(d >>> 4);
            dst[i++] = (byte) Hexdig.encode(d & 0x0F);
        }
        return i - start;
    }

    /**
     * translator: byte[] => byte<br/>
     * keep unrecognized escape sequence<br/>
     * --return number of consumed bytes in src<br/>
     */
    public static void decode(byte[] src, int i, int j, byte[] dst, int start) {
        int ch = src[i++] & 0xFF;
        if (ch == '\\') {
            ch = src[i++] & 0xFF;
            if (ch >= DECODE_MAP.length) {
                dst[start++] = (byte) ch;
            } else {
                if (DECODE_MAP[ch] >= 0) {
                    dst[start++] = (byte) DECODE_MAP[ch];
                } else if (ch == 'x') {
                    int x = Hexdig.decode(src[i++]);
                    x = x << 4 | Hexdig.decode(src[i++]);
                    dst[start++] = (byte) x;
                } else {
                    dst[start++] = '\\';
                    dst[start++] = (byte) ch;
                }
            }
        } else {
            dst[start++] = (byte) ch;
        }
    }

    public static PullablePipe asPullablePipe() {

        return new PullablePipe() {

            private Pullable upstream;

            private PullablePipe upUstream;

            @Override
            public <T extends Pullable> T join(T upstream) {
                this.upstream = upstream;
                this.upUstream = Utf8.asPullablePipe();
                this.upUstream.join(Hexari.asPullablePipe()).join(upstream);
                return upstream;
            }

            @Override
            public int pull() {
                int ch = upstream.pull();
                if (ch != '\\') {
                    return ch;
                }

                ch = upstream.pull();
                if (Ctype.isVisible(ch)) {
                    if (DECODE_MAP[ch] >= 0) {
                        return DECODE_MAP[ch];
                    }
                    if (ch == 'u') {
                        return upUstream.pull();
                    }
                }
                // unrecognized escaped sequence
                return ch;
            }
        };
    }

    public static PushablePipe asPushablePipe() {

        return new PushablePipe() {

            private Pushable downstream;

            private PushablePipe downUstream;

            @Override
            public <T extends Pushable> T join(T downstream) {
                this.downstream = downstream;
                this.downUstream = Utf8.asPushablePipe();
                this.downUstream.join(Hexari.asPushablePipe()).join(downstream);
                return downstream;
            }

            @Override
            public void push(int ch) {
                if (Ctype.isVisible(ch)) {
                    if (ENCODE_MAP[ch] >= 0) {
                        downstream.push('\\');
                        downstream.push(ENCODE_MAP[ch]);
                    } else {
                        downstream.push(ch);
                    }
                    return;
                }

                downstream.push('\\');
                downstream.push('u');
                downUstream.push(ch);
            }
        };
    }
}
