package libjava.io.format;

import libjava.io.Pullable;

/**
 * Often, a parser requires at least one symbol to speculate which token could
 * be expected. The underlying scanner must takes both the symbol and the
 * rest of stream to produce a legal token. To merge those two, the parser need
 * last() or peek() or back().<br/>
 * <br/>
 * Please don't suppose a stream is physically able to move backward.<br/>
 * &emsp;- if using last(), user cannot get all symbols by continuously
 * calling pull(), and hasLast() must be added.<br/>
 * &emsp;- if using peek(), every time peek() following pull(), the stream
 * indeed moves one step - one symbol will be lost if the stream being held
 * outside calls pull().<br/>
 * &emsp;- if using back(), user should be aware only one backward step could
 * succeed - it is also the C way.<br/>
 */
public class IntScanner implements Pullable {

    public static IntScanner wrap(CharSequence cs) {
        return new IntScanner(Pullable.wrap(cs));
    }

    private boolean backed;

    private int last;

    private Pullable upstream;

    public IntScanner(int last, Pullable rest) {
        this.backed = true;
        this.last = last;
        this.upstream = rest;
    }

    public IntScanner(Pullable upstream) {
        this.backed = false;
        this.last = 0;
        this.upstream = upstream;
    }

    public boolean back() {
        if (backed) {
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
        return last = this.upstream.pull();
    }

    @Override
    public int pull() {
        return next();
    }
}
