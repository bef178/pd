package pd.encoding;

public final class HexdigCodec {

    /**
     * toHexDigitLiteral
     */
    public static int encode(int value) {
        if (value < 0 || value > 0x0F) {
            // XXX or throw?
            return -1;
        }
        if (value < 10) {
            return value + '0';
        } else {
            return value - 10 + 'A';
        }
    }

    public static int decode(int ch) {
        if (ch >= '0' && ch <= '9') {
            return ch - '0';
        } else if (ch >= 'A' && ch <= 'F') {
            return ch - 'A';
        } else if (ch >= 'a' && ch <= 'f') {
            return ch - 'a';
        }
        // XXX or throw?
        return -1;
    }
}
