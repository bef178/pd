package pd.io.format;

import pd.io.Pullable;

/**
 * Often, a parser requires at least one symbol to speculate which token
 * could be expected. The underlying scanner must takes both the symbol and
 * the rest of stream to produce a legal token. To merge those two, the
 * stream ought to provide last(), peek() or back().<br/>
 * <br/>
 * These 3 are almost equivalent with a one-slot cache - please don't suppose
 * a stream being able to go backward physically. The difference is subtle:<br/>
 * &emsp;- last() and peek() are similar: hide movement and give the value or
 * throw error state<br/>
 * &emsp;- back() the C way is just movement: a boolean shows the state
 * without exception<br/>
 */
public class IntScanner implements Pullable {

    public static IntScanner wrap(CharSequence cs) {
        return new IntScanner(Pullable.wrap(cs));
    }

    private boolean backed; // uses last

    private int last;

    private Pullable upstream;

    private int offset;

    public IntScanner(int first, Pullable rest) {
        this.backed = true;
        this.last = first;
        this.upstream = rest;
        this.offset = 1;
    }

    public IntScanner(Pullable upstream) {
        this.backed = false;
        this.last = 0;
        this.upstream = upstream;
        this.offset = 0;
    }

    public boolean back() {
        if (backed || offset == 0) {
            return false;
        }

        backed = true;
        return true;
    }

    public int next() {
        if (backed) {
            backed = false;
            return last;
        }
        ++offset;
        return last = this.upstream.pull();
    }

    @Override
    public int pull() {
        return next();
    }
}
