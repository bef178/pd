package τ.typedef;

public final class Bits {

    public static void clear(int self, int bits) {
        self &= ~bits;
    }

    public static boolean has(int self, int bits) {
        return (self & bits) != 0;
    }

    public static void set(int self, int bits) {
        self |= bits;
    }

    private Bits() {
        // private dummy
    }
}
