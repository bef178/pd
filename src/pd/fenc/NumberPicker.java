package pd.fenc;

import static pd.fenc.IReader.EOF;

class NumberPicker {

    /**
     * exponent := ('E' / 'e') int
     */
    static void pickExponent(CharReader it, IWriter dst) {
        int ch = it.hasNext() ? it.next() : EOF;
        if (ch == 'E' || ch == 'e') {
            dst.append(ch);
            pickInt(it, dst);
            return;
        }
        String actual = new String(Character.toChars(ch));
        throw new ParsingException(
                String.format("unexpected [%s], expecting [E] or [e]", actual));
    }

    public static void pickFloat(CharReader it, IWriter dst) {
        pickNumber(it, dst);
    }

    public static float pickFloat32(CharReader it) {
        InstallmentByteBuffer dst = new InstallmentByteBuffer();
        pickFloat(it, dst);
        return Float.parseFloat(new String(dst.copyBytes()));
    }

    public static double pickFloat64(CharReader it) {
        InstallmentByteBuffer dst = new InstallmentByteBuffer();
        pickFloat(it, dst);
        return Double.parseDouble(new String(dst.copyBytes()));
    }

    /**
     * fraction := '.' 1*digit<br/>
     * <br/>
     * specially, "0.0" is valid<br/>
     */
    static void pickFraction(CharReader it, IWriter dst) {
        int state = 0;
        while (true) {
            switch (state) {
                case 0: {
                    it.eatOrThrow('.');
                    dst.append('.');
                    state = 1;
                    break;
                }
                case 1: {
                    // seen '.'
                    int ch = it.hasNext() ? it.next() : EOF;
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
                            dst.append(ch);
                            state = 2;
                            break;
                        default:
                            throw new ParsingException(ch);
                    }
                    break;
                }
                case 2: {
                    // a valid fraction is recognized
                    int ch = it.hasNext() ? it.next() : EOF;
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
                            dst.append(ch);
                            break;
                        default:
                            it.moveBack();
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
     * pick an valid 10-based integer of string form, per intuition
     */
    static void pickInt(CharReader it, IWriter dst) {
        int state = 0;
        while (true) {
            switch (state) {
                case 0: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    switch (ch) {
                        case '-':
                            dst.append(ch);
                            state = 1;
                            break;
                        case '0':
                            dst.append(ch);
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
                            dst.append(ch);
                            state = 3;
                            break;
                        default:
                            throw new ParsingException(ch);
                    }
                    break;
                }
                case 1: {
                    // seen the negative sign
                    int ch = it.hasNext() ? it.next() : EOF;
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
                            dst.append(ch);
                            state = 3;
                            break;
                        default:
                            // specially, "-0" is not acceptable
                            throw new ParsingException(ch);
                    }
                    break;
                }
                case 3: {
                    // a valid int is recognized
                    int ch = it.hasNext() ? it.next() : EOF;
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
                            dst.append(ch);
                            break;
                        default:
                            it.moveBack();
                            return;
                    }
                    break;
                }
            }
        }
    }

    public static int pickInt32(CharReader it) {
        InstallmentByteBuffer dst = new InstallmentByteBuffer();
        pickInt(it, dst);
        return Integer.parseInt(new String(dst.copyBytes()));
    }

    public static long pickInt64(CharReader it) {
        InstallmentByteBuffer dst = new InstallmentByteBuffer();
        pickInt(it, dst);
        return Long.parseLong(new String(dst.copyBytes()));
    }

    public static Number pickNumber(CharReader it) {
        InstallmentByteBuffer dst = new InstallmentByteBuffer();
        pickNumber(it, dst);
        return new NumberToken(new String(dst.copyBytes()));
    }

    /**
     * a number has 3 parts: integer, fraction and exponent
     */
    static void pickNumber(CharReader it, IWriter dst) {
        pickInt(it, dst);

        int ch = it.hasNext() ? it.next() : EOF;
        if (ch == EOF) {
            return;
        } else if (ch == '.') {
            it.moveBack();
            pickFraction(it, dst);
        }

        ch = it.hasNext() ? it.next() : EOF;
        if (ch == EOF) {
            return;
        } else if (ch == 'E' || ch == 'e') {
            it.moveBack();
            pickExponent(it, dst);
        }

        ch = it.hasNext() ? it.next() : EOF;
        if (ch == EOF) {
            return;
        } else {
            it.moveBack();
        }
    }
}
