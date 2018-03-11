package libcliff.io.codec;

import libcliff.io.Pullable;
import libcliff.io.PullablePipe;

/**
 * Always, a symbol from the feeder is necessary to decide which parser to use.
 * The symbol and the feeder both is input stream but appears two parameters.
 * So introduce a pullable who remembers the last symbol it returns.
 * This name, EchoedPullable, for now.
 */
public class EchoedPullablePipe implements PullablePipe {

    private Pullable upstream = null;

    private int last = E_EOF;

    public EchoedPullablePipe() {
        this(E_EOF);
    }

    public EchoedPullablePipe(int last) {
        this.last = last;
    }

    public int echo() {
        return last;
    }

    @Override
    public EchoedPullablePipe join(Pullable upstream) {
        this.upstream = upstream;
        return this;
    }

    @Override
    public int pull() {
        return this.upstream.pull();
    }
}
