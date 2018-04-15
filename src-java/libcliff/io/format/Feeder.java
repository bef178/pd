package libcliff.io.format;

import libcliff.io.Pullable;

/**
 * Often, the parser requires at least one symbol to infer which token would
 * be expected. The invoked lower scanner must takes both the symbol and the
 * rest of stream to produce a legal token. To merge those two, we need a
 * last() or peek() or back().<br/>
 * <br/>
 * Please don't assume a stream is physically able to move backward.<br/>
 * &emsp;- if using last(), user cannot get all symbols by continuously
 * calling pull(), and hasLast() must be added.<br/>
 * &emsp;- if using peek(), every time peek() following pull(), the stream
 * indeed moves one step - one symbol will be lost if the stream being held
 * outside calls pull().<br/>
 * &emsp;- if using back(), user should be aware only one backward step could
 * succeed - it is also the C way.
 */
public class Feeder implements Pullable {

    public static Feeder wrap(CharSequence cs) {
        return new Feeder(Pullable.wrap(cs));
    }

    private boolean backed = false;

    private int last = -1;

    private Pullable upstream = null;

    private int position = 0;

    public Feeder(int last, Pullable rest) {
        this.backed = true;
        this.last = last;
        this.upstream = rest;
    }

    public Feeder(Pullable upstream) {
        this.backed = false;
        this.last = -1;
        this.upstream = upstream;
    }

    public boolean back() {
        if (!backed && last >= 0) {
            backed = true;
            --position;
            return true;
        }
        return false;
    }

    public int next() {
        if (backed) {
            backed = false;
            ++position;
            return last;
        } else if (last >= 0 || position == 0) {
            last = this.upstream.pull();
            ++position;
            return last;
        } else {
            return this.upstream.pull();
        }
    }

    public int position() {
        return position;
    }

    @Override
    public int pull() {
        return next();
    }
}
