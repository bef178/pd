package pd.fenc;

import pd.util.IntQueue;

public class BackableUnicodeProvider implements UnicodeProvider {

    private final UnicodeProvider src;

    private final IntQueue recent;

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
        this.recent = new IntQueue(backCapacity);
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
            return recent.get(--nBack);
        }
        int value = src.next(); // let it throw if no next value
        recent.pushHead(value);
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
        if (nBack + 1 > recent.size()) {
            return false;
        }
        nBack++;
        return true;
    }
}
