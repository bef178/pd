package pd.fenc;

import static pd.fenc.IReader.EOF;
import static pd.util.AsciiUtil.isAlpha;
import static pd.util.AsciiUtil.isDigit;
import static pd.util.Int32ArrayExtension.contains;

public class ScalarPicker extends NumberPicker {

    public String pickBackSlashEscapedString(CharReader src, int terminator) {
        StringBuilder sb = new StringBuilder();
        IWriter dst = IWriter.unicodeStream(sb);
        if (!tryPickBackSlashEscapedString(src, dst, terminator)) {
            throw new ParsingException();
        }
        return sb.toString();
    }

    public String pickDottedIdentifier(CharReader src) {
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

    public String pickIdentifier(CharReader src) {
        StringBuilder sb = new StringBuilder();
        if (!pickIdentifier(src, IWriter.unicodeStream(sb))) {
            throw new ParsingException();
        }
        return sb.toString();
    }

    /**
     * identifier matches [a-zA-Z_][a-zA-Z_0-9]*<br/>
     * if fail, src.next() will be the illegal character
     */
    private boolean pickIdentifier(CharReader src, IWriter dst) {
        int stat = 0;
        while (true) {
            int ch = src.hasNext() ? src.next() : EOF;
            switch (stat) {
                case 0:
                    if (isAlpha(ch) || ch == '_') {
                        dst.push(ch);
                        stat = 1;
                    } else {
                        src.moveBack();
                        return false;
                    }
                    break;
                case 1:
                    if (isAlpha(ch) || ch == '_' || isDigit(ch)) {
                        dst.push(ch);
                    } else {
                        src.moveBack();
                        return true;
                    }
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    public String pickString(CharReader src, int... closingSymbols) {
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
    public boolean tryPickBackSlashEscapedString(CharReader src, IWriter dst, int terminator) {
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
                    src.moveBack();
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

    public boolean tryPickString(CharReader src, IWriter dst, int... terminators) {
        while (true) {
            int ch = src.hasNext() ? src.next() : EOF;
            if (contains(terminators, ch)) {
                if (ch != EOF) {
                    src.moveBack();
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