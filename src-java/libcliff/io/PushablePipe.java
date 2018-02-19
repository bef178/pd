package libcliff.io;

public interface PushablePipe extends Pushable {

    /**
     * return the opening end of the pipe<br/>
     */
    public static PushablePipe join(Pushable... streams) {
        assert streams != null && streams.length > 0;
        Pushable dst = streams[streams.length - 1];
        for (int i = streams.length - 2; i >= 0; --i) {
            dst = ((PushablePipe) streams[i]).join(dst);
        }
        return (PushablePipe) dst;
    }

    public static int push(int ch, Pushable... streams) {
        return join(streams).push(ch);
    }

    /**
     * accept ch as in [0, 0x10FFFF] or -1 for certain implication<br/>
     * <br/>
     * return the number of bytes finally pushed
     */
    @Override
    int push(int ch);

    /**
     * return this stream
     */
    public PushablePipe join(Pushable downstream);
}
