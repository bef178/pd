package pd.fenc;

import static pd.fenc.IReader.EOF;

import java.util.PrimitiveIterator.OfInt;

public interface ICharReader {

    public static ICharReader wrap(CharSequence cs) {
        return wrap(cs.codePoints().iterator());
    }

    public static ICharReader wrap(OfInt it) {

        return new ICharReader() {

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public int next() {
                return it.hasNext() ? it.nextInt() : EOF;
            }
        };
    }

    public boolean hasNext();

    /**
     * provide an unicode character
     */
    public int next();
}
