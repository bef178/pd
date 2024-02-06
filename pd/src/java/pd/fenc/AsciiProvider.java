package pd.fenc;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

import static pd.util.AsciiExtension.EOF;

public interface AsciiProvider extends UnicodeProvider {

    /**
     * values in [-0x80,0x7F]
     */
    static AsciiProvider wrap(InputStream inputStream) {

        return new AsciiProvider() {

            private final PrimitiveIterator.OfInt ofInt = toIterator(inputStream);

            private int pos = 0;

            @Override
            public boolean hasNext() {
                return ofInt.hasNext();
            }

            @Override
            public int next() {
                int value = (byte) ofInt.nextInt();
                pos++;
                return value;
            }

            @Override
            public int position() {
                return pos;
            }
        };
    }

    static PrimitiveIterator.OfInt toIterator(InputStream inputStream) {

        return new PrimitiveIterator.OfInt() {

            private final int NO_VALUE = -9;

            private int nextValue = NO_VALUE;

            @Override
            public boolean hasNext() {
                if (nextValue == NO_VALUE) {
                    try {
                        nextValue = inputStream.read();
                    } catch (IOException e) {
                        throw new ParsingException(e);
                    }
                }
                return nextValue != EOF;
            }

            @Override
            public int nextInt() {
                if (nextValue == NO_VALUE) {
                    try {
                        nextValue = inputStream.read();
                    } catch (IOException e) {
                        throw new ParsingException(e);
                    }
                }
                if (nextValue == EOF) {
                    throw new NoSuchElementException();
                }
                int result = nextValue;
                nextValue = NO_VALUE;
                return result;
            }
        };
    }
}
