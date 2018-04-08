package libcliff.io;

public interface PullablePipe extends Pullable {

    /**
     * return the opening end of the pipe<br/>
     */
    public static <T extends PullablePipe> T join(T pipe,
            Pullable... streams) {
        Pullable last = pipe;
        for (Pullable src : streams) {
            if (last instanceof PullablePipe) {
                ((PullablePipe) last).join(src);
                last = src;
            } else {
                throw new ParsingException();
            }
        }
        return pipe;
    }

    /**
     * return this stream
     */
    public PullablePipe join(Pullable upstream);
}
