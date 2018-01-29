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

    private static int fromHexariText(int ascii) {
        switch (ascii) {
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
                return ascii - '0';
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
                return ascii - 'A' + 10;
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
                return ascii - 'a' + 10;
            default:
                break;
        }
        throw new IllegalArgumentException();
    }

    public static int fromHexariText(Pullable pipe) {
        return (fromHexariText(pipe.pull()) << 4)
                | fromHexariText(pipe.pull());
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

    private static int toHexariText(int i) {
        assert i >= 0 && i < 16;
        return HEX_DIGIT_TO_LITERAL[i];
    }

    /**
     * accept an int in [0, 255]<br/>
     * return pushed bytes size
     */
    public static int toHexariText(int i, Pushable pushable) {
        i = i & 0xFF;
        int size = 0;
        size += pushable.push(toHexariText(i >>> 4));
        size += pushable.push(toHexariText(i & 0x0F));
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
    public int push(int i) {
        return toHexariText(i, downstream);
    }
}
