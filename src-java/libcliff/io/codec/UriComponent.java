package libcliff.io.codec;

import java.util.BitSet;

import libcliff.io.PullStream;
import libcliff.io.Pullable;
import libcliff.io.PushStream;
import libcliff.io.Pushable;

/**
 * ch => byte[]
 */
public class UriComponent implements PullStream, PushStream {

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

    public static int decode(Pullable upstream) {
        return PullStream.pull(new Utf8(), new UriByte(), upstream);
    }

    public static int encode(int ch, Pushable pushable) {
        if (ch >= 0 && ch <= 0xFF && SHOULD_NOT_ENCODE.get(ch)) {
            return pushable.push(ch);
        } else {
            return PushStream.push(ch, new Utf8(), new UriByte(), pushable);
        }
    }

    private Pullable upstream = null;

    private Pushable downstream = null;

    @Override
    public int pull() {
        return decode(upstream);
    }

    @Override
    public int push(int ch) {
        return encode(ch, downstream);
    }

    @Override
    public PushStream join(Pushable downstream) {
        this.downstream = downstream;
        return this;
    }

    @Override
    public PullStream join(Pullable upstream) {
        this.upstream = upstream;
        return this;
    }
}
