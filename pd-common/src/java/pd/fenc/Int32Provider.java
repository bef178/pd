package pd.fenc;

import java.io.InputStream;
import java.util.PrimitiveIterator.OfInt;

import pd.util.InputStreamExtension;

public interface Int32Provider {

    boolean hasNext();

    int next();

    int position();

    /**
     * values in [-0x80,0x7F]
     */
    static Int32Provider wrap(InputStream inputStream) {
        OfInt ofInt = InputStreamExtension.toIterator(inputStream);
        return new Int32Provider() {

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

    /**
     * values in [0,0x10FFFF]
     */
    static Int32Provider wrap(CharSequence cs) {
        return wrap(cs.codePoints().iterator());
    }

    static Int32Provider wrap(OfInt ofInt) {
        return new Int32Provider() {

            private int pos = 0;

            @Override
            public boolean hasNext() {
                return ofInt.hasNext();
            }

            @Override
            public int next() {
                int value = ofInt.nextInt();
                pos++;
                return value;
            }

            @Override
            public int position() {
                return pos;
            }
        };
    }
}
