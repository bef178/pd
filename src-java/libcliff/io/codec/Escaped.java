package libcliff.io.codec;

import libcliff.io.PullStream;
import libcliff.io.Pullable;
import libcliff.io.PushStream;
import libcliff.io.Pushable;

/**
 * ch => utf8 byte[]
 */
public class Escaped implements PullStream, PushStream {

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
                    return PullStream.join(pullable, new Hexari(), new Utf8())
                            .pull();
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
        return size + PushStream.join(pushable, new Hexari(), new Utf8())
                .push(ch);
    }

    private Pullable upstream = null;

    private Pushable downstream = null;

    @Override
    public int pull() {
        return fromEscaped(upstream);
    }

    @Override
    public int push(int ch) {
        return toEscaped(ch, downstream);
    }

    @Override
    public Escaped join(Pushable downstream) {
        this.downstream = downstream;
        return this;
    }

    @Override
    public Escaped join(Pullable upstream) {
        this.upstream = upstream;
        return this;
    }
}
