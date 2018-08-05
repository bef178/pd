package libjava.io;

import java.util.PrimitiveIterator.OfInt;

public interface Pullable {

    public static final int E_EOF = -1;

    public static Pullable wrap(CharSequence cs) {

        return new Pullable() {

            private OfInt it = cs.codePoints().iterator();

            @Override
            public int pull() {
                if (it.hasNext()) {
                    return it.nextInt();
                }
                return E_EOF;
            }
        };
    }

    /**
     * Return non-negative as value, or negative as status.<br/>
     * Especially, will return E_EOF when reaches the end of stream.<br/>
     */
    public int pull();
}
