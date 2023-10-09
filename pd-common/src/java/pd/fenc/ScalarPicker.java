package pd.fenc;

import pd.util.Int32ArrayExtension;

import static pd.fenc.Int32Provider.EOF;
import static pd.util.AsciiExtension.isAlpha;
import static pd.util.AsciiExtension.isDigit;

public class ScalarPicker {

    public String pickBackSlashEscapedString(UnicodeProvider src, int terminator) {
        StringBuilder sb = new StringBuilder();
        IWriter dst = IWriter.unicodeStream(sb);
        if (!tryPickBackSlashEscapedString(src, dst, terminator)) {
            throw new ParsingException();
        }
        return sb.toString();
    }

    public String pickDottedIdentifier(UnicodeProvider src) {
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (!pickIdentifier(src, IWriter.unicodeStream(sb))) {
                throw new ParsingException();
            }
            if (!src.hasNext() || src.next() != '.') {
                return sb.toString();
            }
            sb.append('.');
        }
    }

    public String pickIdentifier(UnicodeProvider src) {
        StringBuilder sb = new StringBuilder();
        if (!pickIdentifier(src, IWriter.unicodeStream(sb))) {
            throw new ParsingException();
        }
        return sb.toString();
    }

    /**
     * identifier matches [a-zA-Z_][a-zA-Z_0-9]*<br/>
     * if failed, src.next() will be the illegal character
     */
    private boolean pickIdentifier(UnicodeProvider src, IWriter dst) {
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

    public String pickString(UnicodeProvider src, int... closingSymbols) {
        StringBuilder sb = new StringBuilder();
        IWriter dst = IWriter.unicodeStream(sb);
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
    public boolean tryPickBackSlashEscapedString(UnicodeProvider src, IWriter dst, int terminator) {
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

    public boolean tryPickString(UnicodeProvider src, IWriter dst, int... terminators) {
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
}
