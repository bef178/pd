package libcliff.io.codec;

import libcliff.io.Pushable;
import libcliff.primitive.Ctype;

public class Parser {

    private static final int EOF = -1;
    private static final int LF = '\n';

    public static int parseDottedIdentifier(EchoedPullablePipe pullable,
            Pushable pushable) {
        int n = 0;
        while (true) {
            int i = parseIdentifier(pullable, pushable);
            if (i <= 0) {
                return i;
            }
            n += i;
            if (pullable.echo() != '.') {
                return n;
            }
            pullable.pull();
            ++n;
        }
    }

    public static int parseIdentifier(EchoedPullablePipe pullable,
            Pushable pushable) {

        int ch = pullable.echo();
        if (!Ctype.isUpper(ch) && !Ctype.isLower(ch) && ch != '_') {
            return -1;
        }

        pushable.push(ch);
        int n = 1;
        while (true) {
            ch = pullable.pull();
            if (Ctype.isUpper(ch) || Ctype.isLower(ch) || ch == '_'
                    || Ctype.isDigit(ch)) {
                pushable.push(ch);
                ++n;
            } else {
                return n;
            }
        }
    }

    public static int parseQuotedString(EchoedPullablePipe pullable,
            Pushable pushable) {
        return parseString('\"', '\"', pullable, pushable);
    }

    /**
     * closing when meet closingSymbol or throw when meet EOF<br/>
     * closingSymbol will be consumed<br/>
     */
    public static int parseString(int closingSymbol,
            EchoedPullablePipe pullable, Pushable pushable) {
        // don't directly push in case ch == '\\'
        int ch = pullable.echo();
        int n = 0;
        boolean escaped = false;
        while (true) {
            if (escaped) {
                pushable.push(ch);
                ++n;
                escaped = false;
            } else if (ch == '\\') {
                pushable.push(ch);
                ++n;
                escaped = true;
            } else if (ch == closingSymbol) {
                pullable.pull(); // eat one to match those with inexplicit delimiters
                return n;
            } else if (ch == EOF) {
                throw new ParsingException();
            } else {
                pushable.push(ch);
                ++n;
            }
            ch = pullable.pull();
        }
    }

    public static int parseString(int openingSymbol, int closingSymbol,
            EchoedPullablePipe pullable, Pushable pushable) {
        if (pullable.echo() != openingSymbol) {
            return -1;
        }
        pullable.pull();
        return parseString(closingSymbol, pullable, pushable);
    }
}
