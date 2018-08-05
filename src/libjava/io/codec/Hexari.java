package libjava.io.codec;

import static libjava.io.Util.checkByte;

import libjava.io.ParsingException;
import libjava.io.Pullable;
import libjava.io.PullablePipe;
import libjava.io.Pushable;
import libjava.io.PushablePipe;

/**
 * A HexariByte is ASCII presentation of a hex digit, like '9', 'A' or 'e'<br/>
 * <br/>
 * byte 0x65 => byte[] { '6', '5' } under all conditions
 */
public class Hexari {

    public static PullablePipe asPullablePipe() {

        return new PullablePipe() {

            private Pullable upstream = null;

            @Override
            public PullablePipe join(Pullable upstream) {
                this.upstream = upstream;
                return this;
            }

            @Override
            public int pull() {
                return fromHexariBytes(upstream);
            }
        };
    }

    public static PushablePipe asPushablePipe() {

        return new PushablePipe() {

            private Pushable downstream = null;

            @Override
            public PushablePipe join(Pushable downstream) {
                this.downstream = downstream;
                return this;
            }

            @Override
            public void push(int aByte) {
                toHexariBytes(aByte, downstream);
            }
        };
    }

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

    public static int fromHexariBytes(int i, int j) {
        return (fromHexariByte(checkByte(i)) << 4) | fromHexariByte(checkByte(j));
    }

    public static int fromHexariBytes(Pullable pullable) {
        int i = pullable.pull();
        int j = pullable.pull();
        return fromHexariBytes(i, j);
    }

    /**
     * accept an int in [0, 255]<br/>
     * return pushed bytes size
     */
    public static void toHexariBytes(int aByte, Pushable pushable) {
        checkByte(aByte);
        pushable.push(HEX_DIGIT_TO_LITERAL[aByte >>> 4]);
        pushable.push(HEX_DIGIT_TO_LITERAL[aByte & 0x0F]);
    }
}
