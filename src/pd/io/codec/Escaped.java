package pd.io.codec;

import java.util.Arrays;

import pd.cprime.Ctype;
import pd.io.Pullable;
import pd.io.PullablePipe;
import pd.io.Pushable;
import pd.io.PushablePipe;

/**
 * ch => ch or "\\X" or "\\uXX"
 */
public class Escaped {

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
