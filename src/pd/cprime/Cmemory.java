package pd.cprime;

public final class Cmemory {

    public final static class Cbit32 {

        private static final int BITS = 32;

        public static boolean getBit(int mem, int offset) {
            assert offset >= 0 && offset < BITS;
            return getBits(mem, 1 << (BITS - 1 - offset)) != 0;
        }

        public static boolean getBit(int[] mem, int offset) {
            assert mem != null;
            assert offset >= 0 && offset < mem.length * BITS;
            return getBit(mem[offset / BITS], offset % BITS);
        }

        private static int getBits(int mem, int bits) {
            return mem & bits;
        }

        private static int setBit(int mem, int offset, boolean value) {
            assert offset >= 0 && offset < BITS;
            return setBits(mem, 1 << (BITS - 1 - offset), value);
        }

        public static void setBit(int[] mem, int offset, boolean value) {
            assert mem != null;
            assert offset >= 0 && offset < mem.length * BITS;
            final int p = offset / BITS;
            mem[p] = setBit(mem[p], offset % BITS, true);
        }

        private static int setBits(int mem, int bits, boolean value) {
            if (value) {
                return mem | bits;
            } else {
                return mem & ~bits;
            }
        }

        /**
         * logical (compare to arithmetic) shift right<br/>
         * i.e. mem >>> offset
         */
        @Deprecated
        public static int shiftArithmeticR(int mem, int offset) {
            assert offset >= 0 && offset < Cbit32.BITS;
            return (mem >> offset) & ((1 << (Cbit32.BITS - offset)) - 1);
        }

        /**
         * i.e. mem << offset
         */
        @Deprecated
        public static int shiftL(int mem, int offset) {
            assert offset >= 0 && offset < Cbit32.BITS;
            return mem << offset;
        }

        public static void shiftL(int[] mem, int offset) {
            assert mem != null;
            assert offset >= 0;

            final int p = offset / BITS;
            final int q = offset % BITS;

            for (int i = 0; i < mem.length; ++i) {
                mem[i] = i + p < mem.length ? mem[i + p] : 0;
            }

            int last = 0;
            for (int i = mem.length - 1; i >= 0; --i) {
                int result = (mem[i] << q) | last;
                last = mem[i] >>> (BITS - q);
                mem[i] = result;
            }
        }

        /**
         * i.e. mem >> offset
         */
        @Deprecated
        public static int shiftR(int mem, int offset) {
            assert offset >= 0 && offset < Cbit32.BITS;
            return mem >> offset;
        }

        private Cbit32() {
            // private dummy
        }
    }

    public static final class Cbit8 {

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

        private static int getBits(byte mem, int bits) {
            return mem & bits;
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
                return (byte) (mem | bits);
            } else {
                return (byte) (mem & ~bits);
            }
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

        private Cbit8() {
            // private dummy
        }
    }

    private Cmemory() {
        // private dummy;
    }
}
