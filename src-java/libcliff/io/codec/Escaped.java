package libcliff.io.codec;

import libcliff.io.BytePipe;
import libcliff.io.Pipe;
import libcliff.io.Pullable;
import libcliff.io.Pushable;

/**
 * ch => utf8 byte[]
 */
public class Escaped implements Pipe {

    public static int fromEscaped(int first, Pullable pullable) {
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
                    // TODO Codec.pullable(Utf8, Hexari).pull();
                    // TODO Utf8.pipe(Hexari).pipe(pullable);
                    // TODO Pipe.join(Utf8, Hexari).pipe(pullable);
                    return Utf8.fromUtf8Bytes(Hexari.pipe(pullable));
                default:
                    return ch;
            }
        } else {
            return first;
        }
    }

    public static int fromEscaped(Pullable pullable) {
        int first = pullable.pull() & 0xFF;
        return fromEscaped(first, pullable);
    }

    public static int toEscaped(int ch, Pushable pushable) {
        int size = 0;
        size += pushable.push('\\');
        size += pushable.push('u');
        return size + Utf8.toUtf8Bytes(ch, Hexari.pipe(pushable));
    }

    private BytePipe downstream = null;

    public Escaped(BytePipe downstream) {
        this.downstream = downstream;
    }

    @Override
    public int pull() {
        return fromEscaped(downstream);
    }

    @Override
    public int push(int ch) {
        return toEscaped(ch, downstream);
    }
}
