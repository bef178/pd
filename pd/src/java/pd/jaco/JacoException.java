package pd.jaco;

public class JacoException extends RuntimeException {

    static final String KEY_NOT_INDEX = "KeyNotIndex: `%s`";
    static final String NEGATIVE_INDEX = "NegativeIndex: `%d`";
    static final String INDEX_TOO_LARGE = "IndexTooLarge: `%d` of `%d`";
    static final String INVALID_COLLECTION = "InvalidCollection: `%s` not a Map, List or Array";
    static final String INVALID_PATH = "InvalidPath: `%s`";

    public static JacoException keyNotIndex(String key) {
        return new JacoException(String.format(KEY_NOT_INDEX, key));
    }

    public static JacoException negativeIndex(int i) {
        return new JacoException(String.format(NEGATIVE_INDEX, i));
    }

    public static JacoException indexTooLarge(int i, int n) {
        return new JacoException(String.format(INDEX_TOO_LARGE, i, n));
    }

    public static JacoException invalidCollection(String className) {
        return new JacoException(String.format(INVALID_COLLECTION, className));
    }

    public static JacoException invalidPath(String reason) {
        return new JacoException(String.format(INVALID_PATH, reason));
    }

    public JacoException(String message) {
        super(message);
    }
}
