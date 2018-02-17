package libcliff.io;

public interface PullStream extends Pullable {

    /**
     * return the opening end of the pipe<br/>
     * <br/>
     * Not perfect design. The Ideal would be:<br/>
     * PullStream join(PullStream... streams, Pullable src)<br/>
     * which would never work in java.
     */
    public static PullStream join(Pullable... streams) {
        assert streams != null && streams.length > 0;
        Pullable src = streams[streams.length - 1];
        for (int i = streams.length - 2; i >= 0; --i) {
            src = ((PullStream) streams[i]).join(src);
        }
        return (PullStream) src;
    }

    public static int pull(Pullable... streams) {
        return join(streams).pull();
    }

    /**
     * return this stream
     */
    public PullStream join(Pullable upstream);

    /**
     * return ch as an Unicode character in [0, 0x10FFFF] or -1 iff reaches the end
     */
    @Override
    public int pull();
}
