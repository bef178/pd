package pd.io.codec;

import static pd.io.Util.checkByte;

import java.util.BitSet;

import pd.io.Pullable;
import pd.io.PullablePipe;
import pd.io.Pushable;
import pd.io.PushablePipe;

/**
 * ch => byte[]
 */
@Deprecated
public class UriComponent {

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

    public static PullablePipe asPullablePipe() {

        return new PullablePipe() {

            private PullablePipe upstream;

            @Override
            public <T extends Pullable> T join(final T upstream) {
                this.upstream = Utf8.asPullablePipe();
                this.upstream.join(new Pullable() {

                    @Override
                    public int pull() {
                        int ch = checkByte(upstream.pull());
                        if (ch == '%') {
                            return Hexari.fromHexariBytes(upstream);
                        } else {
                            return ch;
                        }
                    }
                });
                return upstream;
            }

            @Override
            public int pull() {
                return this.upstream.pull();
            }
        };
    }

    public static PushablePipe asPushablePipe() {

        return new PushablePipe() {

            private PushablePipe downstream;

            @Override
            public <T extends Pushable> T join(final T downstream) {
                this.downstream = Utf8.asPushablePipe();
                this.downstream.join(new Pushable() {

                    @Override
                    public void push(int ch) {
                        checkByte(ch);
                        if (SHOULD_NOT_ENCODE.get(ch)) {
                            downstream.push(ch);
                        } else {
                            downstream.push('%');
                            Hexari.toHexariBytes(ch, downstream);
                        }
                    }
                });
                return downstream;
            }

            @Override
            public void push(int ch) {
                this.downstream.push(ch);
            }
        };
    }
}
