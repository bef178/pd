package libcliff.io;

import libcliff.io.codec.ParsingException;

public interface PushablePipe extends Pushable {

    /**
     * return the opening end of the pipe<br/>
     */
    public static <T extends PushablePipe> T join(T pipe,
            Pushable... streams) {
        Pushable last = pipe;
        for (Pushable dst : streams) {
            if (last instanceof PushablePipe) {
                ((PushablePipe) last).join(dst);
                last = dst;
            } else {
                throw new ParsingException();
            }
        }
        return pipe;
    }

    /**
     * return this stream
     */
    public PushablePipe join(Pushable downstream);
}
