package libcliff.io;

public interface PushStream extends Pushable {

    /**
     * return the last of the streams as the opening end of the pipe
     */
    public static PushStream join(Pushable dst, PushStream... streams) {
        assert streams != null && streams.length > 0;
        for (int i = 0; i < streams.length; ++i) {
            dst = streams[i].join(dst);
        }
        return streams[streams.length - 1];
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
