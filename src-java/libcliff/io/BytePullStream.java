package libcliff.io;

public interface BytePullStream extends PullStream {

    /**
     * return ch as aByte in [0, 0xFF] or -1 iff reaches the end
     */
    @Override
    public int pull();
}
