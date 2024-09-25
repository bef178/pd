package pd.fenc;

import pd.util.TextNumber;

import static pd.util.AsciiExtension.EOF;

public class NumberPicker {

    private final ScalarPicker scalarPicker = ScalarPicker.singleton();

    public String pickFloatToken(BackableUnicodeProvider src) {
        StringBuilder sb = new StringBuilder();
        UnicodeConsumer dst = UnicodeConsumer.wrap(sb);
        pickFloatToken(src, dst);
        return sb.toString();
    }

    /**
     * a number has 3 parts: integer, fraction and exponent
     */
    void pickFloatToken(BackableUnicodeProvider src, UnicodeConsumer dst) {
        pickIntToken(src, dst);

        int ch = src.hasNext() ? src.next() : EOF;
        if (ch == EOF) {
            return;
        } else if (ch == '.') {
            src.back();
            pickFractionPart(src, dst);
        } else {
            src.back();
        }

        ch = src.hasNext() ? src.next() : EOF;
        if (ch == EOF) {
            return;
        } else if (ch == 'E' || ch == 'e') {
            src.back();
            pickExponentPart(src, dst);
        } else {
            src.back();
        }

        ch = src.hasNext() ? src.next() : EOF;
        if (ch == EOF) {
            return;
        } else {
            src.back();
        }
    }

    public float pickFloat32(BackableUnicodeProvider src) {
        return Float.parseFloat(pickFloatToken(src));
    }

    public double pickFloat64(BackableUnicodeProvider src) {
        return Double.parseDouble(pickFloatToken(src));
    }

    /**
     * fraction := '.' 1*digit
     */
    void pickFractionPart(BackableUnicodeProvider src, UnicodeConsumer dst) {
        int state = 0;
        while (true) {
            switch (state) {
                case 0: {
                    scalarPicker.eat(src, '.');
                    dst.next('.');
                    state = 1;
                    break;
                }
                case 1: {
                    // seen '.'
                    int ch = src.hasNext() ? src.next() : EOF;
                    switch (ch) {
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            dst.next(ch);
                            state = 2;
                            break;
                        default:
                            throw new ParsingException(ch);
                    }
                    break;
                }
                case 2: {
                    // a valid fraction is recognized
                    int ch = src.hasNext() ? src.next() : EOF;
                    switch (ch) {
                        case EOF:
                            return;
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            dst.next(ch);
                            break;
                        default:
                            src.back();
                            return;
                    }
                    break;
                }
                default:
                    throw new IllegalStateException();
            }
        }
    }

    /**
     * exponent := ('E' / 'e') int
     */
    void pickExponentPart(BackableUnicodeProvider src, UnicodeConsumer dst) {
        int ch = src.hasNext() ? src.next() : EOF;
        if (ch == 'E' || ch == 'e') {
            dst.next(ch);
            pickIntToken(src, dst);
            return;
        }
        String actual = new String(Character.toChars(ch));
        throw new ParsingException(String.format("unexpected [%s], expecting [E] or [e]", actual));
    }

    public String pickIntToken(BackableUnicodeProvider src) {
        StringBuilder sb = new StringBuilder();
        UnicodeConsumer dst = UnicodeConsumer.wrap(sb);
        pickIntToken(src, dst);
        return sb.toString();
    }

    /**
     * pick a valid 10-based integer of string form, per intuition
     */
    void pickIntToken(BackableUnicodeProvider src, UnicodeConsumer dst) {
        int state = 0;
        while (true) {
            switch (state) {
                case 0: {
                    int ch = src.hasNext() ? src.next() : EOF;
                    switch (ch) {
                        case '-':
                            dst.next(ch);
                            state = 1;
                            break;
                        case '0':
                            dst.next(ch);
                            return;
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            dst.next(ch);
                            state = 3;
                            break;
                        default:
                            throw new ParsingException(ch);
                    }
                    break;
                }
                case 1: {
                    // seen the negative sign
                    int ch = src.hasNext() ? src.next() : EOF;
                    switch (ch) {
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            dst.next(ch);
                            state = 3;
                            break;
                        default:
                            // specially, "-0" is not acceptable
                            throw new ParsingException(ch);
                    }
                    break;
                }
                case 3: {
                    // recognized a valid int
                    int ch = src.hasNext() ? src.next() : EOF;
                    switch (ch) {
                        case EOF:
                            return;
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            dst.next(ch);
                            break;
                        default:
                            src.back();
                            return;
                    }
                    break;
                }
            }
        }
    }

    public int pickInt32(BackableUnicodeProvider src) {
        return Integer.parseInt(pickIntToken(src));
    }

    public long pickInt64(BackableUnicodeProvider src) {
        return Long.parseLong(pickIntToken(src));
    }

    public Number pickNumber(BackableUnicodeProvider src) {
        return new TextNumber(pickFloatToken(src));
    }
}
