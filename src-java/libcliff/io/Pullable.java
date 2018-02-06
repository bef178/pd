package libcliff.io;

public interface Pullable {

    /**
     * return ch as a CodePoint in [0, 0x10FFFF] or -1 iff reaches the end
     */
    int pull();
}
