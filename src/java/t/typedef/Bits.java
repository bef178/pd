package t.typedef;

public final class Bits {

    public static void clear(int self, int bits) {
        self &= ~bits;
    }

    public static int get(int self, int bits) {
        return self & bits;
    }

    public static boolean hasAll(int self, int bits) {
        return get(self, bits) == bits;
    }

    public static boolean hasAny(int self, int bits) {
        return get(self, bits) != 0;
    }

    public static void set(int self, int bits) {
        self |= bits;
    }

    private Bits() {
        // private dummy
    }
}
