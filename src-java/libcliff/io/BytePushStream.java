package libcliff.io;

public interface BytePushStream extends PushStream {

    /**
     * accept ch as aByte in [0, 0xFF] or -1 for certain implication<br/>
     * <br/>
     * return the number of bytes finally pushed
     */
    @Override
    public int push(int aByte);
}
