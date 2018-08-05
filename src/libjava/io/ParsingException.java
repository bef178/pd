package libjava.io;

public class ParsingException extends RuntimeException {

    /**
     * generated serial version UID
     */
    private static final long serialVersionUID = -4161558132029391877L;

    private int pos = -1;

    public ParsingException() {
        super();
    }

    public ParsingException(int expected, int actual) {
        this(expected, actual, -1);
    }

    public ParsingException(int expected, int actual, int pos) {
        this(Character.toChars(expected).toString(),
                Character.toChars(actual).toString(), pos);
    }

    public ParsingException(String message) {
        this(message, -1);
    }

    private ParsingException(String message, int pos) {
        super(message);
        this.pos = pos;
    }

    public ParsingException(String expected, String actual) {
        this(expected, actual, -1);
    }

    public ParsingException(String expected, String actual, int pos) {
        this(String.format("expected [%s] while actual [%s]", expected, actual), pos);
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
