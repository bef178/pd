package libcliff.io;

public class ParsingException extends RuntimeException {

    /**
     * generated serial version UID
     */
    private static final long serialVersionUID = -4161558132029391877L;

    private int pos = -1;

    public ParsingException() {
        super();
    }

    public ParsingException(String detail) {
        this(detail, -1);
    }

    public ParsingException(String detail, int pos) {
        super(detail);
        this.pos = pos;
    }

    public ParsingException(int expectedCodePoint, int acturalCodePoint) {
        this(Character.toChars(expectedCodePoint).toString(),
                Character.toChars(acturalCodePoint).toString(), -1);
    }

    public ParsingException(int expectedCodePoint, int acturalCodePoint,
            int pos) {
        this(Character.toChars(expectedCodePoint).toString(),
                Character.toChars(acturalCodePoint).toString(), pos);
    }

    public ParsingException(String expected, String actual) {
        this(expected, actual, -1);
    }

    public ParsingException(String expected, String actual, int pos) {
        this("Excepted '" + expected + "', got '" + actual + "'", pos);
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
