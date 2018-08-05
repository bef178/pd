package libjava.io.codec;

import static libjava.io.Util.checkByte;

import libjava.io.Pullable;
import libjava.io.PullablePipe;
import libjava.io.Pushable;
import libjava.io.PushablePipe;

/**
 * ch => byte[4]
 */
public class Ucs4 {

    public static PullablePipe asPullablePipe() {

        return new PullablePipe() {

            private Pullable upstream = null;

            @Override
            public PullablePipe join(Pullable upstream) {
                this.upstream = upstream;
                return this;
            }

            @Override
            public int pull() {
                return fromUcs4Bytes(upstream);
            }
        };
    }

    public static PushablePipe asPushablePipe() {

        return new PushablePipe() {

            private Pushable downstream = null;

            @Override
            public PushablePipe join(Pushable downstream) {
                this.downstream = downstream;
                return this;
            }

            @Override
            public void push(int ch) {
                toUcs4Bytes(ch, downstream);
            }
        };
    }

    public static int fromUcs4Bytes(Pullable upstream) {
        int ch = 0;
        for (int i = 0; i < 4; ++i) {
            ch = (ch << 8) | checkByte(upstream.pull());
        }
        return ch;
    }

    public static void toUcs4Bytes(int ch, Pushable downstream) {
        for (int i = 0; i < 4; ++i) {
            int c = ch;
            int j = 4 - 1 - i;
            while (j-- > 0) {
                c >>>= 8;
            }
            downstream.push(c & 0xFF);
        }
    }

}
