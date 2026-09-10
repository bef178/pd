package pd.util;

import lombok.NonNull;

/**
 * `byte` or `byte[]` as BitStream
 */
public class BitExtension {

    private static final int BITS_PER_BYTE = 8;

    /**
     * (0xF0, 0) => 1
     */
    public static boolean getBit(byte octet, int index) {
        throwIfIndexOutOfRange(index);
        int mask = 1 << (BITS_PER_BYTE - 1 - index);
        return (octet & mask) != 0;
    }

    public static boolean getBit(byte @NonNull [] octets, int index) {
        throwIfIndexOutOfRange(index, octets.length * BITS_PER_BYTE);
        return getBit(octets[index / BITS_PER_BYTE], index % BITS_PER_BYTE);
    }

    public static byte setBit(byte octet, int index, boolean value) {
        throwIfIndexOutOfRange(index);
        return setBits(octet, 1 << (BITS_PER_BYTE - 1 - index), value);
    }

    public static void setBit(byte @NonNull [] octets, int index, boolean value) {
        throwIfIndexOutOfRange(index, octets.length * BITS_PER_BYTE);
        final int p = index / BITS_PER_BYTE;
        octets[p] = setBit(octets[p], index % BITS_PER_BYTE, value);
    }

    public static byte setBits(byte octet, int bitsMask, boolean value) {
        if (value) {
            octet |= (byte) bitsMask;
        } else {
            octet &= (byte) ~bitsMask;
        }
        return octet;
    }

    /**
     * i.e. octet << offset
     */
    @Deprecated
    public static byte shiftL(byte octet, int offset) {
        throwIfNegativeOffset(offset);
        if (offset >= BITS_PER_BYTE) {
            return 0;
        }
        octet <<= offset;
        return octet;
    }

    public static void shiftL(byte @NonNull [] octets, int offset) {
        throwIfNegativeOffset(offset);
        final int p = offset / BITS_PER_BYTE;
        final int q = offset % BITS_PER_BYTE;

        for (int i = 0; i < octets.length; ++i) {
            octets[i] = i + p < octets.length ? octets[i + p] : 0;
        }

        int carry = 0;
        for (int i = octets.length - 1; i >= 0; --i) {
            int result = (octets[i] << q) | carry;
            carry = (octets[i] & 0xFF) >>> (BITS_PER_BYTE - q);
            octets[i] = (byte) result;
        }
    }

    /**
     * Arithmetic Shift R
     * i.e. octet >> offset
     */
    @Deprecated
    public static byte shiftR(byte octet, int offset) {
        throwIfNegativeOffset(offset);
        if (offset >= BITS_PER_BYTE) {
            return octet < 0 ? (byte) 0xFF : (byte) 0x00;
        }
        octet >>= offset;
        return octet;
    }

    /**
     * i.e. octet >>> offset
     */
    @Deprecated
    public static byte logicalShiftR(byte octet, int offset) {
        throwIfNegativeOffset(offset);
        if (offset >= BITS_PER_BYTE) {
            return 0;
        }
        octet >>= offset;
        octet &= (byte) ((1 << (BITS_PER_BYTE - offset)) - 1);
        return octet;
    }

    private static void throwIfIndexOutOfRange(int index) {
        throwIfIndexOutOfRange(index, BITS_PER_BYTE);
    }

    private static void throwIfIndexOutOfRange(int index, int upperBound) {
        if (index < 0 || index >= upperBound) {
            throw new IllegalArgumentException(String.format("E: expected `index` is in [0, %d), actual is %d", upperBound, index));
        }
    }

    private static void throwIfNegativeOffset(int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("E: `offset` must not be negative");
        }
    }

    private BitExtension() {
        // private dummy
    }
}
