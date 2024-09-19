package pd.jaco;

public class JacoException extends RuntimeException {

    static final String INVALID_COLLECTION = "InvalidCollection: `%s` not a Map, List or Array";
    static final String KEY_NOT_FOUND = "KeyNotFound: `%s` of `%s`";
    static final String INVALID_PATH = "Invalid path: %s";

    public static JacoException invalidCollection(String className) {
        return new JacoException(String.format(INVALID_COLLECTION, className));
    }

    public static JacoException keyNotFound(Class<?> clazz, String key) {
        return new JacoException(String.format(KEY_NOT_FOUND, key, clazz.getSimpleName()));
    }

    public static JacoException invalidPath(String reason) {
        return new JacoException(String.format(INVALID_PATH, reason));
    }

    public JacoException(String message) {
        super(message);
    }
}
