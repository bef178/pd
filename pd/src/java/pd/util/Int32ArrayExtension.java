package pd.util;

public class Int32ArrayExtension {

    public static boolean contains(int[] a, int value) {
        return indexOf(a, value) >= 0;
    }

    public static int indexOf(int[] a, int value) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == value) {
                return i;
            }
        }
        return -1;
    }

    static void checkOrThrow(int[] a, int i, int j) {
        if (a == null || i < 0 || j > a.length || i > j) {
            throw new IllegalArgumentException();
        }
    }

    private Int32ArrayExtension() {
        // dummy
    }
}
