package pd.util;

/**
 * `byte[]` as Int8Array
 */
public class Int8ArrayExtension {

    public static boolean equals(byte[] a, int aStartIndex, int aEndIndex, byte[] b, int bStartIndex) {
        if (a == null || aStartIndex < 0 || aEndIndex > a.length || aStartIndex > aEndIndex) {
            throw new IllegalArgumentException();
        }
        int n = aEndIndex - aStartIndex;
        if (b == null || bStartIndex < 0 || bStartIndex > b.length - n) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < n; i++) {
            if (a[aStartIndex + i] != b[bStartIndex + i]) {
                return false;
            }
        }
        return true;
    }

    public static int indexOf(byte[] haystack, int haystackStartIndex, int haystackEndIndex, byte[] needle, int needleStartIndex, int needleEndIndex) {
        if (haystack == null || haystackStartIndex < 0 || haystackEndIndex > haystack.length || haystackStartIndex > haystackEndIndex) {
            throw new IllegalArgumentException();
        }
        if (needle == null || needleStartIndex < 0 || needleEndIndex > needle.length || needleStartIndex > needleEndIndex) {
            throw new IllegalArgumentException();
        }
        final int length = needleEndIndex - needleStartIndex;
        for (int i = haystackStartIndex; i < haystackEndIndex - length + 1; i++) {
            if (equals(haystack, i, i + length, needle, needleStartIndex)) {
                return i;
            }
        }
        return -1;
    }

    private Int8ArrayExtension() {
        // dummy
    }
}
