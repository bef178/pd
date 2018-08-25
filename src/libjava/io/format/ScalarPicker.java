package libjava.io.format;

import java.nio.charset.StandardCharsets;

import libjava.io.InstallmentByteBuffer;
import libjava.io.ParsingException;
import libjava.io.Pullable;
import libjava.primitive.Ctype;

public class ScalarPicker {

    public static int eatWhitespace(Pullable pullable) {
        int ch = pullable.pull();
        while (ch != Pullable.E_EOF) {
            if (!Ctype.isWhitespace(ch)) {
                break;
            }
            ch = pullable.pull();
        }
        return ch;
    }

    public static String pickDottedIdentifier(IntScanner it) {
        InstallmentByteBuffer buffer = new InstallmentByteBuffer();
        while (true) {
            pickIdentifier(it, buffer);
            if (buffer.size() == 0) {
                throw new ParsingException();
            }
            if (it.pull() != '.') {
                return new String(buffer.copyBytes());
            }
            buffer.push('.');
        }
    }

    public static float pickFloat(IntScanner it) {
        InstallmentByteBuffer buffer = new InstallmentByteBuffer();
        int stat = 0;
        while (true) {
            int ch = it.pull();
            switch (stat) {
                case 0:
                    if (ch == '-') {
                        buffer.push(ch);
                        stat = 1;
                    } else if (ch == '0') {
                        buffer.push(ch);
                        stat = 2;
                    } else if (Ctype.isDigit(ch)) {
                        buffer.push(ch);
                        stat = 3;
                    } else {
                        it.back();
                        throw new ParsingException(String.format("Unexpected %s", (char) ch));
                    }
                    break;
                case 1:
                    if (ch == '0') {
                        buffer.push(ch);
                        stat = 4;
                    } else if (Ctype.isDigit(ch)) {
                        buffer.push(ch);
                        stat = 3;
                    } else {
                        it.back();
                        throw new ParsingException();
                    }
                    break;
                case 2:
                    if (ch == '.') {
                        buffer.push(ch);
                        stat = 5;
                    } else if (Ctype.isDigit(ch)) {
                        it.back();
                        throw new ParsingException();
                    } else {
                        it.back();
                        return Float.parseFloat(new String(buffer.copyBytes()));
                    }
                    break;
                case 3:
                    if (Ctype.isDigit(ch)) {
                        buffer.push(ch);
                    } else if (ch == '.') {
                        buffer.push(ch);
                        stat = 5;
                    } else {
                        it.back();
                        return Float.parseFloat(new String(buffer.copyBytes()));
                    }
                    break;
                case 4:
                    if (ch == '.') {
                        buffer.push(ch);
                        stat = 5;
                    } else {
                        it.back();
                        throw new ParsingException();
                    }
                    break;
                case 5:
                    if (ch == '0') {
                        buffer.push(ch);
                    } else if (Ctype.isDigit(ch)) {
                        buffer.push(ch);
                        stat = 6;
                    } else {
                        it.back();
                        throw new ParsingException();
                    }
                    break;
                case 6:
                    if (ch == '0') {
                        buffer.push(ch);
                        stat = 5;
                    } else if (Ctype.isDigit(ch)) {
                        buffer.push(ch);
                    } else {
                        it.back();
                        return Float.parseFloat(new String(buffer.copyBytes()));
                    }
                    break;
            }
        }
    }

    public static String pickIdentifier(IntScanner it) {
        InstallmentByteBuffer buffer = new InstallmentByteBuffer();
        pickIdentifier(it, buffer);
        return new String(buffer.copyBytes());
    }

    private static void pickIdentifier(IntScanner it, InstallmentByteBuffer buffer) {
        int stat = 0;
        while (true) {
            int ch = it.pull();
            switch (stat) {
                case 0:
                    if (Ctype.isAlphabetic(ch) || ch == '_') {
                        buffer.push(ch);
                        stat = 1;
                    } else {
                        it.back();
                        throw new ParsingException();
                    }
                    break;
                case 1:
                    if (Ctype.isAlphabetic(ch) || ch == '_' || Ctype.isDigit(ch)) {
                        buffer.push(ch);
                    } else {
                        it.back();
                        return;
                    }
                    break;
                default:
                    break;
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
    public static int pickInt(IntScanner it) {
        InstallmentByteBuffer buffer = new InstallmentByteBuffer();
        int stat = 0;
        while (true) {
            int ch = it.pull();
            switch (stat) {
                case 0:
                    if (ch == '-') {
                        buffer.push(ch);
                        stat = 1;
                    } else if (ch == '0') {
                        buffer.push(ch);
                        stat = 2;
                    } else if (Ctype.isDigit(ch)) {
                        buffer.push(ch);
                        stat = 3;
                    } else {
                        it.back();
                        throw new ParsingException();
                    }
                    break;
                case 1:
                    if (ch != '0' && Ctype.isDigit(ch)) {
                        buffer.push(ch);
                        stat = 3;
                    } else {
                        it.back();
                        throw new ParsingException();
                    }
                    break;
                case 2:
                    if (Ctype.isDigit(ch)) {
                        it.back();
                        throw new ParsingException();
                    } else {
                        it.back();
                        return Integer.parseInt(
                                new String(buffer.copyBytes()));
                    }
                case 3:
                    if (Ctype.isDigit(ch)) {
                        buffer.push(ch);
                    } else {
                        it.back();
                        return Integer.parseInt(
                                new String(buffer.copyBytes()));
                    }
                    break;
            }
        }
    }

    public static String pickString(IntScanner it) {
        return pickString(it, Pullable.E_EOF);
    }

    public static String pickString(IntScanner it, int closingSymbol) {
        return pickString(it, closingSymbol, false);
    }

    /**
     * Return when meet closing symbol; throw when meet EOF if not silent.<br/>
     * The closing symbol will be reached but not consumed and not part of result.<br/>
     */
    public static String pickString(IntScanner it, int closingSymbol, boolean silent) {
        InstallmentByteBuffer buffer = new InstallmentByteBuffer();
        boolean escaped = false;
        while (true) { // not break on EOF
            int ch = it.pull();
            if (escaped) {
                buffer.push(ch);
                escaped = false;
            } else if (ch == '\\') {
                buffer.push(ch);
                escaped = true;
            } else if (ch == closingSymbol) {
                it.back();
                return new String(buffer.copyBytes(), StandardCharsets.UTF_8);
            } else if (ch == Pullable.E_EOF) {
                if (silent) {
                    it.back();
                    return new String(buffer.copyBytes(), StandardCharsets.UTF_8);
                } else {
                    throw new ParsingException("Unexpected EOF");
                }
            } else {
                buffer.push(ch);
            }
        }
    }

    public static String pickString(IntScanner it, int openingSymbol, int closingSymbol) {
        if (it.pull() != openingSymbol) {
            throw new ParsingException();
        }
        return pickString(it, closingSymbol);
    }
}
