package libcliff.io.codec;

import java.util.Arrays;

import libcliff.io.Pullable;
import libcliff.io.PullablePipe;
import libcliff.io.Pushable;
import libcliff.io.PushablePipe;
import libcliff.primitive.Ctype;

/**
 * ch => ch
 */
public class Escaped {

    public static PullablePipe asPuller() {

        return new PullablePipe() {

            private Pullable upstream;

            private Pullable upUstream;

            @Override
            public PullablePipe join(Pullable upstream) {
                this.upstream = upstream;
                this.upUstream = PullablePipe.join(Utf8.asPuller(),
                        Hexari.asPuller(), upstream);
                return this;
            }

            @Override
            public int pull() {
                int ch = upstream.pull();
                if (ch == '\\') {
                    ch = upstream.pull();
                    if (Ctype.isGraph(ch)) {
                        if (DECODE_MAP[ch] >= 0) {
                            return DECODE_MAP[ch];
                        }
                        if (ch == 'u') {
                            return upUstream.pull();
                        }
                    }
                    // unrecognized escaped sequence
                    return ch;
                } else {
                    return ch;
                }
            }
        };
    }

    public static PushablePipe asPusher() {

        return new PushablePipe() {

            private Pushable downstream;

            private Pushable downUstream;

            @Override
            public PushablePipe join(Pushable downstream) {
                this.downstream = downstream;
                this.downUstream = PushablePipe.join(Utf8.asPusher(),
                        Hexari.asPusher(), downstream);
                return this;
            }

            @Override
            public int push(int ch) {
                if (Ctype.isGraph(ch)) {
                    if (ENCODE_MAP[ch] >= 0) {
                        downstream.push('\\');
                        downstream.push(ENCODE_MAP[ch]);
                        return 2;
                    } else {
                        downstream.push(ch);
                        return 1;
                    }
                }
                downstream.push('\\');
                downstream.push('u');
                return 2 + downUstream.push(ch);
            }
        };
    }

    private static final int[] ENCODE_MAP;

    private static final int[] DECODE_MAP;

    static {
        DECODE_MAP = new int[128];
        Arrays.fill(DECODE_MAP, -1);
        DECODE_MAP['\\'] = '\\';
        DECODE_MAP['a'] = 0x07;
        DECODE_MAP['t'] = '\t'; // 0x09
        DECODE_MAP['n'] = '\n'; // 0x0A
        DECODE_MAP['v'] = 0x0B;
        DECODE_MAP['f'] = '\f'; // 0x0C
        DECODE_MAP['r'] = '\r'; // 0x0D
        DECODE_MAP['b'] = '\b';

        ENCODE_MAP = new int[DECODE_MAP.length];
        Arrays.fill(ENCODE_MAP, -1);
        for (int ch = 0; ch < DECODE_MAP.length; ++ch) {
            if (DECODE_MAP[ch] >= 0) {
                ENCODE_MAP[DECODE_MAP[ch]] = ch;
            }
        }
    }

}
