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

    private Int32ArrayExtension() {
        // dummy
    }
}
