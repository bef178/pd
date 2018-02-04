package libcliff.io.codec;

import java.util.BitSet;

import libcliff.io.BytePipe;
import libcliff.io.Pullable;
import libcliff.io.Pushable;

/**
 * for characters within a uri component
 */
public class UriComponent implements BytePipe {

    private static final BitSet SHOULD_ENCODE;

    static {
        SHOULD_ENCODE = new BitSet(256);

        for (int i = 0; i < SHOULD_ENCODE.length(); ++i) {
            SHOULD_ENCODE.set(i);
        }

        // rfc3986 2.3 Unreserved Characters
        for (int i = 'A'; i <= 'Z'; ++i) {
            SHOULD_ENCODE.clear(i);
        }
        for (int i = 'a'; i <= 'z'; ++i) {
            SHOULD_ENCODE.clear(i);
        }
        for (int i = '0'; i <= '9'; ++i) {
            SHOULD_ENCODE.clear(i);
        }
        final String UNRESERVED = "-_.~";
        for (int ch : UNRESERVED.toCharArray()) {
            SHOULD_ENCODE.clear(ch);
        }

        // reserved character should be encoded if it not a delimiter
        final String GEN_DELIMS = ":/?#[]@";
        final String SUB_DELIMS = "!$&'()*+,;=";
        final String RESERVED = GEN_DELIMS + SUB_DELIMS;
        for (int i : RESERVED.getBytes()) {
            SHOULD_ENCODE.set(i & 0xFF);
        }
    }

    public static int decodeByte(Pullable pullable) {
        int ch = pullable.pull();
        if (ch == '%') {
            return Hexari.fromHexariText(pullable);
        } else {
            return ch;
        }
    }

    public static int encodeByte(int aByte, Pushable pushable) {
        if (requiresEncode(aByte)) {
            int size = 0;
            size += pushable.push('%');
            size += Hexari.toHexariText(aByte & 0xFF, pushable);
            return size;
        } else {
            return pushable.push(aByte & 0xFF);
        }
    }

    private static boolean requiresEncode(int ch) {
        return SHOULD_ENCODE.get(ch);
    }

    private BytePipe downstream = null;

    public UriComponent(BytePipe downstream) {
        this.downstream = downstream;
    }

    @Override
    public int pull() {
        return decodeByte(downstream);
    }

    @Override
    public int push(int aByte) {
        return encodeByte(aByte, downstream);
    }
}
