package pd.util;

/**
 * `byte` or `byte[]` as BitStream
 */
public class Int8ArrayExtension {

    private static final int BITS_PER_BYTE = 8;

    public static boolean getBit(byte mem, int offset) {
        assert offset >= 0 && offset < BITS_PER_BYTE;
        int mask = 1 << (BITS_PER_BYTE - 1 - offset);
        return (mem & mask) != 0;
    }

    public static boolean getBit(byte[] a, int offset) {
        assert a != null;
        assert offset >= 0 && offset < a.length * BITS_PER_BYTE;
        return getBit(a[offset / BITS_PER_BYTE], offset % BITS_PER_BYTE);
    }

    public static byte setBit(byte mem, int offset, boolean value) {
        assert offset >= 0 && offset < BITS_PER_BYTE;
        return setBits(mem, 1 << (BITS_PER_BYTE - 1 - offset), value);
    }

    public static void setBit(byte[] a, int offset, boolean value) {
        assert a != null;
        assert offset >= 0 && offset < a.length * BITS_PER_BYTE;
        final int p = offset / BITS_PER_BYTE;
        a[p] = setBit(a[p], offset % BITS_PER_BYTE, true);
    }

    public static byte setBits(byte mem, int bitsMask, boolean value) {
        if (value) {
            mem |= bitsMask;
        } else {
            mem &= ~bitsMask;
        }
        return mem;
    }

    /**
     * logical (compare to arithmetic) shift right<br/>
     * i.e. mem >>> offset
     */
    @Deprecated
    public static byte shiftArithmeticR(byte mem, int offset) {
        assert offset >= 0 && offset < BITS_PER_BYTE;
        mem >>= offset;
        mem &= ((1 << (BITS_PER_BYTE - offset)) - 1);
        return mem;
    }

    /**
     * i.e. mem << offset
     */
    @Deprecated
    public static byte shiftL(byte mem, int offset) {
        assert offset >= 0 && offset < BITS_PER_BYTE;
        mem <<= offset;
        return mem;
    }

    public static void shiftL(byte[] a, int offset) {
        assert a != null;
        assert offset >= 0;

        final int p = offset / BITS_PER_BYTE;
        final int q = offset % BITS_PER_BYTE;

        for (int i = 0; i < a.length; ++i) {
            a[i] = i + p < a.length ? a[i + p] : 0;
        }

        int carry = 0;
        for (int i = a.length - 1; i >= 0; --i) {
            int result = (a[i] << q) | carry;
            carry = a[i] >>> (BITS_PER_BYTE - q);
            a[i] = (byte) result;
        }
    }

    /**
     * i.e. mem >> offset
     */
    @Deprecated
    public static byte shiftR(byte mem, int offset) {
        assert offset >= 0 && offset < BITS_PER_BYTE;
        mem >>= offset;
        return mem;
    }

    private Int8ArrayExtension() {
        // private dummy
    }
}
