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

    public static int readUnsignedInt16BigEndian(byte[] a, int start) {
        checkOrThrow(a, start, start + 2);
        return ((a[start] & 0xFF) << 8) | (a[start + 1] & 0xFF);
    }

    public static int readUnsignedInt16LittleEndian(byte[] a, int start) {
        checkOrThrow(a, start, start + 2);
        return (a[start] & 0xFF) | ((a[start + 1] & 0xFF) << 8);
    }

    public static long readUnsignedInt32BigEndian(byte[] a, int start) {
        checkOrThrow(a, start, start + 4);
        return ((long) (a[start] & 0xFF) << 24)
                | ((long) (a[start + 1] & 0xFF) << 16)
                | ((long) (a[start + 2] & 0xFF) << 8)
                | (a[start + 3] & 0xFF);
    }

    public static long readUnsignedInt32LittleEndian(byte[] a, int start) {
        checkOrThrow(a, start, start + 4);
        return ((long) (a[start] & 0xFF))
                | ((long) (a[start + 1] & 0xFF) << 8)
                | ((long) (a[start + 2] & 0xFF) << 16)
                | ((long) (a[start + 3] & 0xFF) << 24);
    }

    public static long readUnsignedInt63BigEndian(byte[] a, int start) {
        checkOrThrow(a, start, start + 8);
        long high = readUnsignedInt32BigEndian(a, start);
        if (high > Integer.MAX_VALUE) {
            return -1;
        }
        return (high << 32) | readUnsignedInt32BigEndian(a, start + 4);
    }

    public static long readUnsignedInt63LittleEndian(byte[] a, int start) {
        checkOrThrow(a, start, start + 8);
        long high = readUnsignedInt32LittleEndian(a, start + 4);
        if (high > Integer.MAX_VALUE) {
            return -1;
        }
        return (high << 32) | readUnsignedInt32LittleEndian(a, start);
    }

    public static boolean startsWith(byte[] haystack, int haystackStartIndex, int haystackEndIndex, byte[] needle, int needleStartIndex, int needleEndIndex) {
        checkOrThrow(haystack, haystackStartIndex, haystackEndIndex);
        checkOrThrow(needle, needleStartIndex, needleEndIndex);
        int n = needleEndIndex - needleStartIndex;
        return haystackEndIndex - haystackStartIndex >= n
                && equals(haystack, haystackStartIndex, haystackStartIndex + n, needle, needleStartIndex, needleEndIndex);
    }

    public static boolean startsWith(byte[] haystack, int haystackStartIndex, int haystackEndIndex, int[] needle, int needleStartIndex, int needleEndIndex) {
        checkOrThrow(haystack, haystackStartIndex, haystackEndIndex);
        Int32ArrayExtension.checkOrThrow(needle, needleStartIndex, needleEndIndex);
        for (int i = needleStartIndex; i < needleEndIndex; i++) {
            if (needle[i] < 0 || needle[i] > 0xFF) {
                throw new IllegalArgumentException();
            }
        }
        int n = needleEndIndex - needleStartIndex;
        if (haystackEndIndex - haystackStartIndex < n) {
            return false;
        }
        for (int i = 0; i < n; i++) {
            if ((haystack[haystackStartIndex + i] & 0xFF) != needle[needleStartIndex + i]) {
                return false;
            }
        }
        return true;
    }

    static void checkOrThrow(byte[] a, int i, int j) {
        if (a == null || i < 0 || j > a.length || i > j) {
            throw new IllegalArgumentException();
        }
    }

    private Int8ArrayExtension() {
        // dummy
    }
}
