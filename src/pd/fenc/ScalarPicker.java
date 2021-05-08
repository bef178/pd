package pd.fenc;

import static pd.fenc.IReader.EOF;

import pd.ctype.Ctype;

public class ScalarPicker extends NumberPicker {

    public static String pickDottedIdentifier(Int32Scanner src) {
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (!pickIdentifier(src, ICharWriter.wrap(sb))) {
                throw new ParsingException();
            }
            if (!src.hasNext() || src.next() != '.') {
                return sb.toString();
            }
            sb.append('.');
        }
    }

    public static String pickIdentifier(Int32Scanner src) {
        StringBuilder sb = new StringBuilder();
        if (!pickIdentifier(src, ICharWriter.wrap(sb))) {
            throw new ParsingException();
        }
        return sb.toString();
    }

    /**
     * identifier matches [a-zA-Z_][a-zA-Z_0-9]*<br/>
     * if fail, src.next() will be the illegal character
     */
    private static boolean pickIdentifier(Int32Scanner src, ICharWriter dst) {
        int stat = 0;
        while (true) {
            int ch = src.hasNext() ? src.next() : EOF;
            switch (stat) {
                case 0:
                    if (Ctype.isAlpha(ch) || ch == '_') {
                        dst.append(ch);
                        stat = 1;
                    } else {
                        src.moveBack();
                        return false;
                    }
                    break;
                case 1:
                    if (Ctype.isAlpha(ch) || ch == '_' || Ctype.isDigit(ch)) {
                        dst.append(ch);
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

    public static String pickString(Int32Scanner src) {
        return pickString(src, EOF);
    }

    public static String pickString(Int32Scanner src, int closingSymbol) {
        StringBuilder sb = new StringBuilder();
        if (!pickString(src, closingSymbol, ICharWriter.wrap(sb))) {
            throw new ParsingException();
        }
        return sb.toString();
    }

    /**
     * Will succ when meet closing symbol or fail when meet EOF; however EOF can be the closing symbol<br/>
     * The closing symbol will not be consumed and not be a part of result<br/>
     */
    public static boolean pickString(Int32Scanner src, int closingSymbol, ICharWriter dst) {
        boolean isEscaped = false;
        while (true) {
            int ch = src.hasNext() ? src.next() : EOF;
            if (isEscaped) {
                isEscaped = false;
                dst.append('\\');
                dst.append(ch);
            } else if (ch == '\\') {
                isEscaped = true;
            } else if (ch == closingSymbol) {
                if (closingSymbol != EOF) {
                    src.moveBack();
                }
                return true;
            } else if (ch == EOF) {
                return false;
            } else {
                dst.append(ch);
            }
        }
    }

    private ScalarPicker() {
        // dummy
    }
}
