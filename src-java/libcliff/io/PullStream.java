package libcliff.io;

public interface PullStream extends Pullable {

    /**
     * return the last of the streams as the opening end of the pipe
     */
    public static PullStream join(Pullable src, PullStream... streams) {
        assert streams != null && streams.length > 0;
        for (int i = 0; i < streams.length; ++i) {
            src = streams[i].join(src);
        }
        return streams[streams.length - 1];
    }

    /**
     * return ch as an Unicode character in [0, 0x10FFFF] or -1 iff reaches the end
     */
    @Override
    public int pull();

    /**
     * return this stream
     */
    public PullStream join(Pullable upstream);
}
