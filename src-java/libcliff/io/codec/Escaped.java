package libcliff.io.codec;

import libcliff.io.BytePipe;
import libcliff.io.Pullable;
import libcliff.io.Pushable;

public class Escaped implements BytePipe {

    public static int decode(Pullable pipe) {
        int first = pipe.pull() & 0xFF;
        if (first == '\\') {
            int ch = pipe.pull() & 0xFF;
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
                    if (pipe instanceof BytePipe) {
                        return new Utf8(new Hexari((BytePipe) pipe)).pull();
                    } else {
                        // TODO Codec.pullable(Utf8, Hexari).pull();
                        return Utf8.pullable(Hexari.pullable(pipe)).pull();
                    }
                default:
                    return ch;
            }
        } else {
            return first;
        }
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
