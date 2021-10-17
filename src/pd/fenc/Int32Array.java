package pd.fenc;

public final class Int32Array {

    public static final boolean contains(int[] a, int value) {
        return indexOf(a, value) >= 0;
    }

    public static final int indexOf(int[] a, int value) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == value) {
                return i;
            }
        }
        return -1;
    }

    private Int32Array() {
        // dummy
    }
}
