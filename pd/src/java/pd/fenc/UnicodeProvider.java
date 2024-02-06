package pd.fenc;

import java.util.PrimitiveIterator;

public interface UnicodeProvider extends Int32Provider {

    /**
     * values in [0,0x10FFFF]
     */
    static UnicodeProvider wrap(CharSequence cs) {
        return wrap(cs.codePoints().iterator());
    }

    static UnicodeProvider wrap(PrimitiveIterator.OfInt ofInt) {

        return new UnicodeProvider() {

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
