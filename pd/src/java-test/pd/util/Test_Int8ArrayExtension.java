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

    // equals(byte[] a, int aStartIndex, int aEndIndex, byte[] b, int bStartIndex, int bEndIndex)

    @Test
    public void test_equals() {
        assertTrue(Int8ArrayExtension.equals(bytes(0x12, 0x34), 0, 2, bytes(0x12, 0x34), 0, 2));
        assertFalse(Int8ArrayExtension.equals(bytes(0x12, 0x34), 0, 2, bytes(0x12, 0x35), 0, 2));
        // the ranges take part in the comparison, not the whole arrays
        assertTrue(Int8ArrayExtension.equals(bytes(0xFF, 0x12, 0x34, 0xFF), 1, 3, bytes(0x12, 0x34), 0, 2));
        assertTrue(Int8ArrayExtension.equals(bytes(0xFF, 0x12, 0x34, 0xFF), 1, 3, bytes(0x56, 0x12, 0x34, 0x56), 1, 3));
        // ranges of different lengths are not equal
        assertFalse(Int8ArrayExtension.equals(bytes(0x12, 0x34), 0, 2, bytes(0x12, 0x34, 0x56), 0, 3));
        assertFalse(Int8ArrayExtension.equals(bytes(0x12, 0x34, 0x56), 0, 3, bytes(0x12, 0x34), 0, 2));
        // an empty range is vacuously equal to an empty range
        assertTrue(Int8ArrayExtension.equals(bytes(0x12, 0x34), 1, 1, bytes(0xFF), 1, 1));
        assertTrue(Int8ArrayExtension.equals(bytes(0x12, 0x34), 1, 1, bytes(0xFF), 0, 0));
    }

    @Test
    public void test_equals_invalidArguments_throw() {
        final byte[] a = bytes(0x12, 0x34);
        final byte[] b = bytes(0x56, 0x78);
        // `a` or `b` is null
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.equals(null, 0, 2, b, 0, 2));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.equals(a, 0, 2, null, 0, 2));
        // aStartIndex < 0, aStartIndex > aEndIndex, aEndIndex > a.length
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.equals(a, -1, 2, b, 0, 2));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.equals(a, 2, 1, b, 0, 2));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.equals(a, 0, 3, b, 0, 2));
        // bStartIndex < 0, bStartIndex > bEndIndex, bEndIndex > b.length
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.equals(a, 0, 2, b, -1, 2));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.equals(a, 0, 2, b, 2, 1));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.equals(a, 0, 2, b, 0, 3));
        // an end index of Integer.MAX_VALUE is out of range, and no arithmetic may wrap it back into range
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.equals(a, 0, 2, b, 0, Integer.MAX_VALUE));
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

    // readUnsignedInt16BigEndian, readUnsignedInt16LittleEndian,
    // readUnsignedInt32BigEndian, readUnsignedInt32LittleEndian,
    // readUnsignedInt63BigEndian, readUnsignedInt63LittleEndian

    @Test
    public void test_readUnsignedInt16() {
        assertEquals(0x1234, Int8ArrayExtension.readUnsignedInt16BigEndian(bytes(0x12, 0x34), 0));
        assertEquals(0x1234, Int8ArrayExtension.readUnsignedInt16LittleEndian(bytes(0x34, 0x12), 0));
        // octets are unsigned, so the result is not sign-extended
        assertEquals(0xFFFE, Int8ArrayExtension.readUnsignedInt16BigEndian(bytes(0xFF, 0xFE), 0));
        assertEquals(0xFFFE, Int8ArrayExtension.readUnsignedInt16LittleEndian(bytes(0xFE, 0xFF), 0));
        // only the 2 octets at `start` take part
        assertEquals(0x1234, Int8ArrayExtension.readUnsignedInt16BigEndian(bytes(0x99, 0x12, 0x34, 0x99), 1));
        assertEquals(0x1234, Int8ArrayExtension.readUnsignedInt16LittleEndian(bytes(0x99, 0x34, 0x12, 0x99), 1));
        // invalid arguments: null `a`, start < 0, not enough octets
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt16BigEndian(null, 0));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt16LittleEndian(null, 0));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt16BigEndian(bytes(0x12, 0x34), -1));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt16LittleEndian(bytes(0x12, 0x34), -1));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt16BigEndian(bytes(0x12), 0));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt16LittleEndian(bytes(0x12), 0));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt16LittleEndian(bytes(0x12, 0x34), 1));
        // a start index of Integer.MAX_VALUE is out of range, and no arithmetic may wrap it back into range
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt16BigEndian(bytes(0x12, 0x34), Integer.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt16LittleEndian(bytes(0x12, 0x34), Integer.MAX_VALUE - 1));
    }

    @Test
    public void test_readUnsignedInt32() {
        assertEquals(0x12345678L, Int8ArrayExtension.readUnsignedInt32BigEndian(bytes(0x12, 0x34, 0x56, 0x78), 0));
        assertEquals(0x12345678L, Int8ArrayExtension.readUnsignedInt32LittleEndian(bytes(0x78, 0x56, 0x34, 0x12), 0));
        // octets are unsigned, so the result is not sign-extended
        assertEquals(0x80000000L, Int8ArrayExtension.readUnsignedInt32BigEndian(bytes(0x80, 0x00, 0x00, 0x00), 0));
        assertEquals(0xFFFFFFFFL, Int8ArrayExtension.readUnsignedInt32BigEndian(bytes(0xFF, 0xFF, 0xFF, 0xFF), 0));
        assertEquals(0xFFFFFFFFL, Int8ArrayExtension.readUnsignedInt32LittleEndian(bytes(0xFF, 0xFF, 0xFF, 0xFF), 0));
        // only the 4 octets at `start` take part
        assertEquals(0x12345678L, Int8ArrayExtension.readUnsignedInt32BigEndian(bytes(0x99, 0x12, 0x34, 0x56, 0x78), 1));
        assertEquals(0x12345678L, Int8ArrayExtension.readUnsignedInt32LittleEndian(bytes(0x99, 0x78, 0x56, 0x34, 0x12), 1));
        // invalid arguments: null `a`, start < 0, not enough octets
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt32BigEndian(null, 0));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt32LittleEndian(null, 0));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt32BigEndian(bytes(0x12, 0x34, 0x56, 0x78), -1));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt32LittleEndian(bytes(0x12, 0x34, 0x56, 0x78), -1));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt32BigEndian(bytes(0x12, 0x34, 0x56), 0));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt32LittleEndian(bytes(0x12, 0x34, 0x56), 0));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt32LittleEndian(bytes(0x12, 0x34, 0x56, 0x78), 1));
        // a start index of Integer.MAX_VALUE is out of range, and no arithmetic may wrap it back into range
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt32BigEndian(bytes(0x12, 0x34, 0x56, 0x78), Integer.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt32LittleEndian(bytes(0x12, 0x34, 0x56, 0x78), Integer.MAX_VALUE - 2));
    }

    @Test
    public void test_readUnsignedInt63() {
        assertEquals(0x123456789ABCDEF0L, Int8ArrayExtension.readUnsignedInt63BigEndian(bytes(0x12, 0x34, 0x56, 0x78, 0x9A, 0xBC, 0xDE, 0xF0), 0));
        assertEquals(0x123456789ABCDEF0L, Int8ArrayExtension.readUnsignedInt63LittleEndian(bytes(0xF0, 0xDE, 0xBC, 0x9A, 0x78, 0x56, 0x34, 0x12), 0));
        // the largest value a signed 64-bit integer can hold
        assertEquals(Long.MAX_VALUE, Int8ArrayExtension.readUnsignedInt63BigEndian(bytes(0x7F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF), 0));
        assertEquals(Long.MAX_VALUE, Int8ArrayExtension.readUnsignedInt63LittleEndian(bytes(0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x7F), 0));
        // bit 63 set does not fit in a positive long
        assertEquals(-1, Int8ArrayExtension.readUnsignedInt63BigEndian(bytes(0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00), 0));
        assertEquals(-1, Int8ArrayExtension.readUnsignedInt63LittleEndian(bytes(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x80), 0));
        // only the 8 octets at `start` take part
        assertEquals(0x123456789ABCDEF0L, Int8ArrayExtension.readUnsignedInt63BigEndian(bytes(0x99, 0x12, 0x34, 0x56, 0x78, 0x9A, 0xBC, 0xDE, 0xF0), 1));
        assertEquals(0x123456789ABCDEF0L, Int8ArrayExtension.readUnsignedInt63LittleEndian(bytes(0x99, 0xF0, 0xDE, 0xBC, 0x9A, 0x78, 0x56, 0x34, 0x12), 1));
        // invalid arguments: null `a`, start < 0, not enough octets
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt63BigEndian(null, 0));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt63LittleEndian(null, 0));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt63BigEndian(bytes(0x12, 0x34, 0x56, 0x78, 0x9A, 0xBC, 0xDE, 0xF0), -1));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt63LittleEndian(bytes(0x12, 0x34, 0x56, 0x78, 0x9A, 0xBC, 0xDE, 0xF0), -1));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt63BigEndian(bytes(0x12, 0x34, 0x56, 0x78, 0x9A, 0xBC, 0xDE), 0));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt63LittleEndian(bytes(0x12, 0x34, 0x56, 0x78, 0x9A, 0xBC, 0xDE), 0));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt63LittleEndian(bytes(0x12, 0x34, 0x56, 0x78, 0x9A, 0xBC, 0xDE, 0xF0), 1));
        // a start index of Integer.MAX_VALUE is out of range, and no arithmetic may wrap it back into range
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt63BigEndian(bytes(0x12, 0x34, 0x56, 0x78, 0x9A, 0xBC, 0xDE, 0xF0), Integer.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.readUnsignedInt63LittleEndian(bytes(0x12, 0x34, 0x56, 0x78, 0x9A, 0xBC, 0xDE, 0xF0), Integer.MAX_VALUE - 4));
    }

    // startsWith(byte[] a, int aStartIndex, int aEndIndex, byte[] b, int bStartIndex, int bEndIndex)

    @Test
    public void test_startsWith() {
        assertTrue(Int8ArrayExtension.startsWith(bytes(0x12, 0x34, 0x56), 0, 3, bytes(0x12, 0x34), 0, 2));
        assertFalse(Int8ArrayExtension.startsWith(bytes(0x12, 0x34, 0x56), 0, 3, bytes(0x12, 0x35), 0, 2));
        // both ranges start where they are told to, not at the beginning of their arrays
        assertTrue(Int8ArrayExtension.startsWith(bytes(0xFF, 0x12, 0x34), 1, 3, bytes(0x34, 0x12, 0x34), 1, 3));
        // a range is a prefix of itself
        assertTrue(Int8ArrayExtension.startsWith(bytes(0x12, 0x34), 0, 2, bytes(0x12, 0x34), 0, 2));
        // a shorter range of `a` is not a prefix, even if the octets so far match
        assertFalse(Int8ArrayExtension.startsWith(bytes(0x12), 0, 1, bytes(0x12, 0x34), 0, 2));
        // only the first `bEndIndex - bStartIndex` octets of `a` take part in the comparison
        assertTrue(Int8ArrayExtension.startsWith(bytes(0x12, 0x34, 0xFF), 0, 3, bytes(0x12, 0x34), 0, 2));
        // an empty range of `b` is vacuously a prefix
        assertTrue(Int8ArrayExtension.startsWith(bytes(0x12, 0x34), 0, 2, bytes(0xFF), 1, 1));
        // while an empty range of `a` is not a prefix of a non-empty range of `b`
        assertFalse(Int8ArrayExtension.startsWith(bytes(0x12, 0x34), 1, 1, bytes(0x34), 0, 1));
    }

    @Test
    public void test_startsWith_invalidArguments_throw() {
        final byte[] a = bytes(0x12, 0x34);
        final byte[] b = bytes(0x56, 0x78);
        // `a` or `b` is null
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.startsWith(null, 0, 2, b, 0, 2));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.startsWith(a, 0, 2, (byte[]) null, 0, 2));
        // aStartIndex < 0, aStartIndex > aEndIndex, aEndIndex > a.length
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.startsWith(a, -1, 2, b, 0, 2));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.startsWith(a, 2, 1, b, 0, 2));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.startsWith(a, 0, 3, b, 0, 2));
        // bStartIndex < 0, bStartIndex > bEndIndex, bEndIndex > b.length
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.startsWith(a, 0, 2, b, -1, 2));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.startsWith(a, 0, 2, b, 2, 1));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.startsWith(a, 0, 2, b, 0, 3));
    }

    // startsWith(byte[] haystack, int haystackStartIndex, int haystackEndIndex,
    //         int[] needle, int needleStartIndex, int needleEndIndex)

    @Test
    public void test_startsWith_intNeedle() {
        assertTrue(Int8ArrayExtension.startsWith(bytes(0x12, 0x34, 0x56), 0, 3, new int[]{0x12, 0x34}, 0, 2));
        assertFalse(Int8ArrayExtension.startsWith(bytes(0x12, 0x34, 0x56), 0, 3, new int[]{0x12, 0x35}, 0, 2));
        // a needle entry is an unsigned octet, so 0xFF matches the byte -1
        assertTrue(Int8ArrayExtension.startsWith(bytes(0x89, 0x50, 0x4E, 0x47), 0, 4, new int[]{0x89, 0x50}, 0, 2));
        // both ranges start where they are told to, not at the beginning of their arrays
        assertTrue(Int8ArrayExtension.startsWith(bytes(0xFF, 0x12, 0x34), 1, 3, new int[]{0x34, 0x12, 0x34}, 1, 3));
        // a range is a prefix of itself
        assertTrue(Int8ArrayExtension.startsWith(bytes(0x12, 0x34), 0, 2, new int[]{0x12, 0x34}, 0, 2));
        // a shorter range of the haystack is not a prefix, even if the octets so far match
        assertFalse(Int8ArrayExtension.startsWith(bytes(0x12), 0, 1, new int[]{0x12, 0x34}, 0, 2));
        // only the first `needleEndIndex - needleStartIndex` octets of the haystack take part in the comparison
        assertTrue(Int8ArrayExtension.startsWith(bytes(0x12, 0x34, 0xFF), 0, 3, new int[]{0x12, 0x34}, 0, 2));
        // an empty needle range is vacuously a prefix
        assertTrue(Int8ArrayExtension.startsWith(bytes(0x12, 0x34), 0, 2, new int[]{0xFF}, 1, 1));
        // while an empty haystack range is not a prefix of a non-empty needle range
        assertFalse(Int8ArrayExtension.startsWith(bytes(0x12, 0x34), 1, 1, new int[]{0x34}, 0, 1));
    }

    @Test
    public void test_startsWith_intNeedle_invalidArguments_throw() {
        final byte[] haystack = bytes(0x12, 0x34);
        final int[] needle = new int[]{0x56, 0x78};
        // `haystack` or `needle` is null
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.startsWith(null, 0, 2, needle, 0, 2));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.startsWith(haystack, 0, 2, (int[]) null, 0, 2));
        // haystackStartIndex < 0, haystackStartIndex > haystackEndIndex, haystackEndIndex > haystack.length
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.startsWith(haystack, -1, 2, needle, 0, 2));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.startsWith(haystack, 2, 1, needle, 0, 2));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.startsWith(haystack, 0, 3, needle, 0, 2));
        // needleStartIndex < 0, needleStartIndex > needleEndIndex, needleEndIndex > needle.length
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.startsWith(haystack, 0, 2, needle, -1, 2));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.startsWith(haystack, 0, 2, needle, 2, 1));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.startsWith(haystack, 0, 2, needle, 0, 3));
        // a needle entry must be an unsigned octet
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.startsWith(haystack, 0, 2, new int[]{-1}, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.startsWith(haystack, 0, 2, new int[]{0x100}, 0, 1));
        // the value check does not depend on the haystack being long enough
        assertThrows(IllegalArgumentException.class, () -> Int8ArrayExtension.startsWith(bytes(0x12), 0, 1, new int[]{0x12, 0x100}, 0, 2));
        // only the octets in the needle range are checked
        assertTrue(Int8ArrayExtension.startsWith(bytes(0x12, 0x34), 0, 2, new int[]{0x100, 0x12}, 1, 2));
    }
}
