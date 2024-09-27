package pd.jaco;

import java.util.Arrays;

import lombok.NonNull;

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

    private final JacoGetter jacoGetter;
    private final JacoSetter jacoSetter;

    public JacoMan() {
        this.jacoGetter = new JacoGetter();
        this.jacoSetter = new JacoSetter(jacoGetter);
    }

    public JacoMan(JacoGetter jacoGetter, JacoSetter jacoSetter) {
        this.jacoGetter = jacoGetter;
        this.jacoSetter = jacoSetter;
    }

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

    public String toJson(Object jaco) {
        return toJson(jaco, new JacoToJsonSerializer.Config());
    }

    public String toJson(Object jaco, @NonNull JacoToJsonSerializer.Config config) {
        if (jaco == null) {
            return null;
        }
        return new JacoToJsonSerializer(config).toJson(jaco);
    }

    public Object fromJson(String json) {
        if (json == null) {
            return null;
        }
        return new JacoFromJsonDeserializer().fromJson(json);
    }

    public <T> T toEntity(Object jaco, Class<T> targetClass) {
        return toEntity(jaco, targetClass, new JacoToEntityConverter.Config());
    }

    public <T> T toEntity(Object jaco, Class<T> targetClass, @NonNull JacoToEntityConverter.Config config) {
        if (jaco == null) {
            return null;
        }
        return new JacoToEntityConverter(config).toEntity(jaco, targetClass);
    }

    public Object fromEntity(Object entity) {
        return fromEntity(entity, new JacoFromEntityConverter.Config());
    }

    public Object fromEntity(Object entity, @NonNull JacoFromEntityConverter.Config config) {
        if (entity == null) {
            return null;
        }
        return new JacoFromEntityConverter(config).fromEntity(entity);
    }
}
