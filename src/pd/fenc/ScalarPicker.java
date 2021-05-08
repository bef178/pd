package pd.fenc;

import static pd.fenc.IReader.EOF;

import pd.ctype.Ctype;
import pd.fenc.ParsingException.Reason;

public class ScalarPicker extends NumberPicker {

    public static String pickDottedIdentifier(Int32Scanner src) {
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (!pickIdentifier(src, ICharWriter.wrap(sb))) {
                throw new ParsingException(Reason.NotIdentifier);
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
            throw new ParsingException(Reason.NotIdentifier);
        }
        return sb.toString();
    }

    /**
     * identifier matches [a-zA-Z_][a-zA-Z_0-9]*
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

    public static String pickString(Int32Scanner it) {
        return pickString(it, EOF, false);
    }

    public static String pickString(Int32Scanner it, int closingSymbol) {
        return pickString(it, closingSymbol, false);
    }

    /**
     * Return when meet closing symbol; throw when meet EOF if not silent.<br/>
     * The closing symbol will be reached but not consumed and not part of result.<br/>
     */
    public static String pickString(Int32Scanner src, int closingSymbol, boolean silent) {
        StringBuilder sb = new StringBuilder();
        pickString(src, closingSymbol, silent, ICharWriter.wrap(sb));
        return sb.toString();
    }

    public static void pickString(Int32Scanner src, int closingSymbol, boolean silent, ICharWriter dst) {
        boolean isEscaped = false;
        while (true) {
            int ch = src.hasNext() ? src.next() : EOF;
            if (isEscaped) {
                isEscaped = false;
                dst.append(ch);
            } else if (ch == '\\') {
                isEscaped = true;
            } else if (ch == closingSymbol) {
                src.moveBack();
                return;
            } else if (ch == EOF) {
                if (silent) {
                    return;
                } else {
                    throw new ParsingException(EOF);
                }
            }
        }
    }

    public static String pickString(Int32Scanner it, int openingSymbol, int closingSymbol) {
        if (!it.hasNext() || it.next() != openingSymbol) {
            throw new ParsingException();
        }
        return pickString(it, closingSymbol, false);
    }

    private ScalarPicker() {
        // dummy
    }
}
