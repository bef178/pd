package libcliff.io;

import java.util.PrimitiveIterator.OfInt;

public interface Pullable {

    static final int E_EOF = -1;

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

    public int pull();
}
