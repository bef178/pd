package libcliff.io.codec;

import libcliff.io.BytePullStream;
import libcliff.io.BytePushStream;
import libcliff.io.Pullable;
import libcliff.io.Pushable;

/**
 * byte 0x65 => byte[] { '%', '6', '5' } under all conditions
 */
class UriByte implements BytePullStream, BytePushStream {

    public static int fromBytes(Pullable pullable) {
        int aByte = pullable.pull();
        if (aByte == '%') {
            return Hexari.fromHexariBytes(pullable);
        } else {
            return aByte;
        }
    }

    public static int toBytes(int aByte, Pushable pushable) {
        int size = 0;
        size += pushable.push('%');
        size += Hexari.toHexariBytes(aByte & 0xFF, pushable);
        return size;
    }

    private Pullable upstream;

    private Pushable downstream;

    @Override
    public UriByte join(Pullable upstream) {
        this.upstream = upstream;
        return this;
    }

    @Override
    public UriByte join(Pushable downstream) {
        this.downstream = downstream;
        return this;
    }

    @Override
    public int pull() {
        return fromBytes(upstream);
    }

    @Override
    public int push(int aByte) {
        return toBytes(aByte, downstream);
    }
}
