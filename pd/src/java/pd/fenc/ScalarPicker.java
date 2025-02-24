package pd.fenc;

import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.function.IntPredicate;

import static pd.util.AsciiExtension.EOF;
import static pd.util.AsciiExtension.isAlpha;
import static pd.util.AsciiExtension.isDigit;
import static pd.util.AsciiExtension.isWhitespace;

public class ScalarPicker {

    private static final ScalarPicker one = new ScalarPicker();

    public static ScalarPicker singleton() {
        return one;
    }

    private ScalarPicker() {
        // dummy
    }

    /**
     * might return empty string
     */
    public String pickString(UnicodeProvider src, IntPredicate charset) {
        StringBuilder sb = new StringBuilder();
        pickString(src, UnicodeConsumer.wrap(sb), charset);
        return sb.toString();
    }

    private void pickString(UnicodeProvider src, UnicodeConsumer dst, IntPredicate charset) {
        while (true) {
            if (src.hasNext()) {
                int ch = src.next();
                if (charset.test(ch)) {
                    dst.next(ch);
                } else {
                    src.back();
                    break;
                }
            } else {
                break;
            }
        }
    }

    /**
     * might return empty string
     */
    public String pickBackSlashEscapedString(UnicodeProvider src, IntPredicate charset) {
        StringBuilder sb = new StringBuilder();
        if (!tryPickBackSlashEscapedString(src, UnicodeConsumer.wrap(sb), charset)) {
            throw new ParsingException();
        }
        return sb.toString();
    }

    /**
     * return `true` if meet any of terminators<br/>
     * return `false` if `src` ends with escaping state<br/>
     * - `charset` always contains `\`<br/>
     * - `charset` always contains escaped unicodes<br/>
     * - a unicode not in charset is a terminator<br/>
     */
    private boolean tryPickBackSlashEscapedString(UnicodeProvider src, UnicodeConsumer dst, IntPredicate charset) {
        boolean isEscaping = false;
        while (true) {
            if (src.hasNext()) {
                int ch = src.next();
                if (isEscaping) {
                    isEscaping = false;
                    dst.next('\\');
                    dst.next(ch);
                } else if (ch == '\\') {
                    isEscaping = true;
                } else if (charset.test(ch)) {
                    dst.next(ch);
                } else {
                    src.back();
                    return true;
                }
            } else {
                return !isEscaping;
            }
        }
    }

    public String pickDottedIdentifierOrThrow(UnicodeProvider src) {
        StringBuilder sb = new StringBuilder();
        while (true) {
            sb.append(pickIdentifierOrThrow(src));
            if (src.hasNext()) {
                if (src.next() == '.') {
                    sb.append('.');
                } else {
                    src.back();
                    break;
                }
            } else {
                break;
            }
        }
        return sb.toString();
    }

    public String pickIdentifierOrThrow(UnicodeProvider src) {
        StringBuilder sb = new StringBuilder();
        if (!tryPickIdentifier(src, UnicodeConsumer.wrap(sb))) {
            throw new ParsingException();
        }
        return sb.toString();
    }

    /**
     * identifier matches [a-zA-Z_][a-zA-Z_0-9]*<br/>
     * return `false` if no identifier picked<br/>
     */
    private boolean tryPickIdentifier(UnicodeProvider src, UnicodeConsumer dst) {
        int stat = 0;
        while (true) {
            int ch = src.hasNext() ? src.next() : EOF;
            switch (stat) {
                case 0:
                    if (isAlpha(ch) || ch == '_') {
                        dst.next(ch);
                        stat = 1;
                    } else {
                        src.back();
                        return false;
                    }
                    break;
                case 1:
                    if (isAlpha(ch) || ch == '_' || isDigit(ch)) {
                        dst.next(ch);
                    } else {
                        src.back();
                        return true;
                    }
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    public void eatOneOrThrow(UnicodeProvider src, int expected) {
        if (!tryEatOne(src, ch -> ch == expected)) {
            if (src.hasNext()) {
                int value = src.next();
                src.back();
                throw new ParsingException(String.format("E: expected `0x%X`, actual `0x%X`", expected, value));
            } else {
                throw new ParsingException(String.format("E: expected `0x%X`, actual `EOF`", expected));
            }
        }
    }

    public boolean tryEatOne(UnicodeProvider src, int expected) {
        return tryEatOne(src, ch -> ch == expected);
    }

    public boolean tryEatOne(UnicodeProvider src, IntPredicate expected) {
        if (src.hasNext()) {
            if (expected.test(src.next())) {
                return true;
            } else {
                src.back();
                return false;
            }
        } else {
            return false;
        }
    }

    public void eatSequenceOrThrow(UnicodeProvider src, String s) {
        eatSequenceOrThrow(src, s.codePoints().iterator());
    }

    public void eatSequenceOrThrow(UnicodeProvider src, int... a) {
        eatSequenceOrThrow(src, Arrays.stream(a).iterator());
    }

    public void eatSequenceOrThrow(UnicodeProvider src, PrimitiveIterator.OfInt a) {
        while (a.hasNext()) {
            eatOneOrThrow(src, a.nextInt());
        }
    }

    /**
     * return num whitespaces eaten
     */
    public int eatWhitespaces(UnicodeProvider src) {
        int n = 0;
        while (src.hasNext()) {
            int ch = src.next();
            if (isWhitespace(ch)) {
                n++;
            } else {
                src.back();
                break;
            }
        }
        return n;
    }
}
