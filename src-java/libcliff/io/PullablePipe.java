package libcliff.io;

public interface PullablePipe extends Pullable {

    /**
     * return the opening end of the pipe<br/>
     * <br/>
     * Not perfect design. The Ideal would be:<br/>
     * PullStream join(PullStream... streams, Pullable src)<br/>
     * which would never work in java.
     */
    public static PullablePipe join(Pullable... streams) {
        assert streams != null && streams.length > 0;
        Pullable src = streams[streams.length - 1];
        for (int i = streams.length - 2; i >= 0; --i) {
            src = ((PullablePipe) streams[i]).join(src);
        }
        return (PullablePipe) src;
    }

    public static int pull(Pullable... streams) {
        return join(streams).pull();
    }

    /**
     * return this stream
     */
    public PullablePipe join(Pullable upstream);

    /**
     * return ch as in [0, 0x10FFFF] or -1 iff reaches the end
     */
    @Override
    public int pull();
}
