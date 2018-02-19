package libcliff.io.codec;

/**
 * byte => byte<br/>
 * <br/>
 * Could be a pipe<br/>
 */
public class CheckedByte {

    public static int checkByte(int aByte) {
        if (aByte >= 0 && aByte <= 0xFF) {
            return aByte;
        }
        throw new ParsingException();
    }

    /**
     * accept aByte in [0, 0xFF] or -1 for certain implication
     */
    public static int checkByteEx(int aByte) {
        if (aByte >= 0 && aByte <= 0xFF || aByte == -1) {
            return aByte;
        }
        throw new ParsingException();
    }
}
