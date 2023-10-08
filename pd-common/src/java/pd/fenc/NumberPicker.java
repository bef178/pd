package pd.fenc;

import static pd.fenc.Int32Provider.EOF;

class NumberPicker {

    /**
     * exponent := ('E' / 'e') int
     */
    void pickExponent(UnicodeProvider src, IWriter dst) {
        int ch = src.hasNext() ? src.next() : EOF;
        if (ch == 'E' || ch == 'e') {
            dst.push(ch);
            pickInt(src, dst);
            return;
        }
        String actual = new String(Character.toChars(ch));
        throw new ParsingException(String.format("unexpected [%s], expecting [E] or [e]", actual));
    }

    /**
     * a number has 3 parts: integer, fraction and exponent
     */
    public void pickFloat(UnicodeProvider src, IWriter dst) {
        pickInt(src, dst);

        int ch = src.hasNext() ? src.next() : EOF;
        if (ch == EOF) {
            return;
        } else if (ch == '.') {
            src.back();
            pickFraction(src, dst);
        } else {
            src.back();
        }

        ch = src.hasNext() ? src.next() : EOF;
        if (ch == EOF) {
            return;
        } else if (ch == 'E' || ch == 'e') {
            src.back();
            pickExponent(src, dst);
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

    public float pickFloat32(UnicodeProvider src) {
        StringBuilder sb = new StringBuilder();
        pickFloat(src, IWriter.unicodeStream(sb));
        return Float.parseFloat(sb.toString());
    }

    public double pickFloat64(UnicodeProvider src) {
        StringBuilder sb = new StringBuilder();
        pickFloat(src, IWriter.unicodeStream(sb));
        return Double.parseDouble(sb.toString());
    }

    /**
     * fraction := '.' 1*digit<br/>
     * <br/>
     * specially, "0.0" is valid<br/>
     */
    void pickFraction(UnicodeProvider src, IWriter dst) {
        int state = 0;
        while (true) {
            switch (state) {
                case 0: {
                    src.eat('.');
                    dst.push('.');
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
                            dst.push(ch);
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
                            dst.push(ch);
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
     * pick a valid 10-based integer of string form, per intuition
     */
    void pickInt(UnicodeProvider src, IWriter dst) {
        int state = 0;
        while (true) {
            switch (state) {
                case 0: {
                    int ch = src.hasNext() ? src.next() : EOF;
                    switch (ch) {
                        case '-':
                            dst.push(ch);
                            state = 1;
                            break;
                        case '0':
                            dst.push(ch);
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
                            dst.push(ch);
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
                            dst.push(ch);
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
                            dst.push(ch);
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

    public int pickInt32(UnicodeProvider src) {
        StringBuilder sb = new StringBuilder();
        pickInt(src, IWriter.unicodeStream(sb));
        return Integer.parseInt(sb.toString());
    }

    public long pickInt64(UnicodeProvider src) {
        StringBuilder sb = new StringBuilder();
        pickInt(src, IWriter.unicodeStream(sb));
        return Long.parseLong(sb.toString());
    }

    public Number pickNumber(UnicodeProvider src) {
        StringBuilder sb = new StringBuilder();
        pickFloat(src, IWriter.unicodeStream(sb));
        return new TextNumber(sb.toString());
    }
}
