package pd.io.format;

import pd.io.Pullable;

/**
 * Often, a parser requires at least one symbol to speculate which token could
 * be expected and then forward the symbol together with the rest stream to the
 * corresponding token parser. For an uniform API, the stream is ought to
 * provide last(), peek() or back() besides next().<br/>
 * <br/>
 * A general stream never moves backward, so the difference is subtle:<br/>
 * &emsp;- last(): dual read method; no cursor move; may throw; but parsers
 *      may doubt of starting from last() or next()<br/>
 * &emsp;- peek(): dual read method; appears no cursor move but actually is;
 *      may throw<br/>
 * &emsp;- back(): unique read method; appears cursor move but actually not; no
 *      throw; just the C way<br/>
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
