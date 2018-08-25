package libjava.io.format;

import java.nio.charset.StandardCharsets;

import libjava.io.InstallmentByteBuffer;
import libjava.io.ParsingException;
import libjava.io.Pullable;
import libjava.io.PullablePipe;
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
        PullablePipe p = pickStringAsPullablePipe(it, closingSymbol, silent);
        for (int ch = p.pull(); ch != Pullable.E_EOF; ch = p.pull()) {
            buffer.append(ch);
        }
        return new String(buffer.copyBytes(), StandardCharsets.UTF_8);
    }

    public static String pickString(IntScanner it, int openingSymbol, int closingSymbol) {
        if (it.pull() != openingSymbol) {
            throw new ParsingException();
        }
        return pickString(it, closingSymbol);
    }

    public static PullablePipe pickStringAsPullablePipe(IntScanner it, int closingSymbol,
            boolean silent) {

        return new PullablePipe() {

            private IntScanner upstream = it;

            private boolean escaped = false;

            @Override
            public <T extends Pullable> T join(T upstream) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int pull() {
                int ch = it.pull();
                if (escaped) {
                    escaped = false;
                    return ch;
                } else if (ch == '\\') {
                    escaped = true;
                    return ch;
                } else if (ch == closingSymbol) {
                    upstream.back();
                    return Pullable.E_EOF;
                } else if (ch == Pullable.E_EOF) {
                    if (silent) {
                        it.back();
                        return Pullable.E_EOF;
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
