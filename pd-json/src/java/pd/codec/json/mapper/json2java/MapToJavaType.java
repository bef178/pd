package pd.codec.json.mapper.json2java;

import pd.codec.json.datatype.Json;

@FunctionalInterface
public interface MapToJavaType<T> {
    Class<? extends T> map(Json json, String path, Class<T> targetClass);
}
