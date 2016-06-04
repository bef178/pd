package τ.typedef.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.BitSet;

import τ.typedef.basic.Blob;

public final class FormatCodec {

    public interface Nextable {

        byte next();
    }

    public static final class Base64 extends FormatCodecBase64 {
        // dummy
    }

    public static final class PrivateContract {

        public static int decode(Nextable callback) {
            expect('\\', callback.next());
            int ch = callback.next();
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
                    return Unicode.fromUtf8HexBytes(callback);
                default:
                    return ch;
            }
        }

        public static int encode(int ch, Blob blob) {
            int n = 2 + Unicode.utf8Length(ch) * 2;
            if (blob != null) {
                if (blob.isEmpty()) {
                    blob.init(n);
                }
                int start = blob.i;
                blob.next((byte) '\\');
                blob.next((byte) 'u');
                int m = Unicode.toUtf8HexBytes(ch, blob);
                assert m == n - 2;
                blob.i = start;
            }
            return n;
        }

        private static void expect(int expected, int actual) {
            if (expected != actual) {
                throw new IllegalArgumentException("Excepts \'"
                        + (char) expected + "\', gets \'" + (char) actual
                        + "\'");
            }
        }

        private PrivateContract() {
            // private dummy
        }
    }

    public static final class Unicode {

        public static int fromUtf8(Blob blob) {
            int ch = blob.next() & 0xFF;
            int n = utf8LengthByUtf8((byte) ch);
            if (n < 0) {
                throw new ParsingException();
            }
            if (n == 1) {
                return ch;
            }
            ch = ch & ~(0xFF >>> (8 - n) << (8 - n));
            for (int i = 1; i < n; ++i) {
                ch = blob.next() & 0x3F | (ch << 6);
            }
            return ch;
        }

        public static int fromUtf8HexBytes(Nextable callback) {
            int firstByte = (hexByte2HexInt(callback.next()) << 4)
                    | hexByte2HexInt(callback.next());

            int n = utf8LengthByUtf8((byte) firstByte);
            if (n < 0) {
                throw new ParsingException();
            }

            if (n == 1) {
                return firstByte;
            }

            int ch = firstByte & ~(0xFF >>> (8 - n) << (8 - n));
            for (int i = 1; i < n; ++i) {
                int b = (Unicode.hexByte2HexInt(callback.next()) << 4)
                        | Unicode.hexByte2HexInt(callback.next());
                ch = b & 0x3F | (ch << 6);
            }
            return ch;
        }

        /**
         * '0' => 0, 'A' => 10, etc<br/>
         *
         * @return -1 if given an invalid hex
         */
        public static int hexByte2HexInt(int hexByte) {
            assert hexByte >= 0;
            switch (hexByte) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    return hexByte - '0';
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                    return hexByte - 'A' + 10;
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                    return hexByte - 'a' + 10;
                default:
                    return -1;
            }
        }

        /**
         * 0x00 => '0', 0x0A => 'A'<br/>
         */
        public static byte hexInt2HexByte(int i) {
            return DIGIT_HEX[i];
        }

        /**
         * [0-9A-Fa-f]
         */
        public static boolean isHexByte(int ch) {
            return hexByte2HexInt(ch) >= 0;
        }

        /**
         * 0x61 => "61", 0x161 => "0161", 0x6161 => "6161"<br/>
         * output to the given blob<br/>
         * it will be a dry run if the blob is null, and the blob will be enlarged if it is empty<br/>
         *
         * @return the number of bytes written
         */
        public static int toHexBytes(int ch, int minLength, Blob blob) {
            assert ch >= 0;
            minLength = (minLength + 1) & ~0x01;

            int n = 2;
            for (int i = ch >>> 8; i != 0; i >>>= 8) {
                n += 2;
            }
            if (n < minLength) {
                n = minLength;
            }

            if (blob != null) {
                if (blob.isEmpty()) {
                    blob.init(n);
                }
                for (int i = blob.i + n - 1; i >= blob.i; --i) {
                    blob.a[i] = DIGIT_HEX[ch & 0x0F];
                    ch >>>= 4;
                }
            } else {
                // dry run
            }
            return n;
        }

        public static int toUcs4(int ch, Blob blob) {
            assert ch >= 0;
            if (blob != null) {
                if (blob.isEmpty()) {
                    blob.init(4);
                }
                int pos = blob.i + 4;
                while (ch != 0) {
                    blob.a[--pos] = (byte) (0xFF & ch);
                    ch >>>= 8;
                }
            }
            return 4;
        }

        /**
         * a fast encoder without checking content<br/>
         * if checking required, use
         * <code>new String(Character.toChars(ch)).getBytes("UTF-8")</code>
         *
         * @return standard utf8 byte sequence
         */
        public static int toUtf8(int ch, Blob blob) {
            assert ch >= 0;
            final int n = utf8Length(ch);

            if (blob != null) {
                if (blob.isEmpty()) {
                    blob.init(n);
                }
                if (n == 1) {
                    // ASCII
                    blob.a[blob.i] = (byte) ch;
                } else {
                    int pos = blob.i + n;
                    while (--pos != blob.i) {
                        int b = ch & 0x3F | 0x80;
                        blob.a[pos] = (byte) b;
                        ch >>>= 6;
                    }
                    // set leading bits
                    blob.a[pos] = (byte) (ch | (0xFF >> (8 - n) << (8 - n)));
                }
            }
            return n;
        }

        public static int toUtf8HexBytes(int ch, Blob blob) {
            assert ch >= 0;
            final int n = utf8Length(ch);

            if (blob != null) {
                if (blob.isEmpty()) {
                    blob.init(n * 2);
                }
                if (n == 1) {
                    toHexBytes(ch, 0, blob);
                } else {
                    blob.i += n * 2;
                    for (int i = n; i > 1; --i) {
                        blob.i -= 2;
                        toHexBytes(ch & 0x3F | 0x80, 0, blob);
                        ch >>>= 6;
                    }
                    blob.i -= 2;
                    int b = ch | (0xFF >> (8 - n) << (8 - n));
                    toHexBytes(b, 0, blob);
                }
            }
            return n * 2;
        }

        // UTF-8 uses 6b * 5 + 1b = 31b, the highest bit is not used
        public static int utf8Length(int ch) {
            assert ch >= 0;
            int bytes = 0;
            if (ch <= 0x7F) {
                bytes = 1;
            } else if (ch <= 0x7FF) {
                // LE 11-bit: 0000 0111 1111 1111 => 1101 1111 1011 1111
                //                  ~~~ ~~~~ ~~~~ =>    ~ ~~~~   ~~ ~~~~
                bytes = 2;
            } else if (ch <= 0xFFFF) {
                // LE 16b
                bytes = 3;
            } else if (ch <= 0x1FFFFF) {
                // LE 21b
                bytes = 4;
            } else if (ch <= 0x3FFFFFF) {
                // LE 26b
                bytes = 5;
            } else if (ch <= 0x7FFFFFFF) {
                // LE 31b
                bytes = 6;
            }
            return bytes;
        }

        public static int utf8LengthByUtf8(byte b) {
            if ((b & 0x80) == 0) {
                return 1;
            } else if ((b & 0x20) == 0) {
                return 2;
            } else if ((b & 0x10) == 0) {
                return 3;
            } else if ((b & 0x08) == 0) {
                return 4;
            } else if ((b & 0x04) == 0) {
                return 5;
            } else if ((b & 0x02) == 0) {
                return 6;
            }
            return -1;
        }

        private Unicode() {
            // private dummy
        }
    }

    /**
     * for characters within a uri component
     */
    public static final class Uri {

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
            Unicode.toHexBytes(b, 0, blob);
            ostream.write(blob.a);
            return ostream;
        }

        public static boolean shouldBeEncoded(int ch) {
            return SHOULD_BE_ENCODED.get(ch);
        }

        private Uri() {
            // private dummy
        }
    }

    private static final byte[] DIGIT_HEX = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'
    };

}
