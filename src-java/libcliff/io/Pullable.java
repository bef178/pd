package libcliff.io;

import java.util.PrimitiveIterator.OfInt;

public interface Pullable {

    static final int EOF = -1;

    public static Pullable wrap(CharSequence cs) {

        return new Pullable() {

            private OfInt it = cs.codePoints().iterator();

            @Override
            public int pull() {
                if (it.hasNext()) {
                    return it.nextInt();
                }
                return EOF;
            }
        };
    }

    /**
     * Return non-negative as code point or binary byte, or negative as status code<br/>
     * Especially, will always return EOF when reaches the end of stream.<br/>
     * Wont throw exception.
     */
    public int pull();
}
