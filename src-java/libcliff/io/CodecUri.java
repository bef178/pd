package libcliff.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.BitSet;

import libcliff.io.codec.Hexari;

/**
 * for characters within a uri component
 */
class CodecUri {

    private static final BitSet SHOULD_BE_ENCODED = new BitSet(256);

    static {
        for (int i = 0; i < SHOULD_BE_ENCODED.length(); ++i) {
            SHOULD_BE_ENCODED.set(i);
        }

        // rfc3986 2.3 Unreserved Characters
        for (int i = 'A'; i <= 'Z'; ++i) {
            SHOULD_BE_ENCODED.clear(i);
        }
        for (int i = 'a'; i <= 'z'; ++i) {
            SHOULD_BE_ENCODED.clear(i);
        }
        for (int i = '0'; i <= '9'; ++i) {
            SHOULD_BE_ENCODED.clear(i);
        }
        final String UNRESERVED = "-_.~";
        for (int ch : UNRESERVED.toCharArray()) {
            SHOULD_BE_ENCODED.clear(ch);
        }

        // reserved character should be encoded if it not a delimiter
        final String GEN_DELIMS = ":/?#[]@";
        final String SUB_DELIMS = "!$&'()*+,;=";
        final String RESERVED = GEN_DELIMS + SUB_DELIMS;
        for (char ch : RESERVED.toCharArray()) {
            SHOULD_BE_ENCODED.set(ch);
        }
    }

    public static OutputStream encodeAndPut(byte b, OutputStream ostream)
            throws IOException {
        ostream.write('%');
        Blob blob = new Blob();
        Hexari.toHexariText(b, blob);
        ostream.write(blob.a);
        return ostream;
    }

    public static boolean shouldBeEncoded(int ch) {
        return SHOULD_BE_ENCODED.get(ch);
    }
}
