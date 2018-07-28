package libjava.adt;

import java.nio.ByteBuffer;
import java.util.Arrays;

import libjava.primitive.Cmemory.Cbit8;

/**
 * - wrap continues bits into a big integer<br/>
 * - will not implicitly change capacity<br/>
 * - api name after asm<br/>
 */
public class BitInt implements Comparable<BitInt> {

    private static final int BITS = 8;

    private static int bitSize(byte[] segs) {
        boolean topBit = segs[0] < 0;
        byte impliedValue = (byte) (topBit ? -1 : 0);
        for (int i = 0; i < segs.length; ++i) {
            if (segs[i] != impliedValue) {
                for (int j = 0; j < BITS; ++j) {
                    if (Cbit8.getBit(segs[i], j) != topBit) {
                        return (segs.length - i) * BITS - j + 1;
                    }
                }
            }
        }
        return 2;
    }

    private static int compareTo(byte[] iSegs, byte[] jSegs) {
        boolean iLt0 = iSegs[0] < 0;
        boolean jLt0 = jSegs[0] < 0;
        if (iLt0 && !jLt0) {
            return -1;
        }
        if (!iLt0 && jLt0) {
            return 1;
        }

        int iSize = size(iSegs);
        int jSize = size(jSegs);

        if (iSize != jSize) {
            return (iSize > jSize == iLt0) ? -1 : 1;
        }

        int iStart = iSegs.length - iSize;
        int jStart = jSegs.length - jSize;
        while (iStart < iSegs.length && jStart < jSegs.length) {
            if (iSegs[iStart] != jSegs[jStart]) {
                return (iSegs[iStart] > jSegs[jStart] == iLt0)
                        ? -1 : 1;
            }
            ++iStart;
            ++jStart;
        }
        return 0;
    }

    private static byte[] getBytes(int value) {
        final int bytes = Math.max(32 / BITS, 1);
        return ByteBuffer.allocate(bytes).putInt(value).array();
    }

    public static BitInt imul(BitInt multiplicand, BitInt multiplier) {
        boolean topBit = multiplicand.segs[0] < 0 ^ multiplier.segs[0] < 0;

        BitInt i = multiplicand;
        if (multiplicand.segs[0] < 0) {
            i = multiplicand.copy();
            i.neg();
        }

        BitInt j = multiplier;
        if (multiplier.segs[0] < 0) {
            j = multiplier.copy();
            j.neg();
        }

        BitInt product = mul(i, j);
        if (!topBit) {
            product.neg();
        }
        return product;
    }

    private static void mov(byte[] dst, byte[] src) {
        int n = Math.min(dst.length, src.length);

        int i = src.length - n;
        int j = dst.length - n;
        while (n-- > 0) {
            dst[j++] = src[i++];
        }

        if (dst.length > src.length) {
            Arrays.fill(dst, 0, dst.length - src.length,
                    (byte) (src[0] < 0 ? -1 : 0));
        }
    }

    public static BitInt mul(BitInt multiplicand, BitInt multiplier) {
        assert multiplicand != null;
        assert multiplier != null;
        BitInt product = multiplicand.copySize();
        int start = multiplier.segs.length - size(multiplier.segs);
        for (int j = multiplier.segs.length - 1; j >= start; --j) {
            if (Cbit8.getBit(multiplier.segs, j)) {
                int i = multiplier.segs.length * BITS - 1 - j;
                BitInt a = multiplicand.copy();
                a.shiftL(i);
                product.add(a);
            }
        }
        return product;
    }

    /**
     * find the min number of segments that this BigInt costs
     */
    private static int size(byte[] segs) {
        byte impliedValue = (byte) (segs[0] < 0 ? -1 : 0);
        for (int i = 0; i < segs.length; ++i) {
            if (segs[i] != impliedValue) {
                return segs.length - i;
            }
        }
        return 1;
    }

    public static BitInt wrap(byte[] segs) {
        return new BitInt(segs);
    }

    private boolean CF;

    private BitInt RV;

    private final byte[] segs;

    public BitInt(BitInt o) {
        this.segs = Arrays.copyOf(o.segs, o.segs.length);
    }

    private BitInt(byte[] segs) {
        this.segs = segs;
    }

    public BitInt(int numBits) {
        assert numBits > 0;
        if (numBits < BITS) {
            numBits = BITS;
        }
        int size = (numBits + BITS - 1) / BITS;
        segs = new byte[size];
    }

    private void adc(byte value) {
        final byte impliedValue = (byte) (value < 0 ? -1 : 0);
        for (int i = segs.length - 1; i >= 0; i--) {
            segs[i] = adc(segs[i], value);
            // accelerate
            if (impliedValue == -1 && CF) {
                break;
            } else if (impliedValue == 0 && !CF) {
                break;
            }
            value = impliedValue;
        }
    }

    private byte adc(byte augend, byte addend) {
        byte sum = (byte) (augend + addend + (CF ? 1 : 0));

        if (augend == 0 || addend == 0) {
            CF = false;
        } else if (augend > 0 && addend > 0) {
            CF = false;
        } else if (augend < 0 && addend < 0) {
            CF = true;
        } else if (sum >= 0) {
            CF = true;
        } else {
            CF = false;
        }

        return sum;
    }

    public void add(BitInt o) {
        add(o.segs);
    }

    public void add(byte value) {
        adc(value);
    }

    private void add(byte[] src) {
        CF = false;
        byte[] dst = segs;
        int impliedValue = src[0] < 0 ? -1 : 0;
        for (int i = dst.length - 1; i >= 0; i--) {
            int j = src.length - dst.length + i;
            int value = j >= 0 ? src[j] : impliedValue;
            if (value == -1 && CF) {
                return;
            } else if (value == 0 && !CF) {
                return;
            }
            dst[i] = adc(src[i], (byte) value);
        }
    }

    public void add(int value) {
        add(getBytes(value));
    }

    @Override
    public int compareTo(BitInt o) {
        return compareTo(segs, o.segs);
    }

    public BitInt copy() {
        return new BitInt(this);
    }

    public BitInt copySize() {
        final int size = segs.length * BITS;
        byte[] segs = new byte[size];
        Arrays.fill(segs, (byte) 0);
        return new BitInt(segs);
    }

    public BitInt div(final BitInt divisor) {
        BitInt dividend = this;
        BitInt quotient = dividend.copySize();
        BitInt remainder = dividend.copySize();
        int jBitSize = bitSize(divisor.segs);
        int offset = bitSize(remainder.segs) - jBitSize;
        while (offset >= 0) {
            BitInt j = remainder.copySize();
            j.mov(divisor);
            j.shiftL(offset);
            if (remainder.compareTo(j) > 0) {
                remainder.sub(j);
                BitInt i = quotient.copySize();
                i.mov(1);
                i.shiftL(offset);
                quotient.add(i);
                offset = bitSize(remainder.segs) - jBitSize;
            } else {
                offset--;
            }
        }
        RV = remainder;
        return quotient;
    }

    public BitInt idiv(BitInt divisor) {
        BitInt dividend = this;
        boolean topBit = dividend.segs[0] < 0 ^ divisor.segs[0] < 0;
        if (dividend.segs[0] < 0) {
            dividend = dividend.copy();
            dividend.neg();
        }
        if (divisor.segs[0] < 0) {
            divisor = divisor.copy();
            divisor.neg();
        }
        BitInt quotient = dividend.div(divisor);
        if (topBit) {
            quotient.neg();
        }
        return quotient;
    }

    public BitInt imod(BitInt divisor) {
        BitInt remainder = mod(divisor);
        if (segs[0] < 0) {
            remainder.neg();
        }
        return remainder;
    }

    public void inc() {
        add((byte) 1);
    }

    public BitInt mod(BitInt divisor) {
        div(divisor);
        return RV.copy();
    }

    public void mov(BitInt o) {
        mov(segs, o.segs);
    }

    public void mov(int value) {
        mov(segs, getBytes(value));
    }

    /**
     * additive inverse
     */
    public void neg() {
        not();
        inc();
    }

    /**
     * bit-wise not
     */
    public void not() {
        for (int i = 0; i < segs.length; ++i) {
            segs[i] = (byte) ~segs[i];
        }
    }

    private void shiftL(int offset) {
        Cbit8.shiftL(segs, offset);
    }

    // BigInt sub(BigInt minuend, BigInt subtrahend)
    public void sub(BitInt subtrahend) {
        BitInt o = subtrahend.copy();
        o.neg();
        add(o);
    }

    public void sub(int value) {
        add(-value);
    }
}
