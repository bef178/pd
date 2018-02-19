package libcliff.io.codec;

import libcliff.io.Pullable;
import libcliff.io.PullablePipe;
import libcliff.io.Pushable;
import libcliff.io.PushablePipe;

/**
 * ch => utf8 byte[]
 */
public class Escaped implements PullablePipe, PushablePipe {

    public static int fromEscaped(int firstByte, Pullable pullable) {
        CheckedByte.checkByteEx(firstByte);
        if (firstByte == '\\') {
            int ch = CheckedByte.checkByteEx(pullable.pull());
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
                    return PullablePipe.pull(new Utf8(), new Hexari(), pullable);
                default:
                    return ch;
            }
        } else {
            return firstByte;
        }
    }

    public static int fromEscaped(Pullable pullable) {
        int firstByte = CheckedByte.checkByteEx(pullable.pull());
        return fromEscaped(firstByte, pullable);
    }

    public static int toEscaped(int ch, Pushable pushable) {
        int size = 0;
        size += pushable.push('\\');
        size += pushable.push('u');
        return size + PushablePipe.push(ch, new Utf8(), new Hexari(), pushable);
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
