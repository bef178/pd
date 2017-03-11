package cc.typedef.io;

class CodecGlyph {

    private static final byte[] HEX_DIGIT_TO_LITERAL = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'
    };

    public static int decode(int last, Nextable it) {
        expect('\\', last);
        assert it.hasNext();
        int ch = it.next();
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
                return fromUtf8Bytes(it, true);
            default:
                return ch;
        }
    }

    public static int decode(Nextable it) {
        assert it != null;
        assert it.hasNext();
        int last = it.next();
        return decode(last, it);
    }

    public static int encode(int ch, Pushable it) {
        int n = 2 + utf8Length(ch) * 2;
        assert it != null;
        it.push('\\');
        it.push('u');
        int m = toUtf8Bytes(ch, it, true);
        assert m == n - 2;
        return n;
    }

    private static void expect(int expected, int actual) {
        if (expected != actual) {
            throw new IllegalArgumentException("Excepts \'"
                    + (char) expected + "\', gets \'" + (char) actual
                    + "\'");
        }
    }

    public static int fromUtf8Bytes(Nextable it, boolean isLiteral) {
        int firstByte = isLiteral
                ? (hexDigitFromLiteral(it.next()) << 4)
                        | hexDigitFromLiteral(it.next())
                : it.next();

        int n = utf8LengthByFirstByte((byte) firstByte);
        if (n < 0) {
            throw new ParsingException();
        }

        if (n == 1) {
            return firstByte;
        }

        int ch = firstByte & ~(0xFF >>> (8 - n) << (8 - n));
        for (int i = 1; i < n; ++i) {
            int b = isLiteral
                    ? (hexDigitFromLiteral(it.next()) << 4)
                            | hexDigitFromLiteral(it.next())
                    : it.next();
            ch = b & 0x3F | (ch << 6);
        }
        return ch;
    }

    /**
     * '0' => 0, 'A' => 10, etc<br/>
     *
     * @return -1 if receive an invalid hex
     */
    private static int hexDigitFromLiteral(int ch) {
        assert isHexDigit(ch);
        switch (ch) {
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
                return ch - '0';
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
                return ch - 'A' + 10;
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
                return ch - 'a' + 10;
            default:
                return -1;
        }
    }

    private static boolean isHexDigit(int i) {
        return i >= 0 && i < 16;
    }

    /**
     * accept an int in [0, 255]<br/>
     * return pushed bytes size
     */
    private static int toLiteral(int ch, Pushable it) {
        ch = ch & 0xFF;
        assert it != null;
        it.push(HEX_DIGIT_TO_LITERAL[ch >>> 4]);
        it.push(HEX_DIGIT_TO_LITERAL[ch & 0x0F]);
        return 2;
    }

    public static int toUcs4Bytes(int ch, Pushable it) {
        assert ch >= 0;
        assert it != null;

        for (int i = 0; i < 4; ++i) {
            int c = ch;
            int j = 4 - 1 - i;
            while (j-- > 0) {
                c >>>= 8;
            }
            it.push(c & 0xFF);
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
    public static int toUtf8Bytes(int ch, Pushable it, boolean isLiteral) {
        assert ch >= 0;
        final int n = utf8Length(ch);

        assert it != null;
        if (n == 1) {
            // ASCII
            if (isLiteral) {
                toLiteral(ch, it);
                return 2;
            } else {
                it.push(ch);
                return 1;
            }
        }

        for (int i = 0; i < n; ++i) {
            int c = ch;
            int j = n - 1 - i;
            while (j-- > 0) {
                c >>>= 6;
            }
            if (i == 0) {
                c = c | (0xFF >> (8 - n) << (8 - n));
            } else {
                c = c & 0x3F | 0x80;
            }
            if (isLiteral) {
                toLiteral(c, it);
            } else {
                it.push(c);
            }
        }

        return isLiteral ? n * 2 : n;
    }

    // UTF-8 uses 6b * 5 + 1b = 31b, the highest bit is not used
    private static int utf8Length(int ch) {
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

    private static int utf8LengthByFirstByte(byte b) {
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
}
