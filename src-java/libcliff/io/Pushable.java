package libcliff.io;

public interface Pushable {

    /**
     * accept ch as a CodePoint in [0, 0x10FFFF]<br/>
     * <br/>
     * return the size of finally pushed bytes
     */
    int push(int ch);
}
