package pd.jaco;

public class JacoException extends RuntimeException {

    static final String KEY_NOT_INDEX = "KeyNotIndex: `%s`";
    static final String NEGATIVE_INDEX = "NegativeIndex: `%d`";
    static final String INVALID_DATA_TYPE = "InvalidDataType: `%s` not a valid data type";

    public static JacoException keyNotIndex(String key) {
        return new JacoException(String.format(KEY_NOT_INDEX, key));
    }

    public static JacoException negativeIndex(int i) {
        return new JacoException(String.format(NEGATIVE_INDEX, i));
    }

    public static JacoException invalidDataType(String className) {
        return new JacoException(String.format(INVALID_DATA_TYPE, className));
    }

    public JacoException(String message) {
        super(message);
    }
}
