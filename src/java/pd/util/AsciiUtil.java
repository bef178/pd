package pd.util;

/**
 * `int` as ascii code<br/>
 * <br/>
 * naming: Util consisting of static methods<br/>
 */
public class AsciiUtil {

    public static final int HT = '\t';
    public static final int LF = '\n';
    public static final int CR = '\r';
    public static final int SP = ' ';
    public static final int COMMA = ',';
    public static final int DOUBLE_QUOTE = '\"';

    /**
     * [A-Za-z0-9]
     */
    public static boolean isAlnum(int ch) {
        return isAlpha(ch) || isDigit(ch);
    }

    public static boolean isAlpha(int ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z');
    }

    public static boolean isAscii(int ch) {
        return ch >= 0 && ch <= 0xFF;
    }

    public static boolean isControl(int ch) {
        return ch >= 0 && ch < 0x20 || ch == 0x7F;
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
            return (ch >= '0' && ch < '9') || (ch >= 'a' && ch < 'a' + radix - 10);
        } else {
            return false;
        }
    }

    public static boolean isLower(int ch) {
        return ch >= 'a' && ch <= 'z';
    }

    /**
     * visible single-width graph or SP<br/>
     * @see int isprint(int ch)
     */
    public static boolean isPrintable(int ch) {
        return ch >= 0x20 && ch < 0x7F;
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

    /**
     * @see int isgraph(int ch)
     */
    public static boolean isVisible(int ch) {
        return ch > 0x20 && ch < 0x7F;
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

    private AsciiUtil() {
        // private dummy
    }
}
