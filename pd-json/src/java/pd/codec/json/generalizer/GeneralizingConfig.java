package pd.codec.json.generalizer;

import java.util.LinkedHashMap;

public class GeneralizingConfig {

    public final LinkedHashMap<Class<?>, MapToJsonFunc> refs = new LinkedHashMap<>();

    public <T> void register(Class<?> targetClass, MapToJsonFunc mapFunc) {
        if (mapFunc == null) {
            throw new NullPointerException();
        }
        // TODO should log: swapped out `{}` => `{}`", key, old
        refs.put(targetClass, mapFunc);
    }
}
