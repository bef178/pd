package libcliff.io;

import java.util.PrimitiveIterator.OfInt;

public interface Pullable {

    public static Pullable wrap(CharSequence cs) {

        return new Pullable() {

            private OfInt it = cs.codePoints().iterator();

            @Override
            public int pull() {
                if (it.hasNext()) {
                    return it.nextInt();
                }
                return -1;
            }
        };
    }

    /**
     * return -1 iff reaches the end
     */
    public int pull();
}
