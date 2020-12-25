package pd.fenc;

import java.nio.charset.StandardCharsets;

import pd.ctype.Ctype;
import pd.fenc.ParsingException.Reason;

public class ScalarPicker {

    public static int nextSkippingWhitespaces(Int32Scanner it) {
        while (true) {
            int ch = it.hasNext() ? it.next() : IReader.EOF;
            if (!Ctype.isWhitespace(ch)) {
                return ch;
            }
        }
    }

    public static String pickDottedIdentifier(Int32Scanner it) {
        InstallmentByteBuffer dst = new InstallmentByteBuffer();
        while (true) {
            if (!pickIdentifier(it, dst)) {
                throw new ParsingException(Reason.NotIdentifier.toString());
            }
            if (!it.hasNext() || it.next() != '.') {
                return new String(dst.copyBytes());
            }
            dst.append('.');
        }
    }

    public static float pickFloat(Int32Scanner it) {
        InstallmentByteBuffer dst = new InstallmentByteBuffer();
        int stat = 0;
        while (true) {
            int ch = it.hasNext() ? it.next() : IReader.EOF;
            switch (stat) {
                case 0:
                    if (ch == '-') {
                        dst.append(ch);
                        stat = 1;
                    } else if (ch == '0') {
                        dst.append(ch);
                        stat = 2;
                    } else if (Ctype.isDigit(ch)) {
                        dst.append(ch);
                        stat = 3;
                    } else {
                        it.moveBack();
                        throw new ParsingException(String.format("Unexpected [%s]",
                                new String(Character.toChars(ch))));
                    }
                    break;
                case 1:
                    if (ch == '0') {
                        dst.append(ch);
                        stat = 4;
                    } else if (Ctype.isDigit(ch)) {
                        dst.append(ch);
                        stat = 3;
                    } else {
                        it.moveBack();
                        throw new ParsingException();
                    }
                    break;
                case 2:
                    if (ch == '.') {
                        dst.append(ch);
                        stat = 5;
                    } else if (Ctype.isDigit(ch)) {
                        it.moveBack();
                        throw new ParsingException();
                    } else {
                        it.moveBack();
                        return Float.parseFloat(new String(dst.copyBytes()));
                    }
                    break;
                case 3:
                    if (Ctype.isDigit(ch)) {
                        dst.append(ch);
                    } else if (ch == '.') {
                        dst.append(ch);
                        stat = 5;
                    } else {
                        it.moveBack();
                        return Float.parseFloat(new String(dst.copyBytes()));
                    }
                    break;
                case 4:
                    if (ch == '.') {
                        dst.append(ch);
                        stat = 5;
                    } else {
                        it.moveBack();
                        throw new ParsingException();
                    }
                    break;
                case 5:
                    if (ch == '0') {
                        dst.append(ch);
                    } else if (Ctype.isDigit(ch)) {
                        dst.append(ch);
                        stat = 6;
                    } else {
                        it.moveBack();
                        throw new ParsingException();
                    }
                    break;
                case 6:
                    if (ch == '0') {
                        dst.append(ch);
                        stat = 5;
                    } else if (Ctype.isDigit(ch)) {
                        dst.append(ch);
                    } else {
                        it.moveBack();
                        return Float.parseFloat(new String(dst.copyBytes()));
                    }
                    break;
            }
        }
    }

    public static String pickIdentifier(Int32Scanner it) {
        InstallmentByteBuffer dst = new InstallmentByteBuffer();
        if (!pickIdentifier(it, dst)) {
            throw new ParsingException(Reason.NotIdentifier);
        }
        return new String(dst.copyBytes());
    }

    private static boolean pickIdentifier(Int32Scanner it, IWriter dst) {
        int stat = 0;
        while (true) {
            int ch = it.hasNext() ? it.next() : IReader.EOF;
            switch (stat) {
                case 0:
                    if (Ctype.isAlpha(ch) || ch == '_') {
                        dst.append(ch);
                        stat = 1;
                    } else {
                        it.moveBack();
                        return false;
                    }
                    break;
                case 1:
                    if (Ctype.isAlpha(ch) || ch == '_' || Ctype.isDigit(ch)) {
                        dst.append(ch);
                    } else {
                        it.moveBack();
                        return true;
                    }
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    /**
     * check the current symbol matches
     * throw if no match
     *
     * return the number of symbols it consumed or negative iff error
     * specially, it may return 0, although it is ambiguous
     *
     * ok: 0, -1, 1
     * not ok: 00, -0
     */
    public static int pickInt(Int32Scanner it) {
        InstallmentByteBuffer dst = new InstallmentByteBuffer();
        int stat = 0;
        while (true) {
            int ch = it.next();
            switch (stat) {
                case 0:
                    if (ch == '-') {
                        dst.append(ch);
                        stat = 1;
                    } else if (ch == '0') {
                        dst.append(ch);
                        stat = 2;
                    } else if (Ctype.isDigit(ch)) {
                        dst.append(ch);
                        stat = 3;
                    } else {
                        it.moveBack();
                        throw new ParsingException();
                    }
                    break;
                case 1:
                    if (ch != '0' && Ctype.isDigit(ch)) {
                        dst.append(ch);
                        stat = 3;
                    } else {
                        it.moveBack();
                        throw new ParsingException();
                    }
                    break;
                case 2:
                    if (Ctype.isDigit(ch)) {
                        it.moveBack();
                        throw new ParsingException();
                    } else {
                        it.moveBack();
                        return Integer.parseInt(new String(dst.copyBytes()));
                    }
                case 3:
                    if (Ctype.isDigit(ch)) {
                        dst.append(ch);
                    } else {
                        it.moveBack();
                        return Integer.parseInt(new String(dst.copyBytes()));
                    }
                    break;
            }
        }
    }

    public static String pickString(Int32Scanner it) {
        return pickString(it, IReader.EOF, false);
    }

    public static String pickString(Int32Scanner it, int closingSymbol) {
        return pickString(it, closingSymbol, false);
    }

    /**
     * Return when meet closing symbol; throw when meet EOF if not silent.<br/>
     * The closing symbol will be reached but not consumed and not part of result.<br/>
     */
    public static String pickString(Int32Scanner it, int closingSymbol, boolean silent) {
        InstallmentByteBuffer dst = new InstallmentByteBuffer();
        Int32Scanner p = pickStringAsScanner(it, closingSymbol, silent);
        while (p.hasNext()) {
            dst.append(p.next());
        }
        return new String(dst.copyBytes(), StandardCharsets.UTF_8);
    }

    public static String pickString(Int32Scanner it, int openingSymbol, int closingSymbol) {
        if (!it.hasNext() || it.next() != openingSymbol) {
            throw new ParsingException();
        }
        return pickString(it, closingSymbol, false);
    }

    public static Int32Scanner pickStringAsScanner(Int32Scanner it, int closingSymbol,
            boolean silent) {

        return new Int32Scanner() {

            private boolean escaped = false;

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public int next() {
                int ch = it.next();
                if (escaped) {
                    escaped = false;
                    return ch;
                } else if (ch == '\\') {
                    escaped = true;
                    return ch;
                } else if (ch == closingSymbol) {
                    it.moveBack();
                    return IReader.EOF;
                } else if (ch == IReader.EOF) {
                    if (silent) {
                        it.moveBack();
                        return IReader.EOF;
                    } else {
                        throw new ParsingException("Unexpected EOF");
                    }
                } else {
                    return ch;
                }

            }
        };
    }
}
