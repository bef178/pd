package libcliff.io;

public interface BytePushable {

    /**
     * accept aByte as an int in [0, 255]<br/>
     * <br/>
     * return the size of finally pushed bytes
     */
    int push(int aByte);
}
