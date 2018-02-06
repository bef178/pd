package libcliff.io;

public interface BytePullable {

    /**
     * return aByte as in [0, 255] or -1 iff reaches the end
     */
    int pull();
}
