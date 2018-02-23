package libcliff.io;

public interface PullablePipe extends Pullable {

    /**
     * return the opening end of the pipe<br/>
     * <br/>
     * Not perfect design. The Ideal would be:<br/>
     * PullStream join(PullStream... streams, Pullable src)<br/>
     * which would never work in java.
     */
    public static PullablePipe join(PullablePipe pipe, Pullable... streams) {
        if (streams.length > 0) {
            Pullable src = streams[streams.length - 1];
            for (int i = streams.length - 2; i >= 0; --i) {
                src = ((PullablePipe) streams[i]).join(src);
            }
            pipe.join(src);
        }
        return pipe;
    }

    /**
     * return this stream
     */
    public PullablePipe join(Pullable upstream);
}
