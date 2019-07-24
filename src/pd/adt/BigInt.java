package pd.adt;

import java.nio.ByteBuffer;
import java.util.Arrays;

import pd.ctype.Cbit8;

/**
 * - wrap continues bits into a big integer<br/>
 * - will not implicitly change capacity<br/>
 * - api name after asm<br/>
 */
public class BigInt implements Comparable<BigInt> {

    private static final int BITS = 8;

    private static int compareTo(byte[] iBytes, byte[] jBytes) {
        assert iBytes != null;
        assert jBytes != null;

        if ((iBytes[0] < 0) != (jBytes[0] < 0)) {
            // different sign
            return iBytes[0] - jBytes[0] < 0 ? -1 : 1;
        }

        int iOffset = getFirstBitOffset(iBytes);
        int iLength = iBytes.length - iOffset;

        int jOffset = getFirstBitOffset(jBytes);
        int jLength = jBytes.length - jOffset;

        if (iLength != jLength) {
            return (iLength > jLength) == (iBytes[0] < 0) ? -1 : 1;
        }

        final int BITS = 8;
        for (int i = iOffset / BITS, j = jOffset
                / BITS; i < iBytes.length; ++i, ++j) {
            if (iBytes[i] != jBytes[j]) {
                return iBytes[i] - jBytes[j];
            }
        }
        return 0;
    }

    private static byte[] getBytes(int value) {
        return ByteBuffer.allocate(32 / 8).putInt(value).array();
    }

    /**
     * return the first bit that different from the sign bit, in (0, length].<br/>
     * assume big-endian and signed value
     */
    private static int getFirstBitOffset(byte[] bytes) {
        final int BITS = 8;
        boolean signBit = bytes[0] < 0;
        byte impliedByte = (byte) (signBit ? -1 : 0);
        for (int i = 0; i < bytes.length; ++i) {
            if (bytes[i] != impliedByte) {
                for (int j = 0; j < BITS; ++j) {
                    if (Cbit8.getBit(bytes[i], j) != signBit) {
                        return i * BITS + j;
                    }
                }
            }
        }
        return bytes.length * BITS;
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

    public static BigInt wrap(byte[] bytes) {
        return new BigInt(bytes);
    }

    public static BigInt wrap(int value) {
        return wrap(getBytes(value));
    }

    public static BigInt wrapLength(int numBits) {
        assert numBits > 0;
        if (numBits < BITS) {
            numBits = BITS;
        }
        int size = (numBits + BITS - 1) / BITS;
        byte[] bytes = new byte[size];
        Arrays.fill(bytes, (byte) 0);
        return wrap(bytes);
    }

    private boolean CF;

    private BigInt RV;

    private final byte[] segs;

    public BigInt(BigInt o) {
        this.segs = Arrays.copyOf(o.segs, o.segs.length);
    }

    private BigInt(byte[] bytes) {
        this.segs = bytes;
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

    private void adc(byte[] augend, byte addend) {
        final byte impliedValue = (byte) (addend < 0 ? -1 : 0);
        int i = augend.length - 1;
        augend[i] = adc(augend[i], addend);
        while (--i >= 0) {
            // accelerate
            if (impliedValue == -1 && CF) {
                break;
            } else if (impliedValue == 0 && !CF) {
                break;
            }
            augend[i] = adc(augend[i], impliedValue);
        }
    }

    private void adc(byte[] augend, byte[] addend) {
        adc(augend, augend.length, addend);
    }

    private void adc(byte[] augend, int augendEnd, byte[] addend) {
        final byte impliedValue = (byte) (addend[0] < 0 ? -1 : 0);
        for (int i = augendEnd - 1; i >= 0; i--) {
            int j = augendEnd - augend.length + i;
            augend[i] = adc(augend[i], j >= 0 ? addend[j] : impliedValue);
        }
    }

    public void add(BigInt o) {
        CF = false;
        adc(segs, o.segs);
    }

    public void add(byte value) {
        CF = false;
        adc(segs, value);
    }

    public void add(int value) {
        CF = false;
        adc(segs, getBytes(value));
    }

    @Override
    public int compareTo(BigInt o) {
        return compareTo(segs, o.segs);
    }

    public BigInt copy() {
        return new BigInt(this);
    }

    public BigInt copyLength() {
        return wrapLength(segs.length * BITS);
    }

    public BigInt div(final BigInt divisor) {
        BigInt dividend = this;
        BigInt quotient = dividend.copyLength();
        BigInt remainder = dividend.copyLength();
        int jOffset = getFirstBitOffset(divisor.segs);
        int offset = jOffset - getFirstBitOffset(remainder.segs);
        while (offset >= 0) {
            BigInt j = remainder.copyLength();
            j.mov(divisor);
            Cbit8.shiftL(j.segs, offset);
            if (remainder.compareTo(j) > 0) {
                remainder.sub(j);
                BigInt i = quotient.copyLength();
                i.mov(1);
                Cbit8.shiftL(i.segs, offset);
                quotient.add(i);
                offset = jOffset - getFirstBitOffset(remainder.segs);
            } else {
                offset--;
            }
        }
        RV = remainder;
        return quotient;
    }

    public BigInt idiv(BigInt divisor) {
        BigInt dividend = this;
        boolean topBit = dividend.segs[0] < 0 ^ divisor.segs[0] < 0;
        if (dividend.segs[0] < 0) {
            dividend = dividend.copy();
            dividend.neg();
        }
        if (divisor.segs[0] < 0) {
            divisor = divisor.copy();
            divisor.neg();
        }
        BigInt quotient = dividend.div(divisor);
        if (topBit) {
            quotient.neg();
        }
        return quotient;
    }

    public BigInt imod(BigInt divisor) {
        BigInt remainder = mod(divisor);
        if (segs[0] < 0) {
            remainder.neg();
        }
        return remainder;
    }

    public BigInt imul(BigInt multiplicand, BigInt multiplier) {
        boolean topBit = multiplicand.segs[0] < 0 ^ multiplier.segs[0] < 0;

        BigInt i = multiplicand;
        if (multiplicand.segs[0] < 0) {
            i = multiplicand.copy();
            i.neg();
        }

        BigInt j = multiplier;
        if (multiplier.segs[0] < 0) {
            j = multiplier.copy();
            j.neg();
        }

        BigInt product = mul(i, j);
        if (!topBit) {
            product.neg();
        }
        return product;
    }

    public void inc() {
        add((byte) 1);
    }

    public BigInt mod(BigInt divisor) {
        div(divisor);
        return RV.copy();
    }

    public void mov(BigInt o) {
        mov(segs, o.segs);
    }

    public void mov(int value) {
        mov(segs, getBytes(value));
    }

    public BigInt mul(BigInt multiplicand, BigInt multiplier) {
        BigInt product = BigInt.wrapLength(multiplicand.segs.length * BITS);
        mul(multiplicand.segs, multiplier.segs, product.segs);
        return product;
    }

    private void mul(byte[] multiplicand, byte[] multiplier, byte[] product) {
        assert multiplicand != null;
        assert multiplier != null;

        for (int i = 0; i < multiplicand.length; ++i) {
            int iv = multiplicand[multiplicand.length - 1 - i];
            if (iv == 0) {
                continue;
            }
            for (int j = 0; j < multiplier.length; ++i) {
                if (i + j >= multiplicand.length) {
                    continue;
                }

                int jv = multiplier[multiplier.length - 1 - j];
                if (jv == 0) {
                    continue;
                }

                int pv = (iv < 0 ? (256 - iv) : iv)
                        * (jv < 0 ? (256 - jv) : jv);
                CF = false;
                adc(product, i + j, getBytes(pv));
            }
        }
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

    // BigInt sub(BigInt minuend, BigInt subtrahend)
    public void sub(BigInt subtrahend) {
        BigInt o = subtrahend.copy();
        o.neg();
        add(o);
    }

    public void sub(int value) {
        add(-value);
    }
}
