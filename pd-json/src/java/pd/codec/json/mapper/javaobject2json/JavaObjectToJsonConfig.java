package pd.codec.json.mapper.javaobject2json;

import java.util.LinkedHashMap;

public class JavaObjectToJsonConfig {
    public final LinkedHashMap<Class<?>, IMapToJson> refs = new LinkedHashMap<>();

    public <T> void register(Class<?> targetClass, IMapToJson mapper) {
        if (mapper == null) {
            throw new NullPointerException();
        }
        // TODO should log: swapped out `{}` => `{}`", key, old
        refs.put(targetClass, mapper);
    }
}
