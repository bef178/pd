package libcliff.io.codec;

import libcliff.io.BytePipe;
import libcliff.io.Pullable;
import libcliff.io.Pushable;

/**
 * A HexariByte is ASCII presentation of a hex digit, like '9', 'A' or 'e'<br/>
 * <br/>
 * byte 0x65 => byte[] { '6', '5' } under all conditions
 */
public class Hexari implements BytePipe {

    private static final byte[] HEX_DIGIT_TO_LITERAL = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'
    };

    private static int fromHexariByte(int hexari) {
        switch (hexari) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return hexari - '0';
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
                return hexari - 'A' + 10;
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
                return hexari - 'a' + 10;
            default:
                break;
        }
        throw new ParsingException();
    }

    public static int fromHexariBytes(Pullable pullable) {
        return (fromHexariByte(pullable.pull()) << 4)
                | fromHexariByte(pullable.pull());
    }

    public static BytePipe pipe(BytePipe pipe) {
        return new Hexari().setDownstream((Pullable) pipe)
                .setDownstream((Pushable) pipe);
    }

    public static Pullable pipe(Pullable pipe) {
        return new Hexari().setDownstream(pipe);
    }

    public static Pushable pipe(Pushable pipe) {
        return new Hexari().setDownstream(pipe);
    }

    /**
     * accept an int in [0, 255]<br/>
     * return pushed bytes size
     */
    public static int toHexariBytes(int aByte, Pushable pushable) {
        aByte = aByte & 0xFF;
        int size = 0;
        size += pushable.push(HEX_DIGIT_TO_LITERAL[aByte >>> 4]);
        size += pushable.push(HEX_DIGIT_TO_LITERAL[aByte & 0x0F]);
        return size;
    }

    private Pullable downstreamPullable = null;

    private Pushable downstreamPushable = null;

    private Hexari() {
        // dummy
    }

    @Override
    public int pull() {
        return fromHexariBytes(downstreamPullable);
    }

    @Override
    public int push(int aByte) {
        return toHexariBytes(aByte, downstreamPushable);
    }

    public Hexari setDownstream(Pullable pullable) {
        downstreamPullable = pullable;
        return this;
    }

    public Hexari setDownstream(Pushable pushable) {
        downstreamPushable = pushable;
        return this;
    }
}
