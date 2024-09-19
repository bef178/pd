package pd.jaco;

import java.util.Arrays;

import static pd.util.ObjectExtension.convert;

/**
 * Define T as primitive data container object:
 * ```
 * T = LinkedHashMap<String, T> | ArrayList<T> | String | Int64 | Float64 | Boolean | NULL
 * ```
 * It is a native carrier/bridge for json. In Java, `T` can only be `Object`.
 * A path could be used to get/set value from/to the object.
 */
public class JacoExtension {

    private static final JacoGetter jacoGetter = new JacoGetter();
    private static final JacoSetter jacoSetter = new JacoSetter();

    public static <T> T getWithPath(Object o, String path, Class<T> targetClass) {
        checkPath(path);
        Object value = jacoGetter.get(o, Arrays.asList(path.split("/")));
        return convert(value, targetClass);
    }

    public static <T> T getOrNullWithPath(Object o, String path, Class<T> targetClass) {
        checkPath(path);
        Object value;
        try {
            value = jacoGetter.get(o, Arrays.asList(path.split("/")));
        } catch (JacoException ignored) {
            return null;
        }
        return convert(value, targetClass);
    }

    public static Object setWithPath(Object o, String path, Object value) {
        checkPath(path);
        return jacoSetter.set(o, Arrays.asList(path.split("/")), value);
    }

    static void checkPath(String path) {
        if (path == null) {
            throw JacoException.invalidPath("null");
        } else if (path.isEmpty()) {
            throw JacoException.invalidPath("empty string");
        }
    }
}
