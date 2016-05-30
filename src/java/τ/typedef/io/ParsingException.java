package τ.typedef.io;

public class ParsingException extends RuntimeException {

    public static ParsingException compose(String expected, String got) {
        return new ParsingException(
                "Excepted '" + expected + "', got '" + got);
    }

    public static ParsingException compose(String expected, int got) {
        return ParsingException.compose(expected,
                Character.toChars(got).toString());
    }

    /**
     * generated serial version UID
     */
    private static final long serialVersionUID = -4161558132029391877L;

    private int pos = -1;

    public ParsingException() {
        super();
    }

    public ParsingException(String detail) {
        super(detail);
    }

    public ParsingException(String detail, int pos) {
        super(detail);
        this.pos = pos;
    }

    @Override
    public String toString() {
        if (pos >= 0) {
            return super.toString() + "; pos:" + pos;
        } else {
            return super.toString();
        }
    }
}
