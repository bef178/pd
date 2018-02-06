package libcliff.io.codec;

import java.util.BitSet;

import libcliff.io.BytePipe;
import libcliff.io.BytePullable;
import libcliff.io.BytePushable;
import libcliff.io.Pullable;
import libcliff.io.Pushable;

public class UriComponent implements Pullable, Pushable {

    private static final BitSet SHOULD_NOT_ENCODE;

    static {
        SHOULD_NOT_ENCODE = new BitSet(256);

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

    public static int decode(BytePullable pullable) {
        int aByte = pullable.pull();
        if (aByte == '%') {
            return Hexari.fromHexariText(pullable);
        } else {
            return aByte;
        }
    }

    public static int encode(int ch, BytePushable pushable) {
        if (requiresEncode(ch)) {
            return Utf8.pushable(UriByte.pushable(pushable)).push(ch);
        } else {
            return pushable.push(ch & 0xFF);
        }
    }

    private static boolean requiresEncode(int ch) {
        return ch >= 0 && ch < SHOULD_NOT_ENCODE.size()
                ? !SHOULD_NOT_ENCODE.get(ch)
                : true;
    }

    private BytePipe downstream = null;

    public UriComponent(BytePipe downstream) {
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
