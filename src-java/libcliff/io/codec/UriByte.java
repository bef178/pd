package libcliff.io.codec;

import libcliff.io.BytePipe;
import libcliff.io.Pullable;
import libcliff.io.Pushable;

/**
 * byte 0x65 => byte[] { '%', '6', '5' } under all conditions
 */
class UriByte extends BytePipe {

    public static int fromBytes(Pullable pullable) {
        int aByte = pullable.pull();
        if (aByte == '%') {
            return Hexari.fromHexariBytes(pullable);
        } else {
            return aByte;
        }
    }

    public static Pullable pullable(final Pullable pullable) {
        return new UriByte().setUpstream(pullable);
    }

    public static Pushable pushable(final Pushable pushable) {
        return new UriByte().setDownstream(pushable);
    }

    public static int toBytes(int aByte, Pushable pushable) {
        int size = 0;
        size += pushable.push('%');
        size += Hexari.toHexariBytes(aByte & 0xFF, pushable);
        return size;
    }

    @Override
    public int pullByte() {
        return fromBytes(upstream);
    }

    @Override
    public int pushByte(int aByte) {
        return toBytes(aByte, downstream);
    }
}
