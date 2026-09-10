package pd.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * tests are ordered to follow the method order of {@link Int8ArrayExtension}
 */
public class Test_Int8ArrayExtension {

    private static byte[] bytes(int... values) {
        byte[] a = new byte[values.length];
        for (int i = 0; i < values.length; ++i) {
            a[i] = (byte) values[i];
        }
        return a;
    }

    // equals(byte[] a, int aStartIndex, int aEndIndex, byte[] b, int bStartIndex)

    @Test
    public void test_equals() {
        assertTrue(Int8ArrayExtension.equals(bytes(0x12, 0x34), 0, 2, bytes(0x12, 0x34), 0));
        assertFalse(Int8ArrayExtension.equals(bytes(0x12, 0x34), 0, 2, bytes(0x12, 0x35), 0));
        // a range of `a` against a starting point of `b`
        assertTrue(Int8ArrayExtension.equals(bytes(0xFF, 0x12, 0x34, 0xFF), 1, 3, bytes(0x12, 0x34), 0));
        // only the first `aEndIndex - aStartIndex` octets of `b` take part in the comparison
        assertTrue(Int8ArrayExtension.equals(bytes(0x12, 0x34), 0, 2, bytes(0x12, 0x34, 0x56), 0));
        // an empty range is vacuously equal
        assertTrue(Int8ArrayExtension.equals(bytes(0x12, 0x34), 1, 1, bytes(0xFF), 1));
    }

    @Test
    public void test_equals_invalidArguments_throw() {
        final byte[] a = bytes(0x12, 0x34);
        final byte[] b = bytes(0x56, 0x78);
        // `a` or `b` is null
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.equals(null, 0, 2, b, 0));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.equals(a, 0, 2, null, 0));
        // aStartIndex < 0, aStartIndex > aEndIndex, aEndIndex > a.length
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.equals(a, -1, 2, b, 0));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.equals(a, 2, 1, b, 0));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.equals(a, 0, 3, b, 0));
        // bStartIndex < 0, bStartIndex + n > b.length
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.equals(a, 0, 2, b, -1));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.equals(a, 0, 2, b, 1));
        // `bStartIndex + n` *int*-adds and wraps around, so the bound must be checked without adding
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.equals(a, 0, 2, b, Integer.MAX_VALUE));
    }

    // indexOf(byte[] haystack, int haystackStartIndex, int haystackEndIndex,
    //         byte[] needle, int needleStartIndex, int needleEndIndex)

    @Test
    public void test_indexOf() {
        assertEquals(0, Int8ArrayExtension.indexOf(bytes(0x12, 0x34, 0x56), 0, 3, bytes(0x12, 0x34), 0, 2));
        assertEquals(2, Int8ArrayExtension.indexOf(bytes(0x12, 0x34, 0x56), 0, 3, bytes(0x56), 0, 1));
        assertEquals(-1, Int8ArrayExtension.indexOf(bytes(0x12, 0x34, 0x56), 0, 3, bytes(0x34, 0x12), 0, 2));
        // both ranges are respected, the returned index is absolute
        byte[] haystack = bytes(0x12, 0x34, 0xFF, 0x12, 0x34);
        assertEquals(0, Int8ArrayExtension.indexOf(haystack, 0, 5, bytes(0x12, 0x34), 0, 2));
        assertEquals(3, Int8ArrayExtension.indexOf(haystack, 1, 5, bytes(0x12, 0x34), 0, 2));
        assertEquals(-1, Int8ArrayExtension.indexOf(haystack, 1, 4, bytes(0x12, 0x34), 0, 2));
        assertEquals(1, Int8ArrayExtension.indexOf(bytes(0x12, 0x34, 0x56), 0, 3, bytes(0xFF, 0x34), 1, 2));
        // a needle longer than the haystack range is not found
        assertEquals(-1, Int8ArrayExtension.indexOf(bytes(0x12), 0, 1, bytes(0x12, 0x34), 0, 2));
        // an empty needle matches at haystackStartIndex
        assertEquals(2, Int8ArrayExtension.indexOf(bytes(0x12, 0x34, 0x56), 2, 3, bytes(0x99), 0, 0));
    }

    @Test
    public void test_indexOf_invalidArguments_throw() {
        final byte[] haystack = bytes(0x12, 0x34);
        final byte[] needle = bytes(0x56, 0x78);
        // `haystack` or `needle` is null
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.indexOf(null, 0, 2, needle, 0, 2));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.indexOf(haystack, 0, 2, null, 0, 0));
        // haystackStartIndex < 0, haystackStartIndex > haystackEndIndex, haystackEndIndex > haystack.length
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.indexOf(haystack, -1, 2, needle, 0, 2));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.indexOf(haystack, 2, 1, needle, 0, 2));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.indexOf(haystack, 0, 3, needle, 0, 2));
        // needleStartIndex < 0, needleStartIndex > needleEndIndex, needleEndIndex > needle.length
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.indexOf(haystack, 0, 2, needle, -1, 2));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.indexOf(haystack, 0, 2, needle, 2, 1));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.indexOf(haystack, 0, 2, needle, 0, 3));
    }
}
