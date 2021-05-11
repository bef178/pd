package pd.fenc;

import static pd.fenc.Util.checkByte;

import java.io.IOException;
import java.io.InputStream;

public interface IReader {

    public static final int EOF = -1;

    /**
     * ByteProvider
     */
    public static IReader wrap(byte[] src) {
        return wrap(src, 0, src.length);
    }

    /**
     * ByteProvider
     */
    public static IReader wrap(byte[] src, int i, int j) {

        return new IReader() {

            private int pos = i;

            @Override
            public boolean hasNext() {
                return pos < j;
            }

            @Override
            public int next() {
                return src[pos++];
            }

            @Override
            public int position() {
                return pos;
            }
        };
    }

    /**
     * StreamReader, ByteProvider
     */
    public static IReader wrap(InputStream input) {

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

    public boolean hasNext();

    /**
     * returned value should be checked before use
     */
    public int next();

    public int position();
}
