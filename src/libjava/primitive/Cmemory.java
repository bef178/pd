package libjava.primitive;

public final class Cmemory {

    public final static class Cbit {

        public static boolean getBit(int mem, int offset) {
            assert offset >= 0 && offset < 32;
            return getBits(mem, 1 << offset) != 0;
        }

        private static int getBits(int mem, int bits) {
            return mem & bits;
        }

        public static void setBit(int mem, int offset, boolean value) {
            assert offset >= 0 && offset < 32;
            setBits(mem, 1 << offset, value);
        }

        public static void setBits(int mem, int bits, boolean value) {
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
    public static int lshiftR(int mem, int offset) {
        assert offset >= 0 && offset < 32;
        // return (mem >> offset) & ((1 << (32 - offset)) - 1);
        return mem >>> offset;
    }

    /**
     * i.e. mem << offset
     */
    public static int shiftL(int mem, int offset) {
        assert offset >= 0 && offset < 32;
        return mem << offset;
    }

    public static int shiftR(int mem, int offset) {
        assert offset >= 0 && offset < 32;
        return mem >> offset;
    }

    private Cmemory() {
        // private dummy;
    }
}
