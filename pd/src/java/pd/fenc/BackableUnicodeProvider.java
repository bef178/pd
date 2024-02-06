package pd.fenc;

public class BackableUnicodeProvider implements UnicodeProvider {

    private final UnicodeProvider src;

    private final Recent recent;

    private int nBack;

    public BackableUnicodeProvider(CharSequence cs) {
        this(UnicodeProvider.wrap(cs));
    }

    public BackableUnicodeProvider(UnicodeProvider src) {
        this (src, 2);
    }

    public BackableUnicodeProvider(UnicodeProvider src, int backCapacity) {
        if (backCapacity < 1) {
            throw new IllegalArgumentException("E: expected positive backCapacity, actual " + backCapacity);
        }
        this.src = src;
        this.recent = new Recent(backCapacity);
        this.nBack = 0;
    }

    @Override
    public boolean hasNext() {
        if (nBack > 0) {
            return true;
        } else {
            return src.hasNext();
        }
    }

    @Override
    public int next() {
        if (nBack > 0) {
            return recent.get(-nBack--);
        }
        int value = src.next(); // let it throw if no next value
        recent.add(value);
        return value;
    }

    @Override
    public int position() {
        return src.position() - nBack;
    }

    public void back() {
        if (!tryBack()) {
            throw new ParsingException("E: failed to back");
        }
    }

    public boolean tryBack() {
        if (nBack + 1 > recent.capacity()) {
            return false;
        }
        nBack++;
        return true;
    }

    static class Recent {

        private final int[] a;

        private int n;

        public Recent(int capacity) {
            a = new int[capacity];
            n = 0;
        }

        public void add(int value) {
            a[n++ % a.length] = value;
        }

        public int capacity() {
            return a.length;
        }

        public int get(int i) {
            if (i >= 0 || i < -a.length || i < -n) {
                throw new IndexOutOfBoundsException();
            }
            return a[(n + i) % a.length];
        }
    }
}
