package libcliff.io.codec;

import libcliff.io.BytePipe;
import libcliff.io.BytePullable;
import libcliff.io.BytePushable;

public class UriByte implements BytePipe {

    public static int decodeByte(BytePullable pullable) {
        int aByte = pullable.pull();
        if (aByte == '%') {
            return Hexari.fromHexariText(pullable);
        } else {
            return aByte;
        }
    }

    public static int encodeByte(int aByte, BytePushable pushable) {
        int size = 0;
        size += pushable.push('%');
        size += Hexari.toHexariText(aByte & 0xFF, pushable);
        return size;
    }

    public static BytePushable pushable(final BytePushable pushable) {
        return new BytePushable() {

            private BytePushable pipe = pushable;

            @Override
            public int push(int aByte) {
                return encodeByte(aByte, this.pipe);
            }
        };
    }

    private BytePipe downstream = null;

    public UriByte(BytePipe downstream) {
        this.downstream = downstream;
    }

    @Override
    public int pull() {
        return decodeByte(downstream);
    }

    @Override
    public int push(int aByte) {
        return encodeByte(aByte, downstream);
    }
}
