package pd.fenc;

import pd.util.AsciiExtension;

/**
 * It often requires "pre-reading" few symbols to speculate which token parser should be invoked.<br>
 * While, we know stream cannot move backward physically.<br/>
 * This reader caches few recent-meet characters thus supports "as-if" move backward.<br/>
 */
public class CharReader implements Int32Provider {

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

    private final Int32Provider src;

    private final Recent recent = new Recent(2);

    private int backOffset;

    public CharReader(CharSequence cs) {
        this(Int32Provider.wrap(cs));
    }

    public CharReader(Int32Provider src) {
        this.src = src;
        this.backOffset = 0;
    }

    public void eatOrThrow(int expected) {
        if (!tryEat(expected)) {
            throw new ParsingException(expected, next());
        }
    }

    public void eatWhitespaces() {
        while (hasNext()) {
            int ch = next();
            if (!AsciiExtension.isWhitespace(ch)) {
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
        if (value == EOF) {
            throw new ParsingException();
        }
        recent.add(value);
        return value;
    }

    @Override
    public int position() {
        return src.position() - backOffset;
    }

    /**
     * will stop in front of unexpected value
     */
    public boolean tryEat(int expected) {
        if (hasNext()) {
            if (next() == expected) {
                return true;
            } else {
                moveBack();
                return false;
            }
        } else {
            if (expected == EOF) {
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean tryEat(String expecteds) {
        Int32Provider it = Int32Provider.wrap(expecteds);
        while (it.hasNext()) {
            int expected = it.next();
            if (!tryEat(expected)) {
                return false;
            }
        }
        return true;
    }
}
