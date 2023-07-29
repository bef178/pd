package pd.codec.json.serializer.java2json;

import java.util.LinkedHashMap;

public class SerializeToJsonConfig {

    public final LinkedHashMap<Class<?>, FindJsonInstanceFunc> refs = new LinkedHashMap<>();

    public <T> void register(Class<?> targetClass, FindJsonInstanceFunc mapper) {
        if (mapper == null) {
            throw new NullPointerException();
        }
        // TODO should log: swapped out `{}` => `{}`", key, old
        refs.put(targetClass, mapper);
    }
}
