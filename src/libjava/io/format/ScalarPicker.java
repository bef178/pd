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

    public static String pickDottedIdentifier(IntScanner scanner) {
        InstallmentByteBuffer buffer = new InstallmentByteBuffer();
        while (true) {
            pickIdentifier(scanner, buffer);
            if (buffer.size() == 0) {
                throw new ParsingException();
            }
            if (scanner.pull() != '.') {
                return new String(buffer.copyBytes());
            }
            buffer.push('.');
            scanner.pull();
        }
    }

    /**
     * ok: 0, 1, -1, 1.01, -1.1
     * not ok: 00, -0, 0., .1, -0.0, 0.0
     */
    public static float pickFloat(IntScanner scanner) {
        InstallmentByteBuffer buffer = new InstallmentByteBuffer();
        int stat = 0;
        while (true) {
            int ch = scanner.pull();
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
                        scanner.back();
                        throw new ParsingException(String.format("Unexpected %s", (char) ch));
                    }
                    break;
                case 1:
                    if (ch == '0') {
                        buffer.push(ch);
                        stat = 4;
                    }
                    if (Ctype.isDigit(ch)) {
                        buffer.push(ch);
                        stat = 3;
                    } else {
                        scanner.back();
                        throw new ParsingException();
                    }
                    break;
                case 2:
                    if (ch == '.') {
                        buffer.push(ch);
                        stat = 5;
                    } else if (Ctype.isDigit(ch)) {
                        scanner.back();
                        throw new ParsingException();
                    } else {
                        scanner.back();
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
                        scanner.back();
                        throw new ParsingException();
                    }
                    break;
                case 4:
                    if (ch == '.') {
                        buffer.push(ch);
                        stat = 5;
                    } else {
                        scanner.back();
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
                        scanner.back();
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
                        scanner.back();
                        return Float.parseFloat(new String(buffer.copyBytes()));
                    }
                    break;
            }
        }
    }

    public static String pickIdentifier(IntScanner scanner) {
        InstallmentByteBuffer buffer = new InstallmentByteBuffer();
        pickIdentifier(scanner, buffer);
        return new String(buffer.copyBytes());
    }

    private static void pickIdentifier(IntScanner scanner, InstallmentByteBuffer pusher) {
        int stat = 0;
        while (true) {
            int ch = scanner.pull();
            switch (stat) {
                case 0:
                    if (Ctype.isAlphabetic(ch) || ch == '_') {
                        pusher.push(ch);
                        stat = 1;
                    } else {
                        scanner.back();
                        throw new ParsingException();
                    }
                    break;
                case 1:
                    if (Ctype.isAlphabetic(ch) || ch == '_' || Ctype.isDigit(ch)) {
                        pusher.push(ch);
                    } else {
                        scanner.back();
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
    public static int pickInt(IntScanner scanner) {
        InstallmentByteBuffer buffer = new InstallmentByteBuffer();
        int stat = 0;
        while (true) {
            int ch = scanner.pull();
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
                        scanner.back();
                        throw new ParsingException();
                    }
                    break;
                case 1:
                    if (ch != '0' && Ctype.isDigit(ch)) {
                        buffer.push(ch);
                        stat = 3;
                    } else {
                        scanner.back();
                        throw new ParsingException();
                    }
                    break;
                case 2:
                    if (Ctype.isDigit(ch)) {
                        scanner.back();
                        throw new ParsingException();
                    } else {
                        scanner.back();
                        return Integer.parseInt(
                                new String(buffer.copyBytes()));
                    }
                case 3:
                    if (Ctype.isDigit(ch)) {
                        buffer.push(ch);
                    } else {
                        scanner.back();
                        return Integer.parseInt(
                                new String(buffer.copyBytes()));
                    }
                    break;
            }
        }
    }

    public static String pickString(Pullable pullable) {
        return pickString(pullable, Pullable.E_EOF);
    }

    public static String pickString(Pullable pullable, int closingSymbol) {
        return pickString(pullable, closingSymbol, false);
    }

    /**
     * Return when meet closing symbol, throw when meet EOF if not silent.<br/>
     * The closing symbol will be consumed and will not be part of result.<br/>
     */
    public static String pickString(Pullable pullable, int closingSymbol, boolean silent) {
        InstallmentByteBuffer buffer = new InstallmentByteBuffer();
        boolean escaped = false;
        while (true) { // not break on EOF
            int ch = pullable.pull();
            if (escaped) {
                buffer.push(ch);
                escaped = false;
            } else if (ch == '\\') {
                buffer.push(ch);
                escaped = true;
            } else if (ch == closingSymbol) {
                return new String(buffer.copyBytes(), StandardCharsets.UTF_8);
            } else if (ch == Pullable.E_EOF) {
                if (silent) {
                    return new String(buffer.copyBytes(), StandardCharsets.UTF_8);
                } else {
                    throw new ParsingException("Unexpected EOF");
                }
            } else {
                buffer.push(ch);
            }
        }
    }

    public static String pickString(Pullable pullable, int openingSymbol, int closingSymbol) {
        if (pullable.pull() != openingSymbol) {
            throw new ParsingException();
        }
        return pickString(pullable, closingSymbol);
    }
}
