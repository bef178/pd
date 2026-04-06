package pd.fenc;

import static pd.util.AsciiExtension.EOF;

public class ParsingException extends RuntimeException {

    /**
     * generated serial version UID
     */
    private static final long serialVersionUID = -4161558132029391877L;

    public static ParsingException unexpected(int actual) {
        if (actual == EOF) {
            return new ParsingException("E: unexpected value EOF");
        } else {
            return new ParsingException(String.format("E: unexpected value `0x%X`", actual));
        }
    }

    public static ParsingException expectedAndActual(int expected, int actual) {
        if (actual == EOF) {
            return new ParsingException(String.format("E: expected `0x%X`, actual EOF", expected));
        } else {
            return new ParsingException(String.format("E: expected `0x%X`, actual `0x%X`", expected, actual));
        }
    }

    public ParsingException() {
        super();
    }

    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(Throwable cause) {
        super(cause);
    }
}
