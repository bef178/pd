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
public class Escaped extends DualPipe {

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

    private Pullable upUstream = null;

    private Pushable downUstream = null;

    @Override
    public Escaped join(Pullable upstream) {
        super.join(upstream);
        this.upUstream = PullablePipe.join(new Utf8(), new Hexari(),
                upstream);
        return this;
    }

    @Override
    public Escaped join(Pushable downstream) {
        super.join(downstream);
        this.downUstream = PushablePipe.join(new Utf8(), new Hexari(),
                downstream);
        return this;
    }

    @Override
    public int pull() {
        int ch = super.pull();
        if (ch == '\\') {
            ch = super.pull();
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

    @Override
    public int push(int ch) {
        if (Ctype.isGraph(ch)) {
            if (ENCODE_MAP[ch] >= 0) {
                super.push('\\');
                super.push(ENCODE_MAP[ch]);
                return 2;
            } else {
                super.push(ch);
                return 1;
            }
        }
        super.push('\\');
        super.push('u');
        return 2 + downUstream.push(ch);
    }
}
