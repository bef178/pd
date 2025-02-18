package pd.fenc;

public interface UnicodeProvider {

    boolean hasNext();

    /**
     * read and move;
     * values in [0, 0x10FFFF]
     */
    int next();

    boolean hasPrev();

    int prev();

    int position();

    default void back() {
        prev();
    }

    static UnicodeProvider wrap(CharSequence cs) {

        return new UnicodeProvider() {

            final int[] a = cs == null ? new int[0] : cs.codePoints().toArray();
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < a.length;
            }

            @Override
            public int next() {
                return a[i++];
            }

            @Override
            public boolean hasPrev() {
                return i > 0;
            }

            @Override
            public int prev() {
                return a[--i];
            }

            @Override
            public int position() {
                return i;
            }
        };
    }
}
