package libcliff.io;

public interface PushStream extends Pushable {

    /**
     * return the opening end of the pipe<br/>
     */
    public static PushStream join(Pushable... streams) {
        assert streams != null && streams.length > 0;
        Pushable dst = streams[streams.length - 1];
        for (int i = streams.length - 2; i >= 0; --i) {
            dst = ((PushStream) streams[i]).join(dst);
        }
        return (PushStream) dst;
    }

    public static int push(int ch, Pushable... streams) {
        return join(streams).push(ch);
    }

    /**
     * accept ch as an Unicode character in [0, 0x10FFFF] or -1 for certain implication<br/>
     * <br/>
     * return the number of bytes finally pushed
     */
    @Override
    int push(int ch);

    /**
     * return this stream
     */
    public PushStream join(Pushable downstream);
}
