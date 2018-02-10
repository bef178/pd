package libcliff.io;

import libcliff.io.codec.ParsingException;

public abstract class BytePipe implements Pullable, Pushable {

    protected Pushable downstream;

    protected Pullable upstream;

    protected BytePipe() {
        // dummy
    }

    /**
     * return ch as value in [0, 0xFF] or -1 iff reaches the end
     */
    @Override
    public int pull() {
        return pullByte() & 0xFF;
    }

    protected abstract int pullByte();

    /**
     * accept ch as value in [0, 0xFF]<br/>
     * <br/>
     * return the number of bytes finally pushed
     */
    @Override
    public int push(int ch) {
        if (ch >= 0) {
            return pushByte(ch & 0xFF);
        }
        throw new ParsingException();
    }

    protected abstract int pushByte(int aByte);

    public BytePipe setDownstream(Pushable downstream) {
        this.downstream = downstream;
        return this;
    }

    public BytePipe setUpstream(Pullable upstream) {
        this.upstream = upstream;
        return this;
    }
}
