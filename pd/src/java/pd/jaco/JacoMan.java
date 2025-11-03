package pd.jaco;

import java.util.Arrays;

/**
 * Define T as a simple java container object type:
 * ```
 * T = Map<String, T> | List<T> | Object | NULL
 * ```
 * A path could be used to get/set value from/to the object.
 *
 * Furthermore, if restrict T:
 * ```
 * T = LinkedHashMap<String, T> | LinkedList<T> | String | Int64 | Float64 | Boolean | NULL
 * ```
 * it is a good carrier for json.
 */
public class JacoMan {

    private final JacoGetter jacoGetter = new JacoGetter();
    private final JacoSetter jacoSetter = new JacoSetter(jacoGetter);

    public Object getWithPath(Object o, String path) {
        checkPath(path);
        if (o == null) {
            return null;
        }
        return jacoGetter.get(o, Arrays.asList(path.split("/")));
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
}
