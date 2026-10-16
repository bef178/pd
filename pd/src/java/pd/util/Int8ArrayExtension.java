package pd.util;

/**
 * `byte[]` as Int8Array
 */
public class Int8ArrayExtension {

    public static boolean equals(byte[] a, int aStartIndex, int aEndIndex, byte[] b, int bStartIndex, int bEndIndex) {
        checkOrThrow(a, aStartIndex, aEndIndex);
        checkOrThrow(b, bStartIndex, bEndIndex);
        if (aEndIndex - aStartIndex != bEndIndex - bStartIndex) {
            return false;
        }
        int n = aEndIndex - aStartIndex;
        for (int i = 0; i < n; i++) {
            if (a[aStartIndex + i] != b[bStartIndex + i]) {
                return false;
            }
        }
        return true;
    }

    public static int indexOf(byte[] haystack, int haystackStartIndex, int haystackEndIndex, byte[] needle, int needleStartIndex, int needleEndIndex) {
        checkOrThrow(haystack, haystackStartIndex, haystackEndIndex);
        checkOrThrow(needle, needleStartIndex, needleEndIndex);
        final int n = needleEndIndex - needleStartIndex;
        for (int i = haystackStartIndex; i < haystackEndIndex - n + 1; i++) {
            if (equals(haystack, i, i + n, needle, needleStartIndex, needleEndIndex)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean startsWith(byte[] a, int aStartIndex, int aEndIndex, byte[] b, int bStartIndex, int bEndIndex) {
        checkOrThrow(a, aStartIndex, aEndIndex);
        checkOrThrow(b, bStartIndex, bEndIndex);
        int n = bEndIndex - bStartIndex;
        return aEndIndex - aStartIndex >= n
                && equals(a, aStartIndex, aStartIndex + n, b, bStartIndex, bEndIndex);
    }

    private static void checkOrThrow(byte[] a, int i, int j) {
        if (a == null || i < 0 || j > a.length || i > j) {
            throw new IllegalArgumentException();
        }
    }

    private Int8ArrayExtension() {
        // dummy
    }
}
