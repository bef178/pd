package pd.util;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    private static final Getter getter = new Getter();
    private static final Setter setter = new Setter();

    public static <T> T get(Object o, String path, Class<T> targetClass) {
        if (path == null) {
            throw new IllegalArgumentException(INVALID_PATH_NULL);
        } else if (path.isEmpty()) {
            throw new IllegalArgumentException(INVALID_PATH_EMPTY_STRING);
        }
        List<String> keys = Arrays.asList(path.split("/"));
        o = getter.get(o, keys);
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
            o = getter.get(o, keys);
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
        return setter.set(o, keys, o1);
    }

    static class Getter {

        public Object get(Object o, List<String> keys) {
            for (String key : keys) {
                o = get(o, key);
            }
            return o;
        }

        public Object get(Object o, String... keys) {
            return get(o, Arrays.asList(keys));
        }

        /**
         * throws {@link JacoException}
         */
        public Object get(Object o, String key) {
            if (o == null) {
                throw JacoException.invalidCollection("null");
            } else if (o instanceof Map) {
                Map<?, ?> m = (Map<?, ?>) o;
                if (m.containsKey(key)) {
                    return m.get(key);
                } else {
                    throw JacoException.keyNotFound(o.getClass(), key);
                }
            } else if (o instanceof List) {
                List<?> a = (List<?>) o;
                int index;
                try {
                    index = Integer.parseInt(key);
                } catch (NumberFormatException e) {
                    throw JacoException.keyNotFound(o.getClass(), key);
                }
                if (index >= 0 && index < a.size()) {
                    return a.get(index);
                } else {
                    throw JacoException.keyNotFound(o.getClass(), key);
                }
            } else if (o.getClass().isArray()) {
                Object[] a = (Object[]) o;
                int index;
                try {
                    index = Integer.parseInt(key);
                } catch (NumberFormatException e) {
                    throw JacoException.keyNotFound(o.getClass(), key);
                }
                if (index >= 0 && index < a.length) {
                    return a[index];
                } else {
                    throw JacoException.keyNotFound(o.getClass(), key);
                }
            } else {
                // XXX get object field using reflection?
                throw JacoException.invalidCollection(o.getClass().getSimpleName());
            }
        }

        public Object getOrNull(Object o, String key) {
            try {
                return get(o, key);
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    static class Setter {

        public Object set(Object o, List<String> keys, Object o1) {
            for (int i = 0; i < keys.size() - 1; i++) {
                String key = keys.get(i);
                Object value = getter.getOrNull(o, key);
                if (value == null) {
                    // create collection
                    String nextKey = keys.get(i + 1);
                    boolean preferSequential = false;
                    try {
                        int nextIndex = Integer.parseInt(nextKey);
                        if (nextIndex >= 0 && nextIndex < 100) {
                            preferSequential = true;
                        }
                    } catch (Exception ignored) {
                    }
                    if (preferSequential) {
                        value = new LinkedList<>();
                    } else {
                        value = new LinkedHashMap<>();
                    }
                    set(o, key, value);
                }
                o = value;
            }
            return set(o, keys.get(keys.size() - 1), o1);
        }

        public Object set(Object o, String key, Object o1) {
            if (o == null) {
                throw JacoException.invalidCollection("null");
            } else if (o instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<Object, Object> m = (Map<Object, Object>) o;
                return m.put(key, o1);
            } else if (o instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> a = (List<Object>) o;
                int index;
                try {
                    index = Integer.parseInt(key);
                } catch (NumberFormatException e) {
                    throw JacoException.keyNotFound(o.getClass(), key);
                }
                if (index < 0) {
                    throw JacoException.keyNotFound(o.getClass(), key);
                } else if (index < a.size()) {
                    return a.set(index, o1);
                } else {
                    while (a.size() < index) {
                        a.add(null);
                    }
                    a.add(o1);
                    return null;
                }
            } else if (o.getClass().isArray()) {
                int index;
                try {
                    index = Integer.parseInt(key);
                } catch (NumberFormatException e) {
                    throw JacoException.keyNotFound(o.getClass(), key);
                }
                Object[] a = (Object[]) o;
                if (index >= 0 && index < a.length) {
                    Object old = a[index];
                    a[index] = o1;
                    return old;
                } else {
                    throw JacoException.keyNotFound(o.getClass(), key);
                }
            } else {
                // XXX get object field using reflection?
                throw JacoException.invalidCollection(o.getClass().getSimpleName());
            }
        }
    }

    public static class JacoException extends RuntimeException {

        static final String INVALID_COLLECTION = "InvalidCollection: `%s` not a Map, List or Array";
        static final String KEY_NOT_FOUND = "KeyNotFound: `%s` of `%s`";
        static final String NOT_CONVERTIBLE = "NotConvertible: cannot convert `%s` to `%s`";

        public static JacoException invalidCollection(String className) {
            return new JacoException(String.format(INVALID_COLLECTION, className));
        }

        public static JacoException keyNotFound(Class<?> clazz, String key) {
            return new JacoException(String.format(KEY_NOT_FOUND, key, clazz.getSimpleName()));
        }

        public static JacoException notConvertible(String srcClassName, String dstClassName) {
            return new JacoException(String.format(NOT_CONVERTIBLE, srcClassName, dstClassName));
        }

        public JacoException(String message) {
            super(message);
        }
    }
}
