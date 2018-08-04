package libjava.io.format;

import java.nio.charset.StandardCharsets;

import libjava.io.InstallmentByteBuffer;
import libjava.io.ParsingException;
import libjava.io.Pullable;
import libjava.primitive.Ctype;

public class Parser {

    public static int eatWhitespace(Pullable pullable) {
        int ch = pullable.pull();
        while (ch != Pullable.EOF) {
            if (!Ctype.isWhitespace(ch)) {
                break;
            }
            ch = pullable.pull();
        }
        return ch;
    }

    public static String pickDottedIdentifier(Feeder puller) {
        InstallmentByteBuffer pusher = new InstallmentByteBuffer();
        while (true) {
            pickIdentifier(puller, pusher);
            if (pusher.size() == 0) {
                throw new ParsingException();
            }
            if (puller.pull() != '.') {
                return new String(pusher.copyBytes());
            }
            pusher.push('.');
            puller.pull();
        }
    }

    /**
     * ok: 0, 1, -1, 1.01, -1.1
     * not ok: 00, -0, 0., .1, -0.0, 0.0
     */
    public static float pickFloat(Feeder puller) {
        InstallmentByteBuffer pusher = new InstallmentByteBuffer();
        int stat = 0;
        while (true) {
            int ch = puller.pull();
            switch (stat) {
                case 0:
                    if (ch == '-') {
                        pusher.push(ch);
                        stat = 1;
                    } else if (ch == '0') {
                        pusher.push(ch);
                        stat = 2;
                    } else if (Ctype.isDigit(ch)) {
                        pusher.push(ch);
                        stat = 3;
                    } else {
                        puller.back();
                        throw new ParsingException(
                                String.format("Unexpected %s at %d",
                                        (char) ch, puller.position()));
                    }
                    break;
                case 1:
                    if (ch == '0') {
                        pusher.push(ch);
                        stat = 4;
                    }
                    if (Ctype.isDigit(ch)) {
                        pusher.push(ch);
                        stat = 3;
                    } else {
                        puller.back();
                        throw new ParsingException();
                    }
                    break;
                case 2:
                    if (ch == '.') {
                        pusher.push(ch);
                        stat = 5;
                    } else if (Ctype.isDigit(ch)) {
                        puller.back();
                        throw new ParsingException();
                    } else {
                        puller.back();
                        return Float.parseFloat(
                                new String(pusher.copyBytes()));
                    }
                    break;
                case 3:
                    if (Ctype.isDigit(ch)) {
                        pusher.push(ch);
                    } else if (ch == '.') {
                        pusher.push(ch);
                        stat = 5;
                    } else {
                        puller.back();
                        throw new ParsingException();
                    }
                    break;
                case 4:
                    if (ch == '.') {
                        pusher.push(ch);
                        stat = 5;
                    } else {
                        puller.back();
                        throw new ParsingException();
                    }
                    break;
                case 5:
                    if (ch == '0') {
                        pusher.push(ch);
                    } else if (Ctype.isDigit(ch)) {
                        pusher.push(ch);
                        stat = 6;
                    } else {
                        puller.back();
                        throw new ParsingException();
                    }
                    break;
                case 6:
                    if (ch == '0') {
                        pusher.push(ch);
                        stat = 5;
                    } else if (Ctype.isDigit(ch)) {
                        pusher.push(ch);
                    } else {
                        puller.back();
                        return Float.parseFloat(
                                new String(pusher.copyBytes()));
                    }
                    break;
            }
        }
    }

    public static String pickIdentifier(Feeder puller) {
        InstallmentByteBuffer pusher = new InstallmentByteBuffer();
        pickIdentifier(puller, pusher);
        return new String(pusher.copyBytes());
    }

    private static void pickIdentifier(Feeder puller,
            InstallmentByteBuffer pusher) {
        int stat = 0;
        while (true) {
            int ch = puller.pull();
            switch (stat) {
                case 0:
                    if (Ctype.isUpper(ch) || Ctype.isLower(ch) || ch == '_') {
                        pusher.push(ch);
                        stat = 1;
                    } else {
                        puller.back();
                        throw new ParsingException();
                    }
                    break;
                case 1:
                    if (Ctype.isUpper(ch) || Ctype.isLower(ch) || ch == '_'
                            || Ctype.isDigit(ch)) {
                        pusher.push(ch);
                    } else {
                        puller.back();
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
    public static int pickInt(Feeder puller) {
        InstallmentByteBuffer pusher = new InstallmentByteBuffer();
        int stat = 0;
        while (true) {
            int ch = puller.pull();
            switch (stat) {
                case 0:
                    if (ch == '-') {
                        pusher.push(ch);
                        stat = 1;
                    } else if (ch == '0') {
                        pusher.push(ch);
                        stat = 2;
                    } else if (Ctype.isDigit(ch)) {
                        pusher.push(ch);
                        stat = 3;
                    } else {
                        puller.back();
                        throw new ParsingException();
                    }
                    break;
                case 1:
                    if (ch != '0' && Ctype.isDigit(ch)) {
                        pusher.push(ch);
                        stat = 3;
                    } else {
                        puller.back();
                        throw new ParsingException();
                    }
                    break;
                case 2:
                    if (Ctype.isDigit(ch)) {
                        puller.back();
                        throw new ParsingException();
                    } else {
                        puller.back();
                        return Integer.parseInt(
                                new String(pusher.copyBytes()));
                    }
                case 3:
                    if (Ctype.isDigit(ch)) {
                        pusher.push(ch);
                    } else {
                        puller.back();
                        return Integer.parseInt(
                                new String(pusher.copyBytes()));
                    }
                    break;
            }
        }
    }

    public static String pickString(Feeder puller) {
        return pickString(puller, Pullable.EOF);
    }

    public static String pickString(Pullable puller, int closingSymbol) {
        return pickString(puller, closingSymbol, false);
    }

    /**
     * Return when meet closing symbol, throw when meet EOF with unmuted.<br/>
     * The closing symbol will be consumed and will not be part of result.<br/>
     */
    public static String pickString(Pullable puller, int closingSymbol,
            boolean muted) {
        InstallmentByteBuffer pusher = new InstallmentByteBuffer();
        boolean escaped = false;
        while (true) { // not break on EOF
            int ch = puller.pull();
            if (escaped) {
                pusher.push(ch);
                escaped = false;
            } else if (ch == '\\') {
                pusher.push(ch);
                escaped = true;
            } else if (ch == closingSymbol) {
                return new String(pusher.copyBytes(), StandardCharsets.UTF_8);
            } else if (ch == Pullable.EOF) {
                if (muted) {
                    return new String(pusher.copyBytes(),
                            StandardCharsets.UTF_8);
                } else {
                    throw new ParsingException("Unexpected EOF");
                }
            } else {
                pusher.push(ch);
            }
        }
    }

    public static String pickString(Pullable puller, int openingSymbol,
            int closingSymbol) {
        if (puller.pull() != openingSymbol) {
            throw new ParsingException();
        }
        return pickString(puller, closingSymbol);
    }
}
