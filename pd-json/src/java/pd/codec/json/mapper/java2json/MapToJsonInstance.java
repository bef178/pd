package pd.codec.json.mapper.java2json;

import pd.codec.json.datatype.Json;

public interface MapToJsonInstance {
    Json map(Class<?> targetClass, Object instance);
}
