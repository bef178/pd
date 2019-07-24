package pd.ctype;

public final class Cbit8 {

    private static final int BITS = 8;

    public static boolean getBit(byte mem, int offset) {
        assert offset >= 0 && offset < BITS;
        return getBits(mem, 1 << (BITS - 1 - offset)) != 0;
    }

    public static boolean getBit(byte[] mem, int offset) {
        assert mem != null;
        assert offset >= 0 && offset < mem.length * BITS;
        return getBit(mem[offset / BITS], offset % BITS);
    }

    private static byte getBits(byte mem, int bits) {
        mem &= bits;
        return mem;
    }

    private static byte setBit(byte mem, int offset, boolean value) {
        assert offset >= 0 && offset < BITS;
        return setBits(mem, 1 << (BITS - 1 - offset), value);
    }

    public static void setBit(byte[] mem, int offset, boolean value) {
        assert mem != null;
        assert offset >= 0 && offset < mem.length * BITS;
        final int p = offset / BITS;
        mem[p] = setBit(mem[p], offset % BITS, true);
    }

    private static byte setBits(byte mem, int bits, boolean value) {
        if (value) {
            mem |= bits;
        } else {
            mem &= ~bits;
        }
        return mem;
    }

    /**
     * logical (compare to arithmetic) shift right<br/>
     * i.e. mem >>> offset
     */
    @Deprecated
    public static byte shiftArithmeticR(byte mem, int offset) {
        assert offset >= 0 && offset < BITS;
        mem >>= offset;
        mem &= ((1 << (BITS - offset)) - 1);
        return mem;
    }

    /**
     * i.e. mem << offset
     */
    @Deprecated
    public static byte shiftL(byte mem, int offset) {
        assert offset >= 0 && offset < BITS;
        mem <<= offset;
        return mem;
    }

    public static void shiftL(byte[] mem, int offset) {
        assert mem != null;
        assert offset >= 0;

        final int p = offset / BITS;
        final int q = offset % BITS;

        for (int i = 0; i < mem.length; ++i) {
            mem[i] = i + p < mem.length ? mem[i + p] : 0;
        }

        int carry = 0;
        for (int i = mem.length - 1; i >= 0; --i) {
            int result = (mem[i] << q) | carry;
            carry = mem[i] >>> (BITS - q);
            mem[i] = (byte) result;
        }
    }

    /**
     * i.e. mem >> offset
     */
    @Deprecated
    public static byte shiftR(byte mem, int offset) {
        assert offset >= 0 && offset < BITS;
        mem >>= offset;
        return mem;
    }

    private Cbit8() {
        // private dummy
    }
}
