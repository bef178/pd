package τ.typedef;

import java.util.BitSet;

/**
 * Define logical concepts:<br/>
 * <br/>
 * int <b>ch</b>: a code point represents a character<br/>
 * byte <b>hexByte</b>: a single ASCII character that represents a hex digit, i.e.
 * [0-9a-fA-F]; {hex byte} is a subset of {code point}<br/>
 */
public class Ctype {

    private static final BitSet ALPHANUM;

    static {
        ALPHANUM = new BitSet(128);
        for (int i = 'A'; i <= 'Z'; ++i) {
            ALPHANUM.set(i);
        }
        for (int i = 'a'; i <= 'z'; ++i) {
            ALPHANUM.set(i);
        }
        for (int i = '0'; i <= '9'; ++i) {
            ALPHANUM.set(i);
        }
    }

    /**
     * [A-Za-z0-9]
     */
    public static boolean isAlphanum(int ch) {
        assert ch >= 0;
        return ALPHANUM.get(ch);
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

    public static boolean isLower(int ch) {
        return ch >= 'a' && ch <= 'z';
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
