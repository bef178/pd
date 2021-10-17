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
public interface IReader {

    public static final int EOF = -1;

    public static IReader asciiStream(InputStream input) {

        return new IReader() {

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

    public static IReader octetStream(byte[] src) {
        return octetStream(src, 0, src.length);
    }

    /**
     * wrap to an int32 stream having value in [0, 0xFF]
     */
    public static IReader octetStream(byte[] src, int i, int j) {
        assert src != null;
        assert i >= 0;
        assert j > i && j < src.length;

        return new IReader() {

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

    public static IReader octetStream(InputStream input) {

        return new IReader() {

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
    public static IReader unicodeStream(CharSequence cs) {
        // pain: two or more intermediate objects
        // gain: existing facilities and avoid seldom O(n) on chatAt(i)
        return unicodeStream(cs.codePoints().iterator());
    }

    private static IReader unicodeStream(OfInt it) {

        return new IReader() {

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

    public int position();
}
