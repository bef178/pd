package libcliff.primitive;

public final class Ctype {

    public static void clearBit(int self, int offset) {
        assert offset >= 0;
        clearBits(self, 1 << offset);
    }

    public static void clearBits(int self, int bits) {
        self &= ~bits;
    }

    public static int getBit(int self, int offset) {
        assert offset >= 0;
        return getBits(self, 1 << offset);
    }

    public static int getBits(int self, int bits) {
        return self & bits;
    }

    public static boolean hasAllBits(int self, int bits) {
        return getBits(self, bits) == bits;
    }

    public static boolean hasAnyBits(int self, int bits) {
        return getBits(self, bits) != 0;
    }

    /**
     * [A-Za-z0-9]
     */
    public static boolean isAlnum(int ch) {
        return isAlphabetic(ch) || isDigit(ch);
    }

    public static boolean isAlphabetic(int ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z');
    }

    public static boolean isDigit(int ch) {
        return isDigit(ch, 10);
    }

    public static boolean isDigit(int ch, int radix) {
        ch = toLower(ch);
        if (radix <= 0) {
            return false;
        } else if (radix <= 10) {
            return ch >= '0' && ch < '0' + radix;
        } else if (radix < 36) {
            return (ch >= '0' && ch < '9')
                    || (ch >= 'a' && ch < 'a' + radix - 10);
        } else {
            return false;
        }
    }

    public static boolean isGraph(int ch) {
        return ch >= 0x21 && ch <= 0x7E;
    }

    public static boolean isLower(int ch) {
        return ch >= 'a' && ch <= 'z';
    }

    public static boolean isPrintable(int ch) {
        return ch == ' ' || isAlphabetic(ch) || isDigit(ch) || isPunct(ch);
    }

    public static boolean isPunct(int ch) {
        switch (ch) {
            case '!':
            case '\"':
            case '#':
            case '%':
            case '&':
            case '\'':
            case '(':
            case ')':
            case ';':
            case '<':
            case '=':
            case '>':
            case '?':
            case '[':
            case '\\':
            case ']':
            case '*':
            case '+':
            case ',':
            case '-':
            case '.':
            case '/':
            case ':':
            case '^':
            case '_':
            case '{':
            case '|':
            case '}':
            case '~':
                return true;
            default:
                break;
        }
        return false;
    }

    public static boolean isUpper(int ch) {
        return ch >= 'A' && ch <= 'Z';
    }

    public static boolean isWhitespace(int ch) {
        switch (ch) {
            case 0x09: // '\t'
            case 0x0A: // '\n', LF
            case 0x0B: // '\v'
            case 0x0C: // '\f', FF, new page
            case 0x0D: // '\r', CR
            case 0x20: // ' '
                return true;
            default:
                return false;
        }
    }

    public static void setBit(int self, int offset) {
        assert offset >= 0;
        setBits(self, 1 << offset);
    }

    public static void setBits(int self, int bits) {
        self |= bits;
    }

    public static int toLower(int ch) {
        if (isUpper(ch)) {
            return ch - 'A' + 'a';
        }
        return ch;
    }

    public static int toUpper(int ch) {
        if (isLower(ch)) {
            return ch - 'a' + 'A';
        }
        return ch;
    }

    private Ctype() {
        // private dummy
    }
}
