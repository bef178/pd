package libcliff.io;

public interface Pullable {

    /**
     * return -1 iff reaches the end
     */
    public int pull();
}
