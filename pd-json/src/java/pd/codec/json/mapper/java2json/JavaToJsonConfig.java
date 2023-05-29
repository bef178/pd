package pd.codec.json.mapper.java2json;

import java.util.LinkedHashMap;

public class JavaToJsonConfig {
    public final LinkedHashMap<Class<?>, MapToJsonInstance> refs = new LinkedHashMap<>();

    public <T> void register(Class<?> targetClass, MapToJsonInstance mapper) {
        if (mapper == null) {
            throw new NullPointerException();
        }
        // TODO should log: swapped out `{}` => `{}`", key, old
        refs.put(targetClass, mapper);
    }
}
