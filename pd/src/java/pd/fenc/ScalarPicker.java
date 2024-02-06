package pd.fenc;

import java.util.PrimitiveIterator;

import pd.util.AsciiExtension;
import pd.util.Int32ArrayExtension;

import static pd.util.AsciiExtension.EOF;
import static pd.util.AsciiExtension.isAlpha;
import static pd.util.AsciiExtension.isDigit;

public class ScalarPicker {

    private static final ScalarPicker one = new ScalarPicker();

    public static ScalarPicker singleton() {
        return one;
    }

    private ScalarPicker() {
        // dummy
    }

    public String pickBackSlashEscapedString(BackableUnicodeProvider src, int terminator) {
        StringBuilder sb = new StringBuilder();
        UnicodeConsumer dst = UnicodeConsumer.wrap(sb);
        if (!tryPickBackSlashEscapedString(src, dst, terminator)) {
            throw new ParsingException();
        }
        return sb.toString();
    }

    public String pickDottedIdentifier(BackableUnicodeProvider src) {
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (!pickIdentifier(src, UnicodeConsumer.wrap(sb))) {
                throw new ParsingException();
            }
            if (!src.hasNext() || src.next() != '.') {
                return sb.toString();
            }
            sb.append('.');
        }
    }

    public String pickIdentifier(BackableUnicodeProvider src) {
        StringBuilder sb = new StringBuilder();
        if (!pickIdentifier(src, UnicodeConsumer.wrap(sb))) {
            throw new ParsingException();
        }
        return sb.toString();
    }

    /**
     * identifier matches [a-zA-Z_][a-zA-Z_0-9]*<br/>
     * if failed, src.next() will be the illegal character
     */
    private boolean pickIdentifier(BackableUnicodeProvider src, UnicodeConsumer dst) {
        int stat = 0;
        while (true) {
            int ch = src.hasNext() ? src.next() : EOF;
            switch (stat) {
                case 0:
                    if (isAlpha(ch) || ch == '_') {
                        dst.push(ch);
                        stat = 1;
                    } else {
                        src.back();
                        return false;
                    }
                    break;
                case 1:
                    if (isAlpha(ch) || ch == '_' || isDigit(ch)) {
                        dst.push(ch);
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

    public String pickString(BackableUnicodeProvider src, int... closingSymbols) {
        StringBuilder sb = new StringBuilder();
        UnicodeConsumer dst = UnicodeConsumer.wrap(sb);
        if (!tryPickString(src, dst, closingSymbols)) {
            throw new ParsingException("Unexpected EOF");
        }
        return sb.toString();
    }

    /**
     * Will succ when meet `terminator`<br/>
     * - `terminator` will not be consumed and not be a part of result<br/>
     * - `terminator` can be escaped by `\`<br/>
     * - `terminator` can be `EOF`<br/>
     * Will fail in front of `EOF`<br/>
     */
    public boolean tryPickBackSlashEscapedString(BackableUnicodeProvider src, UnicodeConsumer dst, int terminator) {
        boolean isEscaping = false;
        while (true) {
            int ch = src.hasNext() ? src.next() : EOF;
            if (isEscaping) {
                isEscaping = false;
                dst.push('\\');
                dst.push(ch);
            } else if (ch == '\\') {
                isEscaping = true;
            } else if (ch == terminator) {
                if (ch != EOF) {
                    src.back();
                }
                return true;
            } else if (ch == EOF) {
                // unexpected EOF
                return false;
            } else {
                dst.push(ch);
            }
        }
    }

    public boolean tryPickString(BackableUnicodeProvider src, UnicodeConsumer dst, int... terminators) {
        while (true) {
            int ch = src.hasNext() ? src.next() : EOF;
            if (Int32ArrayExtension.contains(terminators, ch)) {
                if (ch != EOF) {
                    src.back();
                }
                return true;
            } else if (ch == EOF) {
                // unexpected EOF
                return false;
            } else {
                dst.push(ch);
            }
        }
    }

    public void eat(BackableUnicodeProvider src, int expected) {
        if (!tryEat(src, expected)) {
            if (src.hasNext()) {
                int value = src.next();
                src.back();
                throw new ParsingException(String.format("E: expected `0x%X`, actual `0x%X`", expected, value));
            } else {
                throw new ParsingException(String.format("E: expected `0x%X`, actual `EOF`", expected));
            }
        }
    }

    /**
     * will stop in front of unexpected value
     */
    public boolean tryEat(BackableUnicodeProvider src, int expected) {
        if (src.hasNext()) {
            if (src.next() == expected) {
                return true;
            } else {
                src.back();
                return false;
            }
        } else {
            return expected == EOF;
        }
    }

    public void eat(BackableUnicodeProvider src, String s) {
        PrimitiveIterator.OfInt ofInt = s.codePoints().iterator();
        while (ofInt.hasNext()) {
            int expected = ofInt.nextInt();
            eat(src, expected);
        }
    }

    public boolean tryEat(BackableUnicodeProvider src, String s) {
        PrimitiveIterator.OfInt ofInt = s.codePoints().iterator();
        while (ofInt.hasNext()) {
            int expected = ofInt.nextInt();
            if (!tryEat(src, expected)) {
                return false;
            }
        }
        return true;
    }

    public void eatWhitespacesIfAny(BackableUnicodeProvider src) {
        while (src.hasNext()) {
            int ch = src.next();
            if (!AsciiExtension.isWhitespace(ch)) {
                src.back();
                return;
            }
        }
    }
}
