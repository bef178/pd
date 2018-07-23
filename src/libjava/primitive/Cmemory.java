package libjava.primitive;

public final class Cmemory {

    private static final int INT_BITS = 32;

    public final static class Cbit {

        public static boolean getBit(int mem, int offset) {
            assert offset >= 0 && offset < INT_BITS;
            return getBits(mem, 1 << (INT_BITS - 1 - offset)) != 0;
        }

        public static boolean getBit(int[] mem, int offset) {
            assert mem != null;
            assert offset >= 0 && offset < mem.length * INT_BITS;
            return Cbit.getBit(mem[offset / INT_BITS], offset % INT_BITS);
        }

        private static int getBits(int mem, int bits) {
            return mem & bits;
        }

        private static void setBit(int mem, int offset, boolean value) {
            assert offset >= 0 && offset < INT_BITS;
            setBits(mem, 1 << (INT_BITS - 1 - offset), value);
        }

        public static void setBit(int[] mem, int offset, boolean value) {
            assert mem != null;
            assert offset >= 0 && offset < mem.length * INT_BITS;
            Cbit.setBit(mem[offset / INT_BITS], offset % INT_BITS, true);
        }

        private static void setBits(int mem, int bits, boolean value) {
            if (value) {
                mem |= bits;
            } else {
                mem &= ~bits;
            }
        }

        private Cbit() {
            // private dummy
        }
    }

    /**
     * logical (compare to arithmetic) shift right<br/>
     * i.e. mem >>> offset
     */
    @Deprecated
    public static int lshiftR(int mem, int offset) {
        assert offset >= 0 && offset < INT_BITS;
        return (mem >> offset) & ((1 << (INT_BITS - offset)) - 1);
    }

    /**
     * i.e. mem << offset
     */
    @Deprecated
    public static int shiftL(int mem, int offset) {
        assert offset >= 0 && offset < INT_BITS;
        return mem << offset;
    }

    public static void shiftL(int[] mem, int offset) {
        assert mem != null;
        assert offset >= 0;

        final int p = offset / INT_BITS;
        final int q = offset % INT_BITS;

        for (int i = 0; i < mem.length; ++i) {
            mem[i] = i + p < mem.length ? mem[i + p] : 0;
        }

        int last = 0;
        for (int i = mem.length - 1; i >= 0; --i) {
            int result = (mem[i] << q) | last;
            last = mem[i] >>> (INT_BITS - q);
            mem[i] = result;
        }
    }

    public static int shiftR(int mem, int offset) {
        assert offset >= 0 && offset < INT_BITS;
        return mem >> offset;
    }

    private Cmemory() {
        // private dummy;
    }
}
