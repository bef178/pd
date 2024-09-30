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

    public final JacoFromEntityConverter.Config fromEntityConfig;
    public final JacoToEntityConverter.Config toEntityConfig;
    public final JacoToJsonSerializer.Config toJsonConfig;

    public JacoMan() {
        this.jacoGetter = new JacoGetter();
        this.jacoSetter = new JacoSetter(jacoGetter);
        this.fromEntityConfig = new JacoFromEntityConverter.Config();
        this.toEntityConfig = new JacoToEntityConverter.Config();
        this.toJsonConfig = new JacoToJsonSerializer.Config();
    }

    public JacoMan(
            @NonNull JacoGetter jacoGetter,
            @NonNull JacoSetter jacoSetter,
            @NonNull JacoFromEntityConverter.Config fromEntityConfig,
            @NonNull JacoToEntityConverter.Config toEntityConfig,
            @NonNull JacoToJsonSerializer.Config toJsonConfig) {
        this.jacoGetter = jacoGetter;
        this.jacoSetter = jacoSetter;
        this.fromEntityConfig = fromEntityConfig;
        this.toEntityConfig = toEntityConfig;
        this.toJsonConfig = toJsonConfig;
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
        if (jaco == null) {
            return null;
        }
        return new JacoToJsonSerializer(toJsonConfig).toJson(jaco);
    }

    public Object fromJson(String json) {
        if (json == null) {
            return null;
        }
        return new JacoFromJsonDeserializer().fromJson(json);
    }

    public <T> T toEntity(Object jaco, Class<T> targetClass, String startPath) {
        if (jaco == null) {
            return null;
        }
        return new JacoToEntityConverter(toEntityConfig).toEntity(jaco, targetClass, startPath);
    }

    public Object fromEntity(Object entity) {
        if (entity == null) {
            return null;
        }
        return new JacoFromEntityConverter(fromEntityConfig).fromEntity(entity);
    }
}
