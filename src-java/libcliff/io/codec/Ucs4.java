package libcliff.io.codec;

import libcliff.io.Pullable;
import libcliff.io.PullablePipe;
import libcliff.io.Pushable;
import libcliff.io.PushablePipe;

/**
 * ch => byte[4]
 */
public class Ucs4 {

    public static PullablePipe asPuller() {

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

    public static PushablePipe asPusher() {

        return new PushablePipe() {

            private Pushable downstream = null;

            @Override
            public PushablePipe join(Pushable downstream) {
                this.downstream = downstream;
                return this;
            }

            @Override
            public int push(int ch) {
                return toUcs4Bytes(ch, downstream);
            }
        };
    }

    public static int fromUcs4Bytes(Pullable upstream) {
        int ch = 0;
        for (int i = 0; i < 4; ++i) {
            ch = (ch << 8) | (CheckedByte.checkByte(upstream.pull()));
        }
        return ch;
    }

    public static int toUcs4Bytes(int ch, Pushable downstream) {
        int size = 0;
        for (int i = 0; i < 4; ++i) {
            int c = ch;
            int j = 4 - 1 - i;
            while (j-- > 0) {
                c >>>= 8;
            }
            size += downstream.push(c & 0xFF);
        }
        return size;
    }

}
