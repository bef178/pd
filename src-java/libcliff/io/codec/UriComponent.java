package libcliff.io.codec;

import java.util.BitSet;

import libcliff.io.Pullable;
import libcliff.io.PullablePipe;
import libcliff.io.Pushable;
import libcliff.io.PushablePipe;

/**
 * ch => byte[]
 */
public class UriComponent {

    private static class Puller implements PullablePipe {

        private Pullable upstream;

        @Override
        public Puller join(final Pullable upstream) {

            this.upstream = PullablePipe.join(Utf8.asPuller(), new Pullable() {

                @Override
                public int pull() {
                    int ch = CheckedByte.checkByte(upstream.pull());
                    if (ch == '%') {
                        return Hexari.fromHexariBytes(upstream);
                    } else {
                        return ch;
                    }
                }
            });
            return this;
        }

        @Override
        public int pull() {
            return this.upstream.pull();
        }
    }

    private static class Pusher implements PushablePipe {

        private Pushable downstream;

        @Override
        public Pusher join(final Pushable downstream) {

            this.downstream = PushablePipe.join(Utf8.asPusher(), new Pushable() {

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
            });
            return this;
        }

        @Override
        public int push(int ch) {
            return this.downstream.push(ch);
        }
    }

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

    public static PullablePipe asPuller() {
        return new Puller();
    }

    public static PushablePipe asPusher() {
        return new Pusher();
    }

}
