package libjava.io.codec;

import static libjava.io.Util.checkByte;

import libjava.io.ParsingException;
import libjava.io.Pullable;
import libjava.io.PullablePipe;
import libjava.io.Pushable;
import libjava.io.PushablePipe;

/**
 * 0x65 => '6' '5'
 */
public class Hexari {

    public static PullablePipe asPullablePipe() {

        return new PullablePipe() {

            private Pullable upstream = null;

            @Override
            public <T extends Pullable> T join(T upstream) {
                this.upstream = upstream;
                return upstream;
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
            public <T extends Pushable> T join(T downstream) {
                this.downstream = downstream;
                return downstream;
            }

            @Override
            public void push(int aByte) {
                toHexariBytes(aByte, downstream);
            }
        };
    }

    private static int fromHexariByte(int ch) {
        switch (ch) {
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
                return ch - '0';
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
                return ch - 'A' + 10;
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
                return ch - 'a' + 10;
            default:
                throw new ParsingException();
        }
    }

    public static int fromHexariBytes(Pullable pullable) {
        int i = checkByte(pullable.pull());
        int j = checkByte(pullable.pull());
        return (fromHexariByte(i) << 4) | fromHexariByte(j);
    }

    private static byte toHexariByte(int value) {
        switch (value) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                return (byte) (value + '0');
            case 0x0A:
            case 0x0B:
            case 0x0C:
            case 0x0D:
            case 0x0E:
            case 0x0F:
                return (byte) (value - 10 + 'A');
            default:
                throw new ParsingException();
        }
    }

    /**
     * accept an int in [0, 255]<br/>
     * return pushed bytes size
     */
    public static void toHexariBytes(int aByte, Pushable pushable) {
        checkByte(aByte);
        pushable.push(toHexariByte(aByte >>> 4));
        pushable.push(toHexariByte(aByte & 0x0F));
    }
}
