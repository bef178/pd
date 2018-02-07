package libcliff.io.codec;

import libcliff.io.BytePipe;
import libcliff.io.Pushable;
import libcliff.io.Pullable;

/**
 * byte 0x65 => byte[] { '%', '6', '5' } under all conditions
 */
class UriByte implements BytePipe {

    public static int fromBytes(Pullable pullable) {
        int aByte = pullable.pull();
        if (aByte == '%') {
            return Hexari.fromHexariBytes(pullable);
        } else {
            return aByte;
        }
    }

    public static Pullable pullable(final Pullable pullable) {

        return new Pullable() {

            private Pullable pipe = pullable;

            @Override
            public int pull() {
                return fromBytes(pipe);
            }
        };
    }

    public static Pushable pushable(final Pushable pushable) {

        return new Pushable() {

            private Pushable pipe = pushable;

            @Override
            public int push(int aByte) {
                return toBytes(aByte, this.pipe);
            }
        };
    }

    public static int toBytes(int aByte, Pushable pushable) {
        int size = 0;
        size += pushable.push('%');
        size += Hexari.toHexariBytes(aByte & 0xFF, pushable);
        return size;
    }

    private BytePipe downstream = null;

    public UriByte(BytePipe downstream) {
        this.downstream = downstream;
    }

    @Override
    public int pull() {
        return fromBytes(downstream);
    }

    @Override
    public int push(int aByte) {
        return toBytes(aByte, downstream);
    }
}
