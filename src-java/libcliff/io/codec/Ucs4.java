package libcliff.io.codec;

import libcliff.io.BytePipe;
import libcliff.io.BytePullable;
import libcliff.io.BytePushable;
import libcliff.io.Pullable;
import libcliff.io.Pushable;

public class Ucs4 implements Pullable, Pushable {

    public static int fromUcs4Bytes(BytePullable pullable) {
        int ch = 0;
        for (int i = 0; i < 4; ++i) {
            ch = (ch << 8) | (pullable.pull() & 0xFF);
        }
        return ch;
    }

    public static int toUcs4Bytes(int ch, BytePushable pushable) {
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