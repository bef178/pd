package pd.codec.json.serializer.java2json;

import pd.codec.json.datatype.Json;

public interface FindJsonInstanceFunc {
    Json map(Class<?> targetClass, Object instance);
}
