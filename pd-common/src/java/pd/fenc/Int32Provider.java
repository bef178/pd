package pd.fenc;

import java.io.InputStream;
import java.util.PrimitiveIterator.OfInt;

import pd.util.InputStreamExtension;

public interface Int32Provider {

    boolean hasNext();

    int next();

    int position();

    /**
     * values in [0,0xFF]
     */
    static Int32Provider wrap(InputStream inputStream) {
        return wrap(InputStreamExtension.toIterator(inputStream));
    }

    /**
     * values in [0,0x10FFFF]
     */
    static Int32Provider wrap(CharSequence cs) {
        return wrap(cs.codePoints().iterator());
    }

    static Int32Provider wrap(OfInt it) {

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
}
