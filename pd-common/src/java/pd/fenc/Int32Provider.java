package pd.fenc;

import static pd.fenc.Util.checkAscii;
import static pd.fenc.Util.checkByte;

import java.io.IOException;
import java.io.InputStream;
import java.util.PrimitiveIterator.OfInt;

/**
 * no confusion, this is just a provider of int32<br/>
 * value can be octet(byte), ascii, surrogate, unicode, ..., any int32<br>
 * better to add a check before taking the value<br/>
 */
public interface Int32Provider {

    public static final int EOF = -1;

    public static Int32Provider asciiStream(InputStream input) {

        return new Int32Provider() {

            private int pos = 0;

            @Override
            public boolean hasNext() {
                try {
                    return input.available() > 0;
                } catch (IOException e) {
                    throw new ParsingException(e);
                }
            }

            @Override
            public int next() {
                try {
                    int value = checkAscii(input.read());
                    pos++;
                    return value;
                } catch (IOException e) {
                    throw new ParsingException(e);
                }
            }

            @Override
            public int position() {
                return pos;
            }
        };
    }

    public static Int32Provider octetStream(byte[] src) {
        return octetStream(src, 0, src.length);
    }

    /**
     * wrap to an int32 stream having value in [0, 0xFF]
     */
    public static Int32Provider octetStream(byte[] src, int i, int j) {
        assert src != null;
        assert i >= 0;
        assert j > i && j < src.length;

        return new Int32Provider() {

            private int offset = i;

            @Override
            public boolean hasNext() {
                return offset < j;
            }

            @Override
            public int next() {
                return src[offset++] & 0xFF;
            }

            @Override
            public int position() {
                return offset - i;
            }
        };
    }

    public static Int32Provider octetStream(InputStream input) {

        return new Int32Provider() {

            private int pos = 0;

            @Override
            public boolean hasNext() {
                try {
                    return input.available() > 0;
                } catch (IOException e) {
                    throw new ParsingException(e);
                }
            }

            @Override
            public int next() {
                try {
                    int value = checkByte(input.read());
                    pos++;
                    return value;
                } catch (IOException e) {
                    throw new ParsingException(e);
                }
            }

            @Override
            public int position() {
                return pos;
            }
        };
    }

    /**
     * wrap to an int32 stream having value in [0, 0x10FFFF]
     */
    public static Int32Provider unicodeStream(CharSequence cs) {
        // pain: two or more intermediate objects
        // gain: existing facilities and avoid seldom O(n) on chatAt(i)
        return unicodeStream(cs.codePoints().iterator());
    }

    public static Int32Provider unicodeStream(OfInt it) {

        return new Int32Provider() {

            private int pos = 0;

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public int next() {
                int value = it.nextInt();
                pos++;
                return value;
            }

            @Override
            public int position() {
                return pos;
            }
        };
    }

    public boolean hasNext();

    public int next();

    /**
     * the next position it will read
     */
    public int position();
}
