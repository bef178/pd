package libcliff.io.codec;

import libcliff.io.BytePipe;
import libcliff.io.Pullable;
import libcliff.io.Pushable;

public class Escaped implements BytePipe {

    /**
     * @return a code point
     */
    public static int decode(int first, Pullable pullable) {
        if (first == '\\') {
            int ch = pullable.pull() & 0xFF;
            switch (ch) {
                case '\\':
                    return '\\';
                case 'a':
                    return 0x07;
                case 't':
                    return 0x09;
                case 'n':
                    return 0x0A;
                case 'v':
                    return 0x0B;
                case 'f':
                    return 0x0C;
                case 'r':
                    return 0x0D;
                case 'b':
                    return '\b';
                case 'u':
                    if (pullable instanceof BytePipe) {
                        return new Utf8(new Hexari((BytePipe) pullable)).pull();
                    } else {
                        // TODO Codec.pullable(Utf8, Hexari).pull();
                        return Utf8.pullable(Hexari.pullable(pullable)).pull();
                    }
                default:
                    return ch;
            }
        } else {
            return first;
        }
    }

    public static int decode(Pullable pullable) {
        int first = pullable.pull() & 0xFF;
        return decode(first, pullable);
    }

    public static int encode(int ch, Pushable pipe) {
        int size = 0;
        size += pipe.push('\\');
        size += pipe.push('u');
        if (pipe instanceof BytePipe) {
            return size + new Utf8(new Hexari((BytePipe) pipe)).push(ch);
        } else {
            return size + Utf8.pushable(Hexari.pushable(pipe)).push(ch);
        }
    }

    private BytePipe downstream = null;

    public Escaped(BytePipe downstream) {
        this.downstream = downstream;
    }

    @Override
    public int pull() {
        return decode(downstream);
    }

    @Override
    public int push(int ch) {
        return encode(ch, downstream);
    }
}
