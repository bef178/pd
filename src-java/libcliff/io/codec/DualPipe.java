package libcliff.io.codec;

import libcliff.io.Pullable;
import libcliff.io.PullablePipe;
import libcliff.io.Pushable;
import libcliff.io.PushablePipe;

public abstract class DualPipe implements PullablePipe, PushablePipe {

    private Pullable upstream;

    private Pushable downstream;

    @Override
    public DualPipe join(Pullable upstream) {
        this.upstream = upstream;
        return this;
    }

    @Override
    public DualPipe join(Pushable downstream) {
        this.downstream = downstream;
        return this;
    }

    @Override
    public int pull() {
        return this.upstream.pull();
    }

    @Override
    public int push(int ch) {
        return this.downstream.push(ch);
    }
}
