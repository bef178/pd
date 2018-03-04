package libcliff.io.codec;

import java.util.BitSet;

import libcliff.io.Pullable;
import libcliff.io.PullablePipe;
import libcliff.io.Pushable;
import libcliff.io.PushablePipe;

/**
 * ch => byte[]
 */
public class UriComponent extends DualPipe {

    private static final BitSet SHOULD_NOT_ENCODE;

    static {
        SHOULD_NOT_ENCODE = new BitSet(0x7F); // initially false

        // rfc3986 2.3 Unreserved Characters
        for (int i = 'A'; i <= 'Z'; ++i) {
            SHOULD_NOT_ENCODE.set(i);
        }
        for (int i = 'a'; i <= 'z'; ++i) {
            SHOULD_NOT_ENCODE.set(i);
        }
        for (int i = '0'; i <= '9'; ++i) {
            SHOULD_NOT_ENCODE.set(i);
        }
        final String UNRESERVED = "-_.~";
        for (int i : UNRESERVED.toCharArray()) {
            SHOULD_NOT_ENCODE.set(i);
        }

        // reserved character should be encoded if it is not a delimiter
        final String GEN_DELIMS = ":/?#[]@";
        for (int i : GEN_DELIMS.toCharArray()) {
            SHOULD_NOT_ENCODE.clear(i);
        }

        final String SUB_DELIMS = "!$&'()*+,;=";
        for (int i : SUB_DELIMS.toCharArray()) {
            SHOULD_NOT_ENCODE.clear(i);
        }
    }

    @Override
    public UriComponent join(final Pullable upstream) {

        super.join(PullablePipe.join(new Utf8(), new Pullable() {

            @Override
            public int pull() {
                int ch = CheckedByte.checkByte(upstream.pull());
                if (ch == '%') {
                    return Hexari.fromHexariBytes(upstream);
                } else {
                    return ch;
                }
            }
        }));
        return this;
    }

    @Override
    public UriComponent join(final Pushable downstream) {

        super.join(PushablePipe.join(new Utf8(), new Pushable() {

            @Override
            public int push(int ch) {
                ch = CheckedByte.checkByte(ch);
                if (SHOULD_NOT_ENCODE.get(ch)) {
                    return downstream.push(ch);
                } else {
                    int size = 0;
                    size += downstream.push('%');
                    size += Hexari.toHexariBytes(ch, downstream);
                    return size;
                }
            }
        }));
        return this;
    }
}
