package pd.fenc;

import static pd.fenc.IReader.EOF;

public class ParsingException extends RuntimeException {

    public enum Reason {
        NotIdentifier
    }

    /**
     * generated serial version UID
     */
    private static final long serialVersionUID = -4161558132029391877L;

    public ParsingException() {
        super();
    }

    public ParsingException(int actual) {
        this(String.format("unexpected [%s]", actual == EOF
                ? "EOF"
                : Character.toChars(actual).toString()));
    }

    public ParsingException(int expected, int actual) {
        this(Character.toChars(expected).toString(),
                Character.toChars(actual).toString());
    }

    public ParsingException(Reason reason) {
        this(reason.toString());
    }

    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(String expected, String actual) {
        this(String.format("expected [%s] while actual [%s]", expected, actual));
    }

    public ParsingException(Throwable cause) {
        super(cause);
    }
}
