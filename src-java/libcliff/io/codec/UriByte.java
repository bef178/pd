package libcliff.io.codec;

import libcliff.io.Pullable;
import libcliff.io.PullablePipe;
import libcliff.io.Pushable;
import libcliff.io.PushablePipe;

/**
 * byte 0x65 => byte[] { '%', '6', '5' } under all conditions
 */
public class UriByte implements PullablePipe, PushablePipe {

    public static int fromBytes(Pullable pullable) {
        int aByte = CheckedByte.checkByte(pullable.pull());
        if (aByte == '%') {
            return Hexari.fromHexariBytes(pullable);
        } else {
            return aByte;
        }
    }

    public static int toBytes(int aByte, Pushable pushable) {
        CheckedByte.checkByte(aByte);
        int size = 0;
        size += pushable.push('%');
        size += Hexari.toHexariBytes(aByte, pushable);
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
