package libcliff.io.codec;

import libcliff.io.BytePipe;
import libcliff.io.Pipe;
import libcliff.io.Pullable;
import libcliff.io.Pushable;

/**
 * ch  => byte[4]
 */
public class Ucs4 implements Pipe {

    public static int fromUcs4Bytes(Pullable pullable) {
        int ch = 0;
        for (int i = 0; i < 4; ++i) {
            ch = (ch << 8) | (pullable.pull() & 0xFF);
        }
        return ch;
    }

    public static int toUcs4Bytes(int ch, Pushable pushable) {
        int size = 0;
        for (int i = 0; i < 4; ++i) {
            int c = ch;
            int j = 4 - 1 - i;
            while (j-- > 0) {
                c >>>= 8;
            }
            size += pushable.push(c & 0xFF);
        }
        return size;
    }

    private BytePipe downstream = null;

    public Ucs4(BytePipe downstream) {
        this.downstream = downstream;
    }

    @Override
    public int pull() {
        return fromUcs4Bytes(downstream);
    }

    @Override
    public int push(int ch) {
        return toUcs4Bytes(ch, downstream);
    }
}
