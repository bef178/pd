package libcliff.io.codec;

import libcliff.io.BytePipe;
import libcliff.io.Pullable;
import libcliff.io.Pushable;

/**
 * A HexariText is ASCII presentation of a hex digit, like '9', 'A' or 'e'
 */
public class Hexari implements BytePipe {

    private static final byte[] HEX_DIGIT_TO_LITERAL = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'
    };

    private static int fromHexariText(int hexari) {
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

    /**
     * e.g. "AF" => 0xAF
     */
    public static int fromHexariText(Pullable pullable) {
        return (fromHexariText(pullable.pull()) << 4)
                | fromHexariText(pullable.pull());
    }

    public static Pullable pullable(final Pullable pullable) {

        return new Pullable() {

            private Pullable pipe = pullable;

            @Override
            public int pull() {
                return fromHexariText(this.pipe);
            }
        };
    }

    public static Pushable pushable(final Pushable pushable) {

        return new Pushable() {

            private Pushable pipe = pushable;

            @Override
            public int push(int i) {
                return toHexariText(i, this.pipe);
            }
        };
    }

    /**
     * accept an int in [0, 255]<br/>
     * return pushed bytes size
     */
    public static int toHexariText(int aByte, Pushable pushable) {
        aByte = aByte & 0xFF;
        int size = 0;
        size += pushable.push(HEX_DIGIT_TO_LITERAL[aByte >>> 4]);
        size += pushable.push(HEX_DIGIT_TO_LITERAL[aByte & 0x0F]);
        return size;
    }

    private BytePipe downstream = null;

    public Hexari(BytePipe downstream) {
        this.downstream = downstream;
    }

    @Override
    public int pull() {
        return fromHexariText(downstream);
    }

    @Override
    public int push(int aByte) {
        return toHexariText(aByte, downstream);
    }
}
