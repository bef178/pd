package libcliff.io;

public interface PushablePipe extends Pushable {

    /**
     * return the opening end of the pipe<br/>
     */
    public static PushablePipe join(PushablePipe pipe, Pushable... streams) {
        if (streams.length > 0) {
            Pushable dst = streams[streams.length - 1];
            for (int i = streams.length - 2; i >= 0; --i) {
                dst = ((PushablePipe) streams[i]).join(dst);
            }
            pipe.join(dst);
        }
        return pipe;
    }

    /**
     * return this stream
     */
    public PushablePipe join(Pushable downstream);
}
