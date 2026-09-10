package pd.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * tests are ordered to follow the method order of {@link BitExtension}
 */
@SuppressWarnings("deprecation")
public class Test_BitExtension {

    private static byte[] bytes(int... values) {
        byte[] a = new byte[values.length];
        for (int i = 0; i < values.length; ++i) {
            a[i] = (byte) values[i];
        }
        return a;
    }

    private static byte[] shiftL(byte[] src, int offset) {
        byte[] a = src.clone();
        BitExtension.shiftL(a, offset);
        return a;
    }

    // getBit(byte octet, int index)

    @Test
    public void test_getBit() {
        // bit index 0 is the most significant bit
        assertTrue(BitExtension.getBit((byte) 0x80, 0));
        assertFalse(BitExtension.getBit((byte) 0x80, 1));
        assertTrue(BitExtension.getBit((byte) 0x01, 7));
        assertFalse(BitExtension.getBit((byte) 0x01, 6));
        assertTrue(BitExtension.getBit((byte) 0xFF, 5));
        assertFalse(BitExtension.getBit((byte) 0x00, 5));
    }

    @Test
    public void test_getBit_outOfRange_throws() {
        assertThrows(IllegalArgumentException.class, () -> BitExtension.getBit((byte) 0x00, -1));
        assertThrows(IllegalArgumentException.class, () -> BitExtension.getBit((byte) 0x00, 8));
    }

    // getBit(byte[] octets, int index)

    @Test
    public void test_getBit_ofArray() {
        byte[] a = bytes(0x80, 0x01);
        assertTrue(BitExtension.getBit(a, 0));
        assertFalse(BitExtension.getBit(a, 1));
        assertFalse(BitExtension.getBit(a, 7));
        assertFalse(BitExtension.getBit(a, 8));
        assertTrue(BitExtension.getBit(a, 15));
    }

    @Test
    public void test_getBit_ofArray_negativeOffset_throws() {
        assertThrows(IllegalArgumentException.class, () -> BitExtension.getBit(bytes(0x00), -1));
    }

    @Test
    public void test_getBit_ofArray_offsetAtLength_throws() {
        assertThrows(IllegalArgumentException.class, () -> BitExtension.getBit(bytes(0x00), 8));
    }

    @Test
    public void test_getBit_ofArray_emptyArray_throws() {
        assertThrows(IllegalArgumentException.class, () -> BitExtension.getBit(new byte[0], 0));
    }

    // setBit(byte octet, int index, boolean value)

    @Test
    public void test_setBit() {
        assertEquals((byte) 0x80, BitExtension.setBit((byte) 0x00, 0, true));
        assertEquals((byte) 0x01, BitExtension.setBit((byte) 0x00, 7, true));
        assertEquals((byte) 0x00, BitExtension.setBit((byte) 0x80, 0, false));
        assertEquals((byte) 0xFF, BitExtension.setBit((byte) 0xFF, 3, true));
    }

    @Test
    public void test_setBit_outOfRange_throws() {
        assertThrows(IllegalArgumentException.class, () -> BitExtension.setBit((byte) 0x00, -1, true));
        assertThrows(IllegalArgumentException.class, () -> BitExtension.setBit((byte) 0x00, 8, true));
    }

    // setBit(byte[] octets, int index, boolean value)

    @Test
    public void test_setBit_ofArray() {
        byte[] a = bytes(0x00, 0x00);
        BitExtension.setBit(a, 0, true);
        BitExtension.setBit(a, 15, true);
        assertArrayEquals(bytes(0x80, 0x01), a);

        BitExtension.setBit(a, 0, false);
        assertArrayEquals(bytes(0x00, 0x01), a);
    }

    // setBits(byte octet, int bitsMask, boolean value)

    @Test
    public void test_setBits() {
        assertEquals((byte) 0x0F, BitExtension.setBits((byte) 0x00, 0x0F, true));
        assertEquals((byte) 0xFF, BitExtension.setBits((byte) 0xF0, 0x0F, true));
        assertEquals((byte) 0x00, BitExtension.setBits((byte) 0x0F, 0x0F, false));
        assertEquals((byte) 0xF0, BitExtension.setBits((byte) 0xFF, 0x0F, false));
    }

    // shiftL(byte octet, int offset)

    @Test
    public void test_shiftL() {
        assertEquals((byte) 0xFE, BitExtension.shiftL((byte) 0xFF, 1));
        assertEquals((byte) 0x80, BitExtension.shiftL((byte) 0x01, 7));
    }

    // shiftL(byte[] octets, int offset)

    @Test
    public void test_shiftL_ofArray_zeroOffset_isIdentity() {
        assertArrayEquals(bytes(0x00, 0x80), shiftL(bytes(0x00, 0x80), 0));
        assertArrayEquals(bytes(0xFF, 0xFF), shiftL(bytes(0xFF, 0xFF), 0));
        assertArrayEquals(bytes(0xFF, 0x80), shiftL(bytes(0xFF, 0x80), 0));
    }

    @Test
    public void test_shiftL_ofArray_carryMustNotLeakFromNegativeByte() {
        // 0x00FF << 1 == 0x01FE; a sign-extended carry used to yield 0xFFFE
        assertArrayEquals(bytes(0x01, 0xFE), shiftL(bytes(0x00, 0xFF), 1));
        assertArrayEquals(bytes(0x02, 0x00), shiftL(bytes(0x00, 0x80), 2));
    }

    @Test
    public void test_shiftL_ofArray_wholeByteOffset() {
        // shifting by a whole byte moves every byte one slot toward index 0
        assertArrayEquals(bytes(0x00, 0x00), shiftL(bytes(0x00, 0x00), 8));
        assertArrayEquals(bytes(0x80, 0x00), shiftL(bytes(0x00, 0x80), 8));
        assertArrayEquals(bytes(0x12, 0x00), shiftL(bytes(0x00, 0x12), 8));
        assertArrayEquals(bytes(0x34, 0x00), shiftL(bytes(0x12, 0x34), 8));
        assertArrayEquals(bytes(0x00, 0x00), shiftL(bytes(0xFF, 0x00), 8));
    }

    @Test
    public void test_shiftL_ofArray_offsetBeyondLength_zeroesArray() {
        assertArrayEquals(bytes(0x00, 0x00), shiftL(bytes(0xFF, 0xFF), 17));
        assertArrayEquals(bytes(0x00, 0x00), shiftL(bytes(0xFF, 0xFF), 64));
    }

    @Test
    public void test_shiftL_ofArray_negativeOffset_throws() {
        assertThrows(IllegalArgumentException.class, () -> BitExtension.shiftL(bytes(0x00), -1));
    }

    @Test
    public void test_shiftL_ofArray_emptyArray_isNoop() {
        assertEquals(0, shiftL(new byte[0], 3).length);
    }

    // logicalShiftR(byte octet, int offset)

    @Test
    public void test_logicalShiftR_vs_shiftR() {
        // -1 == 0xFF: logical shift zero-fills, arithmetic shift sign-fills
        assertEquals((byte) 0x7F, BitExtension.logicalShiftR((byte) 0xFF, 1));
        assertEquals((byte) 0xFF, BitExtension.shiftR((byte) 0xFF, 1));
        assertEquals((byte) 0x40, BitExtension.logicalShiftR((byte) 0x80, 1));
        assertEquals((byte) 0xC0, BitExtension.shiftR((byte) 0x80, 1));
    }

    // offset contract shared by shiftL, shiftR and logicalShiftR

    @Test
    public void test_shift_negativeOffset_throws() {
        assertThrows(IllegalArgumentException.class, () -> BitExtension.shiftL((byte) 0x00, -1));
        assertThrows(IllegalArgumentException.class, () -> BitExtension.shiftR((byte) 0x00, -1));
        assertThrows(IllegalArgumentException.class, () -> BitExtension.logicalShiftR((byte) 0x00, -1));
    }

    @Test
    public void test_shift_offsetAtOrBeyondByteWidth() {
        // offset >= BITS_PER_BYTE is legal; a left shift drops every bit
        assertEquals((byte) 0x00, BitExtension.shiftL((byte) 0xFF, 8));
        assertEquals((byte) 0x00, BitExtension.shiftL((byte) 0xFF, 9));
        assertEquals((byte) 0x00, BitExtension.shiftL((byte) 0x80, 31));
        // so does a logical right shift
        assertEquals((byte) 0x00, BitExtension.logicalShiftR((byte) 0xFF, 8));
        assertEquals((byte) 0x00, BitExtension.logicalShiftR((byte) 0xFF, 9));
        assertEquals((byte) 0x00, BitExtension.logicalShiftR((byte) 0x80, 31));
        // an arithmetic right shift sign-fills instead, so a negative octet stays negative
        assertEquals((byte) 0xFF, BitExtension.shiftR((byte) 0xFF, 8));
        assertEquals((byte) 0xFF, BitExtension.shiftR((byte) 0xFF, 31));
        assertEquals((byte) 0x00, BitExtension.shiftR((byte) 0x7F, 8));
    }

    @Test
    public void test_shift_offsetBeyondShiftCountMask() {
        // `x << n` masks n by 31, so a naive implementation wraps at offset 32 and 64
        for (int offset : new int[] { 32, 33, 63, 64, 100 }) {
            assertEquals((byte) 0x00, BitExtension.shiftL((byte) 0xFF, offset),
                    String.format("shiftL offset=%d", offset));
            assertEquals((byte) 0x00, BitExtension.shiftL((byte) 0x80, offset),
                    String.format("shiftL offset=%d", offset));
            assertEquals((byte) 0x00, BitExtension.logicalShiftR((byte) 0xFF, offset),
                    String.format("logicalShiftR offset=%d", offset));
            assertEquals((byte) 0xFF, BitExtension.shiftR((byte) 0xFF, offset),
                    String.format("shiftR offset=%d", offset));
            assertEquals((byte) 0x00, BitExtension.shiftR((byte) 0x7F, offset),
                    String.format("shiftR offset=%d", offset));
        }
    }
}
