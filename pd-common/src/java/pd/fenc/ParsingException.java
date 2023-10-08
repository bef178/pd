package pd.fenc;

import static pd.fenc.Int32Provider.EOF;

public class ParsingException extends RuntimeException {

    /**
     * generated serial version UID
     */
    private static final long serialVersionUID = -4161558132029391877L;

    public ParsingException() {
        super();
    }

    public ParsingException(int actual) {
        this(String.format("E: unexpected `%s`", actual == EOF ? "EOF" : Util.codepointToString(actual)));
    }

    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(Throwable cause) {
        super(cause);
    }
}
