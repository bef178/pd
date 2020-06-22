package pd.encoding;

import java.util.PrimitiveIterator.OfInt;

public class UnicodeProvider {

    public static final int EOF = -1;

    public static UnicodeProvider wrap(byte[] utf8) {
        return wrap(new String(utf8));
    }

    public static UnicodeProvider wrap(byte[] utf8, int start, int end) {
        return wrap(new String(utf8, start, end - start));
    }

    public static UnicodeProvider wrap(CharSequence cs) {
        return new UnicodeProvider(cs.codePoints().iterator());
    }

    private final OfInt it;

    private int position;

    private final int[] recents = new int[1];

    private int backOffset = 0;

    private UnicodeProvider(OfInt it) {
        this.it = it;
    }

    /**
     * return num of back steps it actually takes
     */
    public int back(int n) {
        if (n < 0) {
            return 0;
        }

        int i = n + backOffset;
        if (i > recents.length) {
            i = recents.length;
        }
        if (i > position) {
            i = position;
        }
        int d = i - backOffset;
        backOffset = i;
        return d;
    }

    private boolean hasNext() {
        if (backOffset > 0) {
            return true;
        } else {
            return it != null && it.hasNext();
        }
    }

    public int next() {
        if (backOffset > 0) {
            if (backOffset <= recents.length && backOffset < position) {
                int value = recents[(position - backOffset) % recents.length];
                backOffset--;
                return value;
            }
            throw new IllegalArgumentException();
        }

        if (!hasNext()) {
            return EOF;
        }

        int value = it.nextInt();
        recents[position++ % recents.length] = value;
        return value;
    }

    public int position() {
        return position - backOffset;
    }
}
