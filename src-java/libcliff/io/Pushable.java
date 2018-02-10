package libcliff.io;

public interface Pushable {

    /**
     * accept ch as an Unicode character in [0, 0x10FFFF]<br/>
     * <br/>
     * return the number of bytes finally pushed
     */
    int push(int ch);
}
