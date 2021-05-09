package pd.fenc;

/**
 * It often requires "pre-reading" few symbols to speculate which token parser should be invoked.<br>
 * While, we know stream cannot move backward physically.<br/>
 * This reader caches few recent-meet characters thus supports "as-if" move backward.<br/>
 */
public class CharReader implements IReader {

    private class Recent {

        private final int[] a;

        private int n;

        public Recent(int capacity) {
            assert capacity > 0;
            a = new int[capacity];
            n = 0;
        }

        public void add(int value) {
            a[n++ % a.length] = value;
        }

        public int capacity() {
            return a.length;
        }

        public int getLast(int i) {
            assert i > 0 && i <= a.length;
            assert i <= n;
            return a[(n - i) % a.length];
        }
    }

    public static CharReader wrap(CharSequence cs) {
        return new CharReader(ICharReader.wrap(cs));
    }

    private final ICharReader src;

    private int pos;

    private final Recent recent = new Recent(2);

    private int backOffset;

    public CharReader(ICharReader src) {
        this(src, 0);
    }

    public CharReader(ICharReader src, int pos) {
        this.src = src;
        this.pos = pos;
        this.backOffset = 0;
    }

    public void eatOrThrow(int expected) {
        int ch = hasNext() ? next() : EOF;
        if (ch != expected) {
            throw new ParsingException(expected, ch);
        }
    }

    public void eatWhitespaces() {
        while (hasNext()) {
            int ch = next();
            if (!Cascii.isWhitespace(ch)) {
                moveBack();
                return;
            }
        }
    }

    @Override
    public boolean hasNext() {
        if (backOffset > 0) {
            return true;
        } else {
            return src.hasNext();
        }
    }

    public boolean moveBack() {
        return moveBack(1);
    }

    public boolean moveBack(int numSteps) {
        if (backOffset + numSteps <= recent.capacity()) {
            backOffset += numSteps;
            return true;
        }
        return false;
    }

    @Override
    public int next() {
        if (backOffset > 0) {
            return recent.getLast(backOffset--);
        }
        int value = src.next(); // let it throw
        recent.add(value);
        pos++;
        return value;
    }

    public int position() {
        return pos - backOffset;
    }
}
