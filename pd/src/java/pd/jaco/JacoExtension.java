package pd.jaco;

import java.util.Arrays;
import java.util.List;

/**
 * Define T as primitive data container object:
 * ```
 * T = LinkedHashMap<String, T> | ArrayList<T> | String | Int64 | Float64 | Boolean | NULL
 * ```
 * It is a native carrier/bridge for json. In Java, `T` can only be `Object`.
 * A path could be used to get/set value from/to the object.
 */
public class JacoExtension {

    static final String INVALID_PATH_NULL = "InvalidPath: null";
    static final String INVALID_PATH_EMPTY_STRING = "InvalidPath: empty string";

    private static final JacoGetter jacoGetter = new JacoGetter();
    private static final JacoSetter jacoSetter = new JacoSetter();

    public static <T> T get(Object o, String path, Class<T> targetClass) {
        if (path == null) {
            throw new IllegalArgumentException(INVALID_PATH_NULL);
        } else if (path.isEmpty()) {
            throw new IllegalArgumentException(INVALID_PATH_EMPTY_STRING);
        }
        List<String> keys = Arrays.asList(path.split("/"));
        o = jacoGetter.get(o, keys);
        return convert(o, targetClass);
    }

    public static <T> T getOrNull(Object o, String path, Class<T> targetClass) {
        if (path == null) {
            throw new IllegalArgumentException(INVALID_PATH_NULL);
        } else if (path.isEmpty()) {
            throw new IllegalArgumentException(INVALID_PATH_EMPTY_STRING);
        }
        List<String> keys = Arrays.asList(path.split("/"));
        try {
            o = jacoGetter.get(o, keys);
        } catch (JacoException ignored) {
            return null;
        }
        try {
            return convert(o, targetClass);
        } catch (JacoException ignored) {
            return null;
        }
    }

    public static <T> T convert(Object o, Class<T> targetClass) {
        if (o == null) {
            return null;
        }
        if (targetClass.isAssignableFrom(o.getClass())) {
            return targetClass.cast(o);
        }
        if (targetClass == Double.class) {
            if (o.getClass() == Float.class) {
                return targetClass.cast(((Float) o).doubleValue());
            }
        } else if (targetClass == Long.class) {
            if (o.getClass() == Integer.class) {
                return targetClass.cast(((Integer) o).longValue());
            } else if (o.getClass() == Short.class) {
                return targetClass.cast(((Short) o).longValue());
            } else if (o.getClass() == Byte.class) {
                return targetClass.cast(((Byte) o).longValue());
            }
        } else if (targetClass == Integer.class) {
            if (o.getClass() == Short.class) {
                return targetClass.cast(((Short) o).intValue());
            } else if (o.getClass() == Byte.class) {
                return targetClass.cast(((Byte) o).intValue());
            }
        } else if (targetClass == Short.class) {
            if (o.getClass() == Byte.class) {
                return targetClass.cast(((Byte) o).shortValue());
            }
        }
        // XXX try json rebuild?
        throw JacoException.notConvertible(o.getClass().getName(), targetClass.getName());
    }

    public static Object set(Object o, String path, Object o1) {
        if (path == null) {
            throw new IllegalArgumentException(INVALID_PATH_NULL);
        } else if (path.isEmpty()) {
            throw new IllegalArgumentException(INVALID_PATH_EMPTY_STRING);
        }
        List<String> keys = Arrays.asList(path.split("/"));
        return jacoSetter.set(o, keys, o1);
    }
}
