package pd.jaco;

import java.util.Arrays;

import lombok.NonNull;
import pd.util.ObjectExtension;

/**
 * Define T as primitive data container object:
 * ```
 * T = LinkedHashMap<String, T> | LinkedList<T> | String | Int64 | Float64 | Boolean | NULL
 * ```
 * It is a native carrier/bridge for json. In Java, `T` can only be `Object`.
 * A path could be used to get/set value from/to the object.
 */
public class Jaco {

    private final JacoGetter jacoGetter;
    private final JacoSetter jacoSetter;

    public Jaco() {
        this.jacoGetter = new JacoGetter();
        this.jacoSetter = new JacoSetter(jacoGetter);
    }

    public Jaco(JacoGetter jacoGetter, JacoSetter jacoSetter) {
        this.jacoGetter = jacoGetter;
        this.jacoSetter = jacoSetter;
    }

    public <T> T getWithPath(Object o, String path, Class<T> targetClass) {
        checkPath(path);

        if (o == null) {
            return null;
        }

        Object value = jacoGetter.get(o, Arrays.asList(path.split("/")));
        if (value == null) {
            return null;
        }

        return convert(value, targetClass);
    }

    public Object setWithPath(Object o, String path, Object value) {
        checkPath(path);
        if (o == null) {
            o = jacoSetter.createWithNextKey(path.split("/")[0]);
        }
        jacoSetter.set(o, Arrays.asList(path.split("/")), value);
        return o;
    }

    private void checkPath(String path) {
        if (path == null) {
            throw JacoException.invalidPath("null");
        } else if (path.isEmpty()) {
            throw JacoException.invalidPath("empty string");
        }
    }

    private <T> T convert(@NonNull Object o, Class<T> targetClass) {
        T result = ObjectExtension.convert(o, targetClass);
        if (result == null) {
            throw new RuntimeException(String.format("NotConvertible: cannot convert `%s` to `%s`", o.getClass().getName(), targetClass.getName()));
        }
        return result;
    }
}
